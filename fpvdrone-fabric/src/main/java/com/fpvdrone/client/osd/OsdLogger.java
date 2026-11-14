package com.fpvdrone.client.osd;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class OsdLogger {
    private static final Logger LOGGER = LogUtils.getLogger();

    private OsdLogger() {
    }

    public static void layoutDebug(String msg, Object... args) {
        LOGGER.debug("[OSD:Layout] " + msg, args);
    }

    public static void layoutInfo(String msg, Object... args) {
        LOGGER.info("[OSD:Layout] " + msg, args);
    }

    public static void layoutWarn(String msg, Object... args) {
        LOGGER.warn("[OSD:Layout] " + msg, args);
    }

    public static void layoutError(String msg, Object... args) {
        LOGGER.error("[OSD:Layout] " + msg, args);
    }

    public static void registryDebug(String msg, Object... args) {
        LOGGER.debug("[OSD:Registry] " + msg, args);
    }

    public static void registryInfo(String msg, Object... args) {
        LOGGER.info("[OSD:Registry] " + msg, args);
    }

    public static void registryWarn(String msg, Object... args) {
        LOGGER.warn("[OSD:Registry] " + msg, args);
    }

    public static void registryError(String msg, Object... args) {
        LOGGER.error("[OSD:Registry] " + msg, args);
    }

    public static void builderDebug(String msg, Object... args) {
        LOGGER.debug("[OSD:Builder] " + msg, args);
    }

    public static void builderInfo(String msg, Object... args) {
        LOGGER.info("[OSD:Builder] " + msg, args);
    }

    public static void builderWarn(String msg, Object... args) {
        LOGGER.warn("[OSD:Builder] " + msg, args);
    }

    public static void renderWarn(String msg, Object... args) {
        LOGGER.warn("[OSD:Render] " + msg, args);
    }

    public static void renderError(String msg, Object... args) {
        LOGGER.error("[OSD:Render] " + msg, args);
    }

    public static void networkInfo(String msg, Object... args) {
        LOGGER.info("[OSD:Network] " + msg, args);
    }

    public static void networkWarn(String msg, Object... args) {
        LOGGER.warn("[OSD:Network] " + msg, args);
    }

    public static void networkError(String msg, Object... args) {
        LOGGER.error("[OSD:Network] " + msg, args);
    }

    public static void fontInfo(String msg, Object... args) {
        LOGGER.info("[OSD:Font] " + msg, args);
    }

    public static void fontError(String msg, Object... args) {
        LOGGER.error("[OSD:Font] " + msg, args);
    }
}
