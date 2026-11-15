package com.fpvdrone.client.particle;

import com.fpvdrone.data.ParticleSpec;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * One data-driven particle (1.1.8): a textured quad that moves, collides, fades, scales, recolours and animates as its
 * {@link ParticleSpec} says. Ticked by {@link FpvParticles}, possibly on several threads (tick() touches only itself
 * and reads the level).
 */
final class FpvParticle {
    private static final double DECAL_LIFT = 0.02;
    private static final float[][] CORNERS = new float[][]{{-1.0F, -1.0F}, {-1.0F, 1.0F}, {1.0F, 1.0F}, {1.0F, -1.0F}};
    private static final float[][] UVS = new float[][]{{0.0F, 1.0F}, {0.0F, 0.0F}, {1.0F, 0.0F}, {1.0F, 1.0F}};
    private static final float VISIBILITY_STEP = 0.1F;
    private final ParticleSpec spec;
    private final ClientLevel level;
    private final @Nullable Vec3 direction;
    /** The block a decal sits on; it disappears when that block goes. */
    private final @Nullable BlockPos anchor;
    private final @Nullable Quaternionf decalRotation;
    private final float faceShade;
    private double x;
    private double y;
    private double z;
    private double xo;
    private double yo;
    private double zo;
    private double vx;
    private double vy;
    private double vz;
    private final double vx0;
    private final double vy0;
    private final double vz0;
    private final int maxAge;
    private int age;
    private boolean removed;
    private boolean onGround;
    private float roll;
    private float rollO;
    private float visibility;
    private boolean visibilitySet;
    private final boolean blockBreak;
    private float u0;
    private float u1;
    private float v0;
    private float v1;
    private int blockColor = -1;
    private final List<Identifier> textures = new ArrayList<>();
    private Identifier texture;
    private int textureIndex;
    private int textureDelayIndex;
    private float nextTextureTick;
    private int fixedColor = -1;
    private int colorFrom;
    private int colorTo;
    private int colorStartTick;
    private int colorEndTick;
    private int colorIndex;
    private int colorDelayIndex;

    FpvParticle(ParticleSpec spec, ClientLevel level, Vec3 origin, @Nullable Vec3 inheritedVelocity, @Nullable Vec3 direction, Random random) {
        this.spec = spec;
        this.level = level;
        this.direction = direction;
        this.maxAge = lifetime(spec, random);
        Vec3 start = origin;
        if (spec.pos() != null) {
            start = start.add(this.orient(spec.pos(), direction));
        }

        this.x = start.x;
        this.y = start.y;
        this.z = start.z;
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (spec.initialVelocity() != null) {
            float[] init = spec.initialVelocity();
            double ix;
            double iy;
            double iz;
            if (spec.spreadRandomness() != null) {
                float[] s = spec.spreadRandomness();
                ix = 2.0F * s[0] * random.nextDouble() - s[0] + init[0];
                iy = 2.0F * s[1] * random.nextDouble() - s[1] + init[1];
                iz = 2.0F * s[2] * random.nextDouble() - s[2] + init[2];
            } else {
                ix = init[0] + 0.2 - random.nextDouble() * 0.4;
                iy = init[1] + 0.2 - random.nextDouble() * 0.4;
                iz = init[2] + 0.2 - random.nextDouble() * 0.4;
            }

            Vec3 v = this.orient(new float[]{(float)(ix / 10.0), (float)(iy / 10.0), (float)(iz / 10.0)}, direction);
            this.vx = v.x;
            this.vy = v.y;
            this.vz = v.z;
        }

        if (spec.relativeInheritedVelocityFactor() != null && inheritedVelocity != null) {
            float[] f = spec.relativeInheritedVelocityFactor();
            this.vx += inheritedVelocity.x * f[0];
            this.vy += inheritedVelocity.y * f[1];
            this.vz += inheritedVelocity.z * f[2];
        }

        this.vx0 = this.vx;
        this.vy0 = this.vy;
        this.vz0 = this.vz;
        if (spec.renderingOrientation() == ParticleSpec.RenderOrientation.DECAL && direction != null && direction.lengthSqr() > 1.0E-8) {
            Vec3 n = direction.normalize();
            this.anchor = BlockPos.containing(this.x - n.x * 0.1, this.y - n.y * 0.1, this.z - n.z * 0.1);
            this.faceShade = level.cardinalLighting().byFace(Direction.getApproximateNearest(n.x, n.y, n.z));
            this.decalRotation = faceBasis(n);
        } else {
            this.anchor = null;
            this.faceShade = 1.0F;
            this.decalRotation = null;
        }

        this.blockBreak = spec.type() == ParticleSpec.Type.BREAK;
        if (this.blockBreak) {
            this.initBlockTexture(random);
        } else {
            this.initTextures(random);
        }

        this.initColors(random);
    }

