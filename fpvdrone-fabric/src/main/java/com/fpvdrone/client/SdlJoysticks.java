package com.fpvdrone.client;

import static org.lwjgl.sdl.SDLGamepad.*;
import static org.lwjgl.sdl.SDLJoystick.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.sdl.SDLGUID;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDL_GUID;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GLFW-joystick-shaped access to controllers through SDL3 (Minecraft 26.x replaced GLFW with SDL3).
 * A "jid" is the device's index in SDL's current joystick list. Gamepad axes and buttons are reported in GLFW's
 * gamepad order and ranges, so calibration data and channel numbers mean the same thing as in the Forge mod.
 * All calls must happen on the render thread.
 */
public final class SdlJoysticks {
    private static final Logger LOGGER = LoggerFactory.getLogger(SdlJoysticks.class);
    public static final int MAX_JOYSTICKS = 16;
    // GLFW gamepad button order: A, B, X, Y, LB, RB, BACK, START, GUIDE, LTHUMB, RTHUMB, DPAD up/right/down/left
    private static final int[] GLFW_BUTTON_ORDER = {
        SDL_GAMEPAD_BUTTON_SOUTH,
        SDL_GAMEPAD_BUTTON_EAST,
        SDL_GAMEPAD_BUTTON_WEST,
        SDL_GAMEPAD_BUTTON_NORTH,
        SDL_GAMEPAD_BUTTON_LEFT_SHOULDER,
        SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER,
        SDL_GAMEPAD_BUTTON_BACK,
        SDL_GAMEPAD_BUTTON_START,
        SDL_GAMEPAD_BUTTON_GUIDE,
        SDL_GAMEPAD_BUTTON_LEFT_STICK,
        SDL_GAMEPAD_BUTTON_RIGHT_STICK,
        SDL_GAMEPAD_BUTTON_DPAD_UP,
        SDL_GAMEPAD_BUTTON_DPAD_RIGHT,
        SDL_GAMEPAD_BUTTON_DPAD_DOWN,
        SDL_GAMEPAD_BUTTON_DPAD_LEFT
    };
    private static boolean initialized;
    private static boolean available;
    private static int[] ids = new int[0];
    private static long lastRefreshNanos;
    private static final Map<Integer, Long> openJoysticks = new HashMap<>();
    private static final Map<Integer, Long> openGamepads = new HashMap<>();

    private SdlJoysticks() {
    }

    /** Minecraft only initialises SDL video; the joystick and gamepad subsystems are ours to start. */
    public static boolean init() {
        if (!initialized) {
            initialized = true;
            int flags = SDLInit.SDL_INIT_JOYSTICK | SDLInit.SDL_INIT_GAMEPAD;
            available = (SDLInit.SDL_WasInit(flags) & flags) == flags || SDLInit.SDL_InitSubSystem(flags);
            if (!available) {
                LOGGER.warn("SDL joystick/gamepad subsystem unavailable; controller input disabled");
            }

            refresh(true);
        }

        return available;
    }

    /** Re-reads the device list at most once a second (or immediately when forced); closes handles of unplugged devices. */
    public static void refresh(boolean force) {
        if (!available) {
            return;
        }

        long now = System.nanoTime();
        if (!force && now - lastRefreshNanos < 1_000_000_000L) {
            return;
        }

        lastRefreshNanos = now;
        IntBuffer list = SDL_GetJoysticks();
        int[] next;
        if (list == null) {
            next = new int[0];
        } else {
            next = new int[Math.min(list.remaining(), MAX_JOYSTICKS)];
            for (int i = 0; i < next.length; i++) {
                next[i] = list.get(list.position() + i);
            }

            SDLStdinc.SDL_free(list);
        }

        ids = next;
        openJoysticks.entrySet().removeIf(e -> {
            if (!contains(next, e.getKey())) {
                SDL_CloseJoystick(e.getValue());
                return true;
            }

            return false;
        });
        openGamepads.entrySet().removeIf(e -> {
            if (!contains(next, e.getKey())) {
                SDL_CloseGamepad(e.getValue());
                return true;
            }

            return false;
        });
    }

