package com.fpvdrone.client.particle;

import com.fpvdrone.FpvConfig;
import com.fpvdrone.data.ParticleSpec;
import com.fpvdrone.data.SurfaceMaterial;
import com.fpvdrone.util.ParticleSendRange;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The 1.1.8 particle engine for explosions and impacts: data-driven particles from projectile files, and the
 * ground-debris decals shrapnel leaves on blocks. Up to 4,000 live particles, ticked in parallel during big bursts
 * (config {@code parallelParticleTicking}), faded out near the edge of the render distance and in unloaded chunks.
 */
public final class FpvParticles {
    private static final Logger LOGGER = LoggerFactory.getLogger("FPVDrone/Particles");
    private static final int MAX_PARTICLES = 4000;
    private static final double EDGE_FADE = 32.0;
    private static final int PARALLEL_THRESHOLD = 32;
    private static final List<FpvParticle> PARTICLES = new ArrayList<>();
    private static final Random RANDOM = new Random();
    private static final Map<BlockPos, Integer> LIGHT_CACHE = new HashMap<>();
    private static final Map<BlockPos, BlockPos> GROUND_CACHE = new HashMap<>();
    private static final AtomicBoolean PARALLEL_FAILED = new AtomicBoolean(false);
    private static final EnumMap<SurfaceMaterial, ParticleSpec> DECALS = new EnumMap<>(SurfaceMaterial.class);

    private FpvParticles() {
    }

    /** Registers the tick and render hooks; call once during client init. */
    public static void init() {
        ParticleRenderTypes.init();
        ClientTickEvents.END_CLIENT_TICK.register(mc -> tick());
        LevelRenderEvents.COLLECT_SUBMITS.register(FpvParticles::onCollectSubmits);
    }