    private void initBlockTexture(Random random) {
        BlockPos pos = this.groundPos();
        BlockState state = this.level.getBlockState(pos);
        this.texture = TextureAtlas.LOCATION_BLOCKS;
        this.nextTextureTick = this.maxAge;
        if (state.isAir()) {
            this.removed = true;
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
        int cu = random.nextInt(4);
        int cv = random.nextInt(4);
        float su0 = sprite.getU0();
        float su1 = sprite.getU1();
        float sv0 = sprite.getV0();
        float sv1 = sprite.getV1();
        this.u0 = su0 + (su1 - su0) * (cu / 4.0F);
        this.u1 = su0 + (su1 - su0) * ((cu + 1) / 4.0F);
        this.v0 = sv0 + (sv1 - sv0) * (cv / 4.0F);
        this.v1 = sv0 + (sv1 - sv0) * ((cv + 1) / 4.0F);
        this.textures.add(this.texture);
        if (this.spec.useBlockColor()) {
            this.blockColor = state.getMapColor(this.level, pos).col;
        }
    }

    private BlockPos groundPos() {
        BlockPos at = BlockPos.containing(this.x, this.y, this.z);
        if (this.spec.getBlockPropertiesFromGround()) {
            return FpvParticles.cachedGround(at, this::scanDown);
        }

        return this.level.getBlockState(at).isAir() ? at.below() : at;
    }

    private BlockPos scanDown(BlockPos from) {
        BlockPos.MutableBlockPos p = from.mutable();
        int minY = this.level.getMinY();

        for (int i = 0; i < 64 && p.getY() > minY; i++) {
            if (!this.level.getBlockState(p).isAir()) {
                return p.immutable();
            }

            p.move(Direction.DOWN);
        }

        return from;
    }

    private void initTextures(Random random) {
        if (this.spec.texture() != null) {
            this.textures.add(textureId(this.spec.texture()));
        } else if (!this.spec.textureList().isEmpty()) {
            for (String t : this.spec.textureList()) {
                this.textures.add(textureId(t));
            }
        } else {
            this.textures.add(Identifier.fromNamespaceAndPath("fpvdrone", "textures/particles/" + this.spec.type().name().toLowerCase(Locale.ROOT) + ".png"));
        }

        if (this.spec.randomTexture() && this.textures.size() > 1) {
            this.textureIndex = random.nextInt(this.textures.size());
        }

        this.texture = this.textures.get(this.textureIndex);
        this.nextTextureTick = this.textures.size() > 1 && !this.spec.textureDelays().isEmpty() ? this.spec.textureDelays().get(0) : this.maxAge;
    }

    private void initColors(Random random) {
        if (this.spec.toColor() != -1) {
            this.colorFrom = this.spec.color();
            this.colorTo = this.spec.toColor();
            this.colorStartTick = 0;
            this.colorEndTick = this.maxAge;
        } else if (!this.spec.colorList().isEmpty()) {
            List<Integer> list = this.spec.colorList();
            this.colorIndex = this.spec.randomColor() ? random.nextInt(list.size()) : 0;
            this.colorFrom = list.get(this.colorIndex);
            this.colorTo = this.colorIndex + 1 < list.size() ? list.get(this.colorIndex + 1) : list.get(0);
            this.colorStartTick = 0;
            this.colorEndTick = this.spec.colorDelays().isEmpty() ? this.maxAge : this.spec.colorDelays().get(0);
        } else {
            this.fixedColor = this.spec.color();
        }
    }

    void tick() {
        if (this.removed) {
            return;
        }

        if (this.age >= this.maxAge) {
            this.removed = true;
            return;
        }

        if (this.anchor != null && this.age % 10 == 0) {
            BlockState state = this.level.getBlockState(this.anchor);
            if (state.isAir() || state.getCollisionShape(this.level, this.anchor).isEmpty()) {
                this.removed = true;
                return;
            }
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.rollO = this.roll;
        if (!this.spec.stopsOnGround() || !this.onGround) {
            this.limitMovementSpeed();
            this.applyForces();
            this.applyTerminalVelocity();
            this.collide();
            this.x += this.vx;
            this.y += this.vy;
            this.z += this.vz;
            if (this.spec.rotationVelocity() != null) {
                this.roll += this.spec.rotationVelocity()[2];
            }
        }

        this.advanceTexture();
        this.advanceColor();
        this.age++;
    }

    /** movementDuration: the starting speed shrinks linearly to zero over that many ticks. */
    private void limitMovementSpeed() {
        int duration = this.spec.movementDuration();
        if (duration != 0 && this.age <= duration) {
            float left = (float)(duration - this.age) / duration;
            double mx = Math.abs(this.vx0 * left);
            double my = Math.abs(this.vy0 * left);
            double mz = Math.abs(this.vz0 * left);
            if (Math.abs(this.vx) > mx) {
                this.vx = Math.signum(this.vx) * mx;
            }

            if (Math.abs(this.vy) > my) {
                this.vy = Math.signum(this.vy) * my;
            }

            if (Math.abs(this.vz) > mz) {
                this.vz = Math.signum(this.vz) * mz;
            }
        }
    }

    private void applyForces() {
        boolean custom = false;
        if (this.spec.movementVelocity() != null) {
            float[] m = this.spec.movementVelocity();
            this.vx += m[0];
            this.vy += m[1];
            this.vz += m[2];
            custom = true;
        }

        if (this.spec.relativeMovementVelocity() != null) {
            Vec3 v = new Vec3(this.vx, this.vy, this.vz);
            Vec3 add = alongMotion(this.spec.relativeMovementVelocity(), v.lengthSqr() > 1.0E-6 ? v : null);
            this.vx += add.x;
            this.vy += add.y;
            this.vz += add.z;
            custom = true;
        }

        if (!custom) {
            switch (this.spec.type()) {
                case SMOKE -> {
                    this.vx *= 0.9;
                    this.vy += 0.004;
                    this.vz *= 0.9;
                }
                case FLAME -> {
                    this.vx *= 0.96;
                    this.vy *= 0.96;
                    this.vz *= 0.96;
                }
                case BUBBLE -> {
                    if (this.level.getFluidState(BlockPos.containing(this.x, this.y, this.z)).isEmpty()) {
                        this.removed = true;
                    } else {
                        this.vx *= 0.85;
                        this.vy = this.vy * 0.85 + 0.002;
                        this.vz *= 0.85;
                    }
                }
                case BREAK -> {
                    if (!this.onGround) {
                        this.vx *= 0.98;
                        this.vy = this.vy * 0.98 - 0.04;
                        this.vz *= 0.98;
                    } else {
                        this.vx = 0.0;
                        this.vy = 0.0;
                        this.vz = 0.0;
                    }
                }
                case GENERIC -> {
                }
            }
        }

        this.drift(custom);
    }

    /**
     * Port addition (docs/WIND.md): light particles are carried by the wind. Each tick the horizontal speed moves
     * this share of the way to the wind: smoke 0.2, flames and custom particles 0.03, airborne debris 0.01.
     */
    private void drift(boolean custom) {
        double wx = FpvParticles.windX;
        double wz = FpvParticles.windZ;
        if (wx == 0 && wz == 0) {
            return;
        }
        double k;
        if (custom) {
            k = 0.03;
        } else {
            k = switch (this.spec.type()) {
                case SMOKE -> 0.2;
                case FLAME, GENERIC -> 0.03;
                case BREAK -> this.onGround ? 0.0 : 0.01;
                case BUBBLE -> 0.0;
            };
        }
        this.vx += (wx - this.vx) * k;
        this.vz += (wz - this.vz) * k;
    }

    private void applyTerminalVelocity() {
        float[] t = this.spec.terminalVelocity();
        if (t != null) {
            this.vx = clampAbs(this.vx, t[0]);
            this.vy = clampAbs(this.vy, t[1]);
            this.vz = clampAbs(this.vz, t[2]);
        }
    }

    private void collide() {
        if (!this.spec.ignoreCollision()) {
            BlockPos next = BlockPos.containing(this.x + this.vx, this.y + this.vy, this.z + this.vz);
            BlockState state = this.level.getBlockState(next);
            if (!state.isAir() && state.isCollisionShapeFullBlock(this.level, next)) {
                this.onGround = true;
                this.vx = 0.0;
                this.vz = 0.0;
                if (this.vy < 0.0) {
                    this.vy = 0.0;
                }
            }
        }
    }

    private void advanceTexture() {
        if (this.textures.size() > 1 && !this.spec.textureDelays().isEmpty() && this.nextTextureTick <= this.age) {
            if (++this.textureIndex >= this.textures.size()) {
                this.textureIndex = 0;
            }

            this.texture = this.textures.get(this.textureIndex);
            if (++this.textureDelayIndex >= this.spec.textureDelays().size()) {
                this.textureDelayIndex = 0;
            }

            this.nextTextureTick += this.spec.textureDelays().get(this.textureDelayIndex);
        }
    }

    private void advanceColor() {
        if (!this.spec.colorDelays().isEmpty() && !this.spec.colorList().isEmpty() && this.colorEndTick == this.age) {
            List<Integer> list = this.spec.colorList();
            if (++this.colorIndex >= list.size()) {
                this.colorIndex = 0;
            }

            this.colorFrom = list.get(this.colorIndex);
            this.colorTo = this.colorIndex + 1 < list.size() ? list.get(this.colorIndex + 1) : list.get(0);
            if (++this.colorDelayIndex >= this.spec.colorDelays().size()) {
                this.colorDelayIndex = 0;
            }

            this.colorStartTick = this.colorEndTick;
            this.colorEndTick = this.colorEndTick + this.spec.colorDelays().get(this.colorDelayIndex);
        }
    }

    /** Emits the quad, camera-relative, into the buffer of {@link #renderType()}. */
    void render(VertexConsumer buffer, PoseStack.Pose pose, Vec3 cam, Quaternionf camRotation, float partialTick, float fade) {
        float alpha = this.alpha(partialTick) * fade;
        if (this.removed || alpha <= 0.0F) {
            return;
        }

        float half = this.scale(partialTick) * 0.5F;
        int rgb = this.color(partialTick);
        float r = (rgb >> 16 & 0xFF) / 255.0F * this.faceShade;
        float g = (rgb >> 8 & 0xFF) / 255.0F * this.faceShade;
        float b = (rgb & 0xFF) / 255.0F * this.faceShade;
        if (this.blockColor != -1) {
            r *= (this.blockColor >> 16 & 0xFF) / 255.0F;
            g *= (this.blockColor >> 8 & 0xFF) / 255.0F;
            b *= (this.blockColor & 0xFF) / 255.0F;
        }

        double px = this.xo + (this.x - this.xo) * partialTick - cam.x;
        double py = this.yo + (this.y - this.yo) * partialTick - cam.y;
        double pz = this.zo + (this.z - this.zo) * partialTick - cam.z;
        int light;
        if (this.isDecal() && this.direction != null && this.direction.lengthSqr() > 1.0E-8) {
            Vec3 n = this.direction.normalize();
            px += n.x * DECAL_LIFT;
            py += n.y * DECAL_LIFT;
            pz += n.z * DECAL_LIFT;
            light = this.spec.bright()
                ? LightCoordsUtil.FULL_BRIGHT
                : LightCoordsUtil.getLightCoords(this.level, BlockPos.containing(this.x + n.x * 0.5, this.y + n.y * 0.5, this.z + n.z * 0.5));
        } else {
            light = this.spec.bright() ? LightCoordsUtil.FULL_BRIGHT : this.lightAt();
        }

        Quaternionf rot = this.facing(cam, camRotation);
        float rollRad = (float)Math.toRadians(this.rollO + (this.roll - this.rollO) * partialTick);
        if (rollRad != 0.0F) {
            rot.rotateZ(rollRad);
        }

        Vector3f corner = new Vector3f();

        for (int i = 0; i < 4; i++) {
            corner.set(CORNERS[i][0] * half, CORNERS[i][1] * half, 0.0F);
            rot.transform(corner);
            float u = this.blockBreak ? (UVS[i][0] == 0.0F ? this.u0 : this.u1) : UVS[i][0];
            float v = this.blockBreak ? (UVS[i][1] == 0.0F ? this.v0 : this.v1) : UVS[i][1];
            buffer.addVertex(pose, (float)px + corner.x, (float)py + corner.y, (float)pz + corner.z).setUv(u, v).setColor(r, g, b, alpha).setLight(light);
        }
    }

    RenderType renderType() {
        return ParticleRenderTypes.get(this.texture, this.spec.bright(), this.spec.blended(), this.writesDepth(), this.isDecal());
    }

    private boolean isDecal() {
        return this.spec.renderingOrientation() == ParticleSpec.RenderOrientation.DECAL;
    }

    private Quaternionf facing(Vec3 cam, Quaternionf camRotation) {
        return switch (this.spec.renderingOrientation()) {
            case PLAYER -> new Quaternionf(camRotation);
            case YAXIS -> new Quaternionf().rotateY((float)Math.atan2(cam.x - this.x, cam.z - this.z));
            case MOTION -> {
                Vec3 v = new Vec3(this.vx, this.vy, this.vz);
                if (v.lengthSqr() < 1.0E-8) {
                    yield new Quaternionf(camRotation);
                }

                v = v.normalize();
                yield new Quaternionf().rotateTo(0.0F, 1.0F, 0.0F, (float)v.x, (float)v.y, (float)v.z);
            }
            case DECAL -> this.decalRotation != null ? new Quaternionf(this.decalRotation) : new Quaternionf();
            case FIXED -> new Quaternionf();
        };
    }

    /** Quad lying in the plane whose normal is {@code n}. */
    private static Quaternionf faceBasis(Vec3 n) {
        Vec3 ref = Math.abs(n.y) > 0.9 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 right = ref.cross(n).normalize();
        Vec3 up = n.cross(right).normalize();
        Matrix3f m = new Matrix3f();
        m.m00 = (float)right.x;
        m.m01 = (float)right.y;
        m.m02 = (float)right.z;
        m.m10 = (float)up.x;
        m.m11 = (float)up.y;
        m.m12 = (float)up.z;
        m.m20 = (float)n.x;
        m.m21 = (float)n.y;
        m.m22 = (float)n.z;
        return new Quaternionf().setFromNormalized(m);
    }

    /** Light at the particle; inside a solid block, the brightest open neighbour (so smoke in a wall is not black). */
    private int lightAt() {
        return FpvParticles.cachedLight(BlockPos.containing(this.x, this.y, this.z), this::computeLight);
    }

    private int computeLight(BlockPos pos) {
        if (!this.level.getBlockState(pos).isSolidRender()) {
            return LightCoordsUtil.getLightCoords(this.level, pos);
        }

        int block = 0;
        int sky = 0;
        boolean open = false;

        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            if (!this.level.getBlockState(n).isSolidRender()) {
                int packed = LightCoordsUtil.getLightCoords(this.level, n);
                block = Math.max(block, LightCoordsUtil.block(packed));
                sky = Math.max(sky, LightCoordsUtil.sky(packed));
                open = true;
            }
        }

        return open ? LightCoordsUtil.pack(block, sky) : LightCoordsUtil.getLightCoords(this.level, pos);
    }

    private float alpha(float partialTick) {
        float t = (this.age + partialTick) / this.maxAge;
        float from = this.spec.transparency();
        float to = this.spec.toTransparency();
        boolean hasFrom = from >= 0.0F;
        boolean hasTo = to >= 0.0F;
        float start = hasFrom ? from : 1.0F;
        float a;
        if (hasTo) {
            a = lerp(start, to, clamp01(t));
        } else if (hasFrom) {
            // 1.1.8 fades a particle with only "transparency" set towards 0 over its life.
            a = lerp(start, 0.0F, clamp01(t));
        } else {
            a = 1.0F;
        }

        int fadeIn = this.spec.fadeInTransparencyTime();
        if (fadeIn > 0 && fadeIn >= this.age) {
            a *= (float)this.age / fadeIn;
        }

        int fadeOut = this.spec.fadeOutTransparencyTime();
        if (fadeOut > 0 && fadeOut > this.maxAge - this.age) {
            a *= (float)(this.maxAge - this.age) / fadeOut;
        }

        if (this.spec.daytimeReductionFactor() != 0.0F) {
            float sky = this.level.getBrightness(LightLayer.SKY, BlockPos.containing(this.x, this.y, this.z)) / 15.0F;
            a *= 1.0F - this.spec.daytimeReductionFactor() * sky;
        }

        return clamp01(a);
    }

    private float scale(float partialTick) {
        float t = (this.age + partialTick) / this.maxAge;
        float s;
        if (this.spec.type() == ParticleSpec.Type.FLAME && this.spec.scale() == 0.0F && this.spec.toScale() == 0.0F) {
            s = 1.0F - t * t / 2.0F;
        } else if (this.spec.toScale() != 0.0F) {
            s = lerp(this.spec.scale(), this.spec.toScale(), t);
        } else if (this.spec.scale() != 0.0F) {
            s = this.spec.scale();
        } else {
            s = 1.0F;
        }

        int fadeIn = this.spec.fadeInScaleTime();
        if (fadeIn > 0 && fadeIn >= this.age) {
            s *= 1.0F - (float)(fadeIn - this.age) / fadeIn;
        }

        int fadeOut = this.spec.fadeOutScaleTime();
        if (fadeOut > 0 && fadeOut > this.maxAge - this.age) {
            s *= (float)(this.maxAge - this.age) / fadeOut;
        }

        return Math.max(0.0F, s);
    }

    private int color(float partialTick) {
        if (this.fixedColor != -1) {
            return this.fixedColor;
        }

        float span = this.colorEndTick - this.colorStartTick;
        float t = span <= 0.0F ? 1.0F : clamp01((this.age + partialTick - this.colorStartTick) / span);
        return lerpColor(this.colorFrom, this.colorTo, t);
    }

    boolean isRemoved() {
        return this.removed;
    }

    /** Smoothly follows the distance/chunk visibility target (0..1), at most 0.1 per tick. */
    void updateVisibility(float target) {
        float t = clamp01(target);
        if (!this.visibilitySet) {
            this.visibility = t;
            this.visibilitySet = true;
        } else if (this.visibility < t) {
            this.visibility = Math.min(t, this.visibility + VISIBILITY_STEP);
        } else if (this.visibility > t) {
            this.visibility = Math.max(t, this.visibility - VISIBILITY_STEP);
        }
    }

    float visibility() {
        return this.visibility;
    }

    double x() {
        return this.x;
    }

    double y() {
        return this.y;
    }

    double z() {
        return this.z;
    }

    boolean isBright() {
        return this.spec.bright();
    }

    /** Fully opaque particles write depth and are drawn first, unsorted; everything that fades does not. */
    boolean writesDepth() {
        if (this.spec.blended()) {
            return false;
        }

        float from = this.spec.transparency();
        if (from >= 0.0F && from < 1.0F) {
            return false;
        }

        float to = this.spec.toTransparency();
        if (to >= 0.0F && to < 1.0F) {
            return false;
        }

        if (this.spec.fadeInTransparencyTime() > 0 || this.spec.fadeOutTransparencyTime() > 0) {
            return false;
        }

        if (this.spec.daytimeReductionFactor() != 0.0F) {
            return false;
        }

        return this.texture == null || !this.texture.getPath().contains("_translucent");
    }

    private static int lifetime(ParticleSpec spec, Random random) {
        int age;
        if (spec.duration() != 0) {
            if (spec.durationRandomness() != 0) {
                return Math.max(1, spec.duration() + (int)(spec.durationRandomness() * 2 * random.nextFloat() - spec.durationRandomness()));
            }

            age = spec.duration();
        } else {
            age = switch (spec.type()) {
                case SMOKE -> 33;
                case FLAME, BUBBLE -> (int)(8.0 / (random.nextDouble() * 0.8 + 0.2)) + 4;
                case BREAK -> (int)(4.0 / (random.nextDouble() * 0.9 + 0.1));
                default -> (int)(8.0 / (random.nextDouble() * 0.8 + 0.2));
            };
        }

        return Math.max(1, age);
    }

    /** FACING: x = sideways, y = along the burst direction, z = the other side axis. Otherwise world axes. */
    private Vec3 orient(float[] v, @Nullable Vec3 dir) {
        return this.spec.spawningOrientation() == ParticleSpec.SpawnOrientation.FACING && dir != null && dir.lengthSqr() > 1.0E-8
            ? alongFacing(v, dir)
            : new Vec3(v[0], v[1], v[2]);
    }

    private static Vec3 alongFacing(float[] v, Vec3 dir) {
        Vec3 d = dir.normalize();
        Vec3 ref = Math.abs(d.y) > 0.999 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = ref.cross(d).normalize();
        Vec3 other = side.cross(d).normalize();
        return side.scale(v[0]).add(d.scale(v[1])).add(other.scale(v[2]));
    }

    /** relativeMovementVelocity: x/y across the motion, z along it. */
    private static Vec3 alongMotion(float[] v, @Nullable Vec3 motion) {
        Vec3 raw = new Vec3(v[0], v[1], v[2]);
        if (motion == null || motion.lengthSqr() < 1.0E-8) {
            return raw;
        }

        Vec3 fwd = motion.normalize();
        Vec3 ref = Math.abs(fwd.y) > 0.999 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 side = ref.cross(fwd).normalize();
        Vec3 up = fwd.cross(side).normalize();
        return side.scale(raw.x).add(up.scale(raw.y)).add(fwd.scale(raw.z));
    }

    private static Identifier textureId(String raw) {
        String id = raw.contains(":") ? raw : "fpvdrone:" + raw;
        if (!id.endsWith(".png")) {
            id = id + ".png";
        }

        return Identifier.parse(id);
    }

    private static double clampAbs(double v, float limit) {
        return v > limit ? limit : (v < -limit ? -limit : v);
    }

    private static float clamp01(float v) {
        return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static int lerpColor(int from, int to, float t) {
        int r = Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
        int g = Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return r << 16 | g << 8 | b;
    }
}
