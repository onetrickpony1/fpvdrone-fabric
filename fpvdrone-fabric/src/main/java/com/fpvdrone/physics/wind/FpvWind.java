package com.fpvdrone.physics.wind;

import com.fpvdrone.FpvConfig;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The wind a drone feels (docs/WIND.md, build step 1), in m/s (1 block = 1 m), horizontal only. Same code on
 * client and server, so the pilot's client and the server agree without extra packets.
 *
 * <ol>
 *   <li><b>Base wind</b> (the "wind at 6 m over open ground"): from Weathery when it is installed (its wind
 *       direction, storm inflow, gusts and high-wind spells), otherwise a built-in wind that drifts slowly with game
 *       time and rises in rain and thunder.</li>
 *   <li><b>Height profile:</b> log law over the local ground, rougher (slower near the ground) in forests.</li>
 *   <li><b>Shelter:</b> blocks standing above the drone upwind (walls, hills, trees) take most of the wind away.</li>
 * </ol>
 * Wind off ({@code windEnabled} false) returns zero, which gives exactly the 1.1.8 handling.
 */
public final class FpvWind {
    private static final Logger LOGGER = LoggerFactory.getLogger(FpvWind.class);
    /** m/s for Weathery's unitless speed 1.0 (a high-wind spell); normal Weathery wind 0.1..0.6 = 1..5 m/s. */
    public static final float WEATHERY_SCALE = 8.0F;
    private static final float REFERENCE_HEIGHT = 6.0F;
    private static final int SHELTER_SAMPLES = 12;
    private static final float SHELTER_REACH = 24.0F;

    private static final MethodHandle WEATHERY_ANGLE;
    private static final MethodHandle WEATHERY_SPEED;

    static {
        MethodHandle angle = null;
        MethodHandle speed = null;
        if (FabricLoader.getInstance().isModLoaded("weathery")) {
            try {
                Class<?> api = Class.forName("dev.weathery.api.WeatheryApi");
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                angle = lookup.findStatic(api, "windAngle", MethodType.methodType(float.class, Level.class, Vec3.class));
                speed = lookup.findStatic(api, "windSpeed", MethodType.methodType(float.class, Level.class, BlockPos.class, float.class));
                LOGGER.info("Drone wind comes from Weathery");
            } catch (ReflectiveOperationException | LinkageError e) {
                LOGGER.warn("Weathery is installed but its wind API could not be found; using the built-in wind", e);
                angle = null;
                speed = null;
            }
        }
        WEATHERY_ANGLE = angle;
        WEATHERY_SPEED = speed;
    }

    private FpvWind() {
    }

    /** True when the wind comes from Weathery. */
    public static boolean fromWeathery() {
        return WEATHERY_SPEED != null;
    }

    /** Base wind (6 m, open ground) at a position, m/s, into {@code out} (x, z; y = 0). */
    public static Vector3f base(Level level, double x, double y, double z, Vector3f out) {
        out.set(0, 0, 0);
        if (!FpvConfig.isWindEnabled() || level == null) {
            return out;
        }
        float angleDeg;
        float speed;
        if (WEATHERY_SPEED != null) {
            try {
                angleDeg = (float)WEATHERY_ANGLE.invokeExact(level, new Vec3(x, y, z));
                speed = (float)WEATHERY_SPEED.invokeExact(level, BlockPos.containing(x, y, z), 0.0F) * WEATHERY_SCALE;
            } catch (Throwable t) {
                return out;
            }
        } else {
            double t = level.getGameTime();
            angleDeg = builtinAngle(t);
            speed = builtinSpeed(t, level.getRainLevel(1.0F), level.getThunderLevel(1.0F));
        }
        speed *= FpvConfig.getWindStrength();
        // Weathery angle convention: wind at angle a blows toward (-sin a, cos a).
        float a = angleDeg * Mth.DEG_TO_RAD;
        return out.set(-Mth.sin(a) * speed, 0, Mth.cos(a) * speed);
    }

    /** Built-in wind direction: wanders all the way round over a few in-game days, with slower swings on top. */
    static float builtinAngle(double t) {
        return (float)(180 + 140 * Math.sin(t / 19000.0) + 50 * Math.sin(t / 3100.0 + 1.3) + 12 * Math.sin(t / 410.0 + 0.4));
    }

    /** Built-in wind speed, m/s at 6 m: 1..4 in clear weather, up to about 7 in a thunderstorm. */
    static float builtinSpeed(double t, float rain, float thunder) {
        double wander = 0.5 + 0.3 * Math.sin(t / 5300.0 + 2.1) + 0.15 * Math.sin(t / 870.0) + 0.05 * Math.sin(t / 97.0);
        double band = 4.0 + 2.0 * rain + 2.5 * thunder;
        return (float)(band * Mth.clamp(wander, 0.15, 1.0));
    }

    // ---- gusts (docs/WIND.md: discrete 1-cosine gusts) ----

    /** Gust schedule window, ticks: at most one gust per window. */
    private static final int GUST_WINDOW = 400;