    private static boolean contains(int[] arr, int value) {
        for (int v : arr) {
            if (v == value) {
                return true;
            }
        }

        return false;
    }

    private static int idOf(int jid) {
        return available && jid >= 0 && jid < ids.length ? ids[jid] : 0;
    }

    public static boolean isPresent(int jid) {
        return idOf(jid) != 0;
    }

    public static String getName(int jid) {
        int id = idOf(jid);
        return id == 0 ? null : SDL_GetJoystickNameForID(id);
    }

    public static String getGuid(int jid) {
        int id = idOf(jid);
        if (id == 0) {
            return null;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_GUID guid = SDL_GetJoystickGUIDForID(id, SDL_GUID.malloc(stack));
            ByteBuffer out = stack.malloc(33);
            SDLGUID.SDL_GUIDToString(guid, out);
            return org.lwjgl.system.MemoryUtil.memASCII(out, out.capacity() - 1).trim().replace("\u0000", "");
        }
    }

    public static boolean isGamepad(int jid) {
        int id = idOf(jid);
        return id != 0 && SDL_IsGamepad(id);
    }

    private static long joystick(int jid) {
        int id = idOf(jid);
        if (id == 0) {
            return 0L;
        }

        return openJoysticks.computeIfAbsent(id, key -> SDL_OpenJoystick(key));
    }

    private static long gamepad(int jid) {
        int id = idOf(jid);
        if (id == 0 || !SDL_IsGamepad(id)) {
            return 0L;
        }

        return openGamepads.computeIfAbsent(id, key -> SDL_OpenGamepad(key));
    }

    /** Raw joystick axes in [-1, 1]; returns the number of axes written. */
    public static int readJoystickAxes(int jid, float[] out) {
        long handle = joystick(jid);
        if (handle == 0L) {
            return 0;
        }

        int count = Math.min(SDL_GetNumJoystickAxes(handle), out.length);
        for (int i = 0; i < count; i++) {
            out[i] = normalizeAxis(SDL_GetJoystickAxis(handle, i));
        }

        return Math.max(count, 0);
    }

    /** Raw joystick buttons; returns the number of buttons written. */
    public static int readJoystickButtons(int jid, boolean[] out) {
        long handle = joystick(jid);
        if (handle == 0L) {
            return 0;
        }

        int count = Math.min(SDL_GetNumJoystickButtons(handle), out.length);
        for (int i = 0; i < count; i++) {
            out[i] = SDL_GetJoystickButton(handle, i);
        }

        return Math.max(count, 0);
    }

    /** Gamepad axes in GLFW order (LX, LY, RX, RY, LT, RT), triggers mapped from [0, 1] to GLFW's [-1, 1]. */
    public static int readGamepadAxes(int jid, float[] out) {
        long handle = gamepad(jid);
        if (handle == 0L) {
            return 0;
        }

        int count = Math.min(6, out.length);
        for (int i = 0; i < count; i++) {
            float v = normalizeAxis(SDL_GetGamepadAxis(handle, i));
            out[i] = i >= 4 ? v * 2.0F - 1.0F : v;
        }

        return count;
    }

    /** Gamepad buttons in GLFW order. */
    public static int readGamepadButtons(int jid, boolean[] out) {
        long handle = gamepad(jid);
        if (handle == 0L) {
            return 0;
        }

        int count = Math.min(GLFW_BUTTON_ORDER.length, out.length);
        for (int i = 0; i < count; i++) {
            out[i] = SDL_GetGamepadButton(handle, GLFW_BUTTON_ORDER[i]);
        }

        return count;
    }

    private static float normalizeAxis(short raw) {
        return raw < 0 ? raw / 32768.0F : raw / 32767.0F;
    }
}