    /** One particle entry {@code quantity} times at a point. {@code direction} orients FACING spawns and decals. */
    public static void spawn(ParticleSpec spec, Vec3 pos, @Nullable Vec3 inheritedVelocity, @Nullable Vec3 direction) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && spec != null) {
            int count = Math.max(1, spec.quantity());

            for (int i = 0; i < count && PARTICLES.size() < MAX_PARTICLES; i++) {
                PARTICLES.add(new FpvParticle(spec, level, pos, inheritedVelocity, direction, RANDOM));
            }
        }
    }

    public static void spawn(List<ParticleSpec> specs, Vec3 pos, @Nullable Vec3 inheritedVelocity, @Nullable Vec3 direction) {
        if (specs != null) {
            for (ParticleSpec spec : specs) {
                spawn(spec, pos, inheritedVelocity, direction);
            }
        }
    }

    /** A projectile's detonation effect ({@code "particles"} of its data file). */
    public static void spawnProjectileBurst(String projectileId, Vec3 pos, @Nullable Vec3 inheritedVelocity, @Nullable Vec3 direction) {
        spawn(com.fpvdrone.data.ProjectileDefinitions.impactParticles(projectileId), pos, inheritedVelocity, direction);
    }

    /** A shrapnel mark on a block face ({@code normal} points out of the face). */
    public static void spawnDecal(Vec3 pos, Vec3 normal, SurfaceMaterial material) {
        ParticleSpec spec = DECALS.computeIfAbsent(material, FpvParticles::decalSpec);
        spawn(spec, pos, null, normal);
    }

    private static ParticleSpec decalSpec(SurfaceMaterial material) {
        String p = "fpvdrone:textures/particles/bullethole_";
        List<String> textures = switch (material) {
            case STONE -> List.of(p + "stone0", p + "stone1", p + "stone2", p + "stone3");
            case METAL -> List.of(p + "metal0", p + "metal1", p + "metal2", p + "metal3");
            case WOOD -> List.of(p + "wood0", p + "wood1", p + "wood2", p + "wood3");
            case DIRT -> List.of(p + "dirt0", p + "dirt1", p + "dirt3", p + "dirt4");
            case CLAY -> List.of(p + "clay0", p + "clay1", p + "clay2", p + "clay3");
            case GLASS -> List.of(p + "glass0_translucent", p + "glass1_translucent");
            case OTHER -> List.of(
                p + "blank0", p + "blank1", p + "blank2", p + "blank3", p + "blanksoft0", p + "blanksoft1", p + "blanksoft2", p + "blanksoft3", p + "basic"
            );
        };
        return ParticleSpec.decal("decal_" + material.name().toLowerCase(Locale.ROOT), textures);
    }

    public static void clear() {
        PARTICLES.clear();
    }

    public static int count() {
        return PARTICLES.size();
    }

    static int cachedLight(BlockPos pos, ToIntFunction<BlockPos> compute) {
        Integer cached = LIGHT_CACHE.get(pos);
        if (cached != null) {
            return cached;
        }

        int light = compute.applyAsInt(pos);
        LIGHT_CACHE.put(pos, light);
        return light;
    }

    /** Ground lookups during a tick; synchronized because spawning may run while particles tick in parallel. */
    static BlockPos cachedGround(BlockPos pos, Function<BlockPos, BlockPos> compute) {
        synchronized (GROUND_CACHE) {
            return GROUND_CACHE.computeIfAbsent(pos, compute);
        }
    }

    /** Port addition (docs/WIND.md): the wind at the camera, blocks/tick, set each tick before particles move. */
    static volatile double windX;
    static volatile double windZ;

    private static void sampleWind(Minecraft mc) {
        if (!FpvConfig.isWindEnabled() || mc.gameRenderer == null) {
            windX = 0;
            windZ = 0;
            return;
        }
        var cam = mc.gameRenderer.mainCamera().position();
        org.joml.Vector3f base = com.fpvdrone.physics.wind.FpvWind.base(mc.level, cam.x, cam.y, cam.z, new org.joml.Vector3f());
        float shelter = com.fpvdrone.physics.wind.FpvWind.shelter(mc.level, cam.x, cam.y, cam.z, base);
        org.joml.Vector3f w = com.fpvdrone.physics.wind.FpvWind.at(mc.level, cam.x, cam.y, cam.z, shelter, new org.joml.Vector3f());
        windX = w.x / 20.0;
        windZ = w.z / 20.0;
    }

    private static void tickAll() {
        sampleWind(Minecraft.getInstance());
        if (FpvConfig.isParallelParticleTicking() && !PARALLEL_FAILED.get() && PARTICLES.size() >= PARALLEL_THRESHOLD) {
            try {
                PARTICLES.parallelStream().forEach(FpvParticle::tick);
                return;
            } catch (Exception e) {
                if (PARALLEL_FAILED.compareAndSet(false, true)) {
                    LOGGER.warn("Parallel particle tick failed; ticking on one thread for this session", e);
                }
            }
        }

        for (FpvParticle p : PARTICLES) {
            p.tick();
        }
    }

    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            PARTICLES.clear();
            return;
        }

        synchronized (GROUND_CACHE) {
            GROUND_CACHE.clear();
        }

        if (mc.isPaused() || PARTICLES.isEmpty()) {
            return;
        }

        Vec3 cam = mc.gameRenderer.mainCamera().position();
        ClientChunkCache chunks = mc.level.getChunkSource();
        boolean distantHorizons = ParticleSendRange.isDistantHorizonsLoaded();
        double fadeEnd = Math.max(64.0, ParticleSendRange.clientFade(mc.options.getEffectiveRenderDistance() * 16));
        double fadeStart = Math.max(0.0, fadeEnd - EDGE_FADE);
        double fadeWidth = fadeEnd - fadeStart;
        tickAll();

        for (int i = PARTICLES.size() - 1; i >= 0; i--) {
            FpvParticle p = PARTICLES.get(i);
            if (p.isRemoved()) {
                PARTICLES.remove(i);
            } else {
                float target = 0.0F;
                int cx = Mth.floor(p.x()) >> 4;
                int cz = Mth.floor(p.z()) >> 4;
                if (distantHorizons || chunks.hasChunk(cx, cz)) {
                    target = 1.0F;
                    if (fadeWidth > 0.0) {
                        double dx = p.x() - cam.x;
                        double dy = p.y() - cam.y;
                        double dz = p.z() - cam.z;
                        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (d > fadeStart) {
                            target = (float)Math.max(0.0, (fadeEnd - d) / fadeWidth);
                        }
                    }
                }

                p.updateVisibility(target);
            }
        }
    }

    private static void onCollectSubmits(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || PARTICLES.isEmpty()) {
            return;
        }

        LIGHT_CACHE.clear();
        Vec3 cam = context.levelState().cameraRenderState.pos;
        Quaternionf camRotation = new Quaternionf(mc.gameRenderer.mainCamera().rotation());
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        List<FpvParticle> visible = new ArrayList<>();

        for (FpvParticle p : PARTICLES) {
            if (p.visibility() > 0.0F && !p.isRemoved()) {
                visible.add(p);
            }
        }

        if (visible.isEmpty()) {
            return;
        }

        PoseStack poseStack = context.poseStack();
        // Same order as 1.1.8: opaque (depth-writing) first, then the faded ones far to near, lit before bright.
        submit(visible, FpvParticle::writesDepth, poseStack, context, cam, camRotation, partialTick);
        List<FpvParticle> faded = new ArrayList<>();

        for (FpvParticle p : visible) {
            if (!p.writesDepth()) {
                faded.add(p);
            }
        }

        sortFarToNear(faded, cam);
        submit(faded, p -> !p.isBright(), poseStack, context, cam, camRotation, partialTick);
        submit(faded, FpvParticle::isBright, poseStack, context, cam, camRotation, partialTick);
    }

    private static void submit(
        List<FpvParticle> list, Predicate<FpvParticle> filter, PoseStack poseStack, LevelRenderContext context, Vec3 cam, Quaternionf camRotation, float partialTick
    ) {
        Map<RenderType, List<FpvParticle>> groups = new LinkedHashMap<>();

        for (FpvParticle p : list) {
            if (filter.test(p)) {
                groups.computeIfAbsent(p.renderType(), k -> new ArrayList<>()).add(p);
            }
        }

        for (Map.Entry<RenderType, List<FpvParticle>> group : groups.entrySet()) {
            List<FpvParticle> members = group.getValue();
            context.submitNodeCollector().submitCustomGeometry(poseStack, group.getKey(), (pose, buffer) -> {
                for (FpvParticle p : members) {
                    p.render(buffer, pose, cam, camRotation, partialTick, p.visibility());
                }
            });
        }
    }

    private static void sortFarToNear(List<FpvParticle> list, Vec3 cam) {
        IdentityHashMap<FpvParticle, Double> dist = new IdentityHashMap<>();

        for (FpvParticle p : list) {
            double dx = p.x() - cam.x;
            double dy = p.y() - cam.y;
            double dz = p.z() - cam.z;
            dist.put(p, dx * dx + dy * dy + dz * dz);
        }

        list.sort((a, b) -> Double.compare(dist.get(b), dist.get(a)));
    }
}