    /**
     * Extra wind speed of the current gust, m/s at 6 m, for a mean wind of {@code mean} m/s: {@code (Vm/2)(1 -
     * cos(pi s / dm))} over {@code 0 <= s <= 2 dm}, with {@code Vm} 0.3-0.6 x mean and {@code dm} 5-30 m. The
     * schedule comes from game time alone (a hash per 20 s window, 60 % of windows have a gust), so client and server
     * see the same gusts with no packets.
     */
    public static float gust(double gameTime, float mean) {
        if (!FpvConfig.isWindGusts() || mean < 0.3F) {
            return 0.0F;
        }
        long window = (long)Math.floor(gameTime / GUST_WINDOW);
        for (long wdw = window - 1; wdw <= window; wdw++) {
            long h = mix(wdw * 0x9E3779B97F4A7C15L + 0x632BE59BD9B4E019L);
            if ((h & 0xFF) >= 154) {
                continue; // 60 % of windows
            }
            double start = wdw * GUST_WINDOW + ((h >>> 8) & 0xFFFF) / 65536.0 * GUST_WINDOW;
            float vm = mean * (0.3F + 0.3F * (((h >>> 24) & 0xFF) / 255.0F));
            float dm = 5.0F + 25.0F * (((h >>> 32) & 0xFF) / 255.0F);
            double s = (gameTime - start) / 20.0 * Math.max(mean, 2.0F);
            if (s >= 0 && s <= 2 * dm) {
                return (float)(vm / 2 * (1 - Math.cos(Math.PI * s / dm)));
            }
        }
        return 0.0F;
    }

    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Wind at the drone: (base wind + gust) × height profile × (1 − 0.8 × shelter). {@code shelter} is the caller's smoothed
     * shelter value (0..1) from {@link #shelter}.
     */
    public static Vector3f at(Level level, double x, double y, double z, float shelter, Vector3f out) {
        base(level, x, y, z, out);
        if (out.lengthSquared() < 1.0E-6F) {
            return out;
        }
        float mean = out.length();
        out.mul(1.0F + gust(level.getGameTime(), mean) / mean);
        float k = profile(level, x, y, z) * (1.0F - 0.8F * Mth.clamp(shelter, 0.0F, 1.0F));
        return out.mul(k);
    }

    /** Height of {@code y} above the local ground (no leaves), at least 0. */
    public static float heightAboveGround(Level level, double x, double y, double z) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        if (!level.hasChunk(bx >> 4, bz >> 4)) {
            return REFERENCE_HEIGHT;
        }
        return (float)Math.max(0.0, y - level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz));
    }

    /** Log-law factor for the height above the local ground (1 at 6 m; about 0.5 at 0.5 m; capped at 1.6). */
    public static float profile(Level level, double x, double y, double z) {
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        if (!level.hasChunk(bx >> 4, bz >> 4)) {
            return 1.0F;
        }
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
        float h = (float)Math.max(0.5, y - ground);
        float z0 = level.getBiome(BlockPos.containing(x, y, z)).is(BiomeTags.IS_FOREST) || level.getBiome(BlockPos.containing(x, y, z)).is(BiomeTags.IS_JUNGLE) ? 0.5F : 0.03F;
        h = Math.max(h, z0 * 1.5F);
        float f = (float)(Math.log(h / z0) / Math.log(REFERENCE_HEIGHT / z0));
        return Mth.clamp(f, 0.0F, 1.6F);
    }

    /**
     * How sheltered the drone is from {@code wind} (0 open .. 1 fully sheltered): marches upwind over the
     * heightmap; a block column standing {@code e} blocks above the drone at distance {@code d} shelters it fully
     * while {@code d < 3.3 e} (the wake behind an obstacle reaches 3-5 obstacle heights). Under a roof = 1.
     */
    public static float shelter(Level level, double x, double y, double z, Vector3f wind) {
        if (!FpvConfig.isWindSheltering()) {
            return 0.0F;
        }
        float speed = (float)Math.sqrt(wind.x * wind.x + wind.z * wind.z);
        if (speed < 0.05F) {
            return 0.0F;
        }
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        if (!level.hasChunk(bx >> 4, bz >> 4)) {
            return 0.0F;
        }
        if (level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz) > y + 1.0) {
            return 1.0F;
        }
        float ux = -wind.x / speed;
        float uz = -wind.z / speed;
        float best = 0.0F;
        for (int i = 1; i <= SHELTER_SAMPLES; i++) {
            float d = SHELTER_REACH * i / SHELTER_SAMPLES;
            int sx = Mth.floor(x + ux * d);
            int sz = Mth.floor(z + uz * d);
            if (!level.hasChunk(sx >> 4, sz >> 4)) {
                break;
            }
            float excess = (float)(level.getHeight(Heightmap.Types.MOTION_BLOCKING, sx, sz) - y);
            if (excess > 0) {
                best = Math.max(best, Mth.clamp(excess * 3.3F / d, 0.0F, 1.0F));
                if (best >= 1.0F) {
                    break;
                }
            }
        }
        return best;
    }
}
