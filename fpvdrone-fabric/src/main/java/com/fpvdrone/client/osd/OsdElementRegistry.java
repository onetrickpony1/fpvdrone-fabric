package com.fpvdrone.client.osd;

import com.fpvdrone.FPVClientMod;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.Transforms;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class OsdElementRegistry {
    private static final Map<String, OsdElementType> ELEMENTS = new LinkedHashMap<>();
    private static final Map<String, List<OsdElementType>> BY_CATEGORY = new LinkedHashMap<>();
    private static boolean liveDataBound = false;
    private static long armStartTimeMs = 0L;
    private static long armElapsedMs = 0L;
    private static boolean wasArmed = false;
    private static long sessionStartTimeMs = 0L;
    private static long sessionElapsedMs = 0L;
    private static boolean sessionActive = false;
    private static int logSessionNumber = 0;
    private static boolean logWasArmed = false;
    private static final String[] COMPASS_DIRS = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final char SYM_RSSI = '\ue001';
    private static final char SYM_AH_RIGHT = '\ue002';
    private static final char SYM_AH_LEFT = '\ue003';
    private static final char SYM_THR = '\ue004';
    private static final char SYM_OVER_HOME = '\ue005';
    private static final char SYM_VOLT = '\ue006';
    private static final char SYM_MAH = '\ue007';
    private static final char SYM_METRE = '\ue00c';
    private static final char SYM_TEMP_F = '\ue00d';
    private static final char SYM_TEMP_C = '\ue00e';
    private static final char SYM_FEET = '\ue00f';
    private static final char SYM_BBLOG = '\ue010';
    private static final char SYM_HOMEFLAG = '\ue011';
    private static final char SYM_AH_DECORATION = '\ue013';
    private static final char SYM_ROLL = '\ue014';
    private static final char SYM_PITCH = '\ue015';
    private static final char SYM_HEADING_N = '\ue018';
    private static final char SYM_HEADING_S = '\ue019';
    private static final char SYM_HEADING_E = '\ue01a';
    private static final char SYM_HEADING_W = '\ue01b';
    private static final char SYM_HEADING_DIVIDED = '\ue01c';
    private static final char SYM_HEADING_LINE = '\ue01d';
    private static final char SYM_GPS_SAT_L = '\ue01e';
    private static final char SYM_GPS_SAT_R = '\ue01f';
    private static final char SYM_BLANK = ' ';
    private static final char SYM_ARROW_SOUTH = '\ue060';
    private static final char SYM_ARROW_EAST = '\ue064';
    private static final char SYM_ARROW_NORTH = '\ue068';
    private static final char SYM_ARROW_WEST = '\ue06c';
    private static final char SYM_SPEED = '\ue070';
    private static final char SYM_DIST = '\ue071';
    private static final char SYM_AH_CENTER_LINE = '\ue072';
    private static final char SYM_AH_CENTER = '\ue073';
    private static final char SYM_AH_CENTER_LINE_R = '\ue074';
    private static final char SYM_ARROW_SMALL_UP = '\ue075';
    private static final char SYM_ARROW_SMALL_DOWN = '\ue076';
    private static final char SYM_ARROW_SMALL_RIGHT = '\ue077';
    private static final char SYM_WATT_HOUR = '\ue079';
    private static final char SYM_TEMPERATURE = '\ue07a';
    private static final char SYM_LQ = '\ue07b';
    private static final char SYM_ALTITUDE = '\ue07f';
    private static final char SYM_AH_BAR9_0 = '\ue080';
    private static final char SYM_GPS_LAT = '\ue089';
    private static final char SYM_PB_START = '\ue08a';
    private static final char SYM_PB_FULL = '\ue08b';
    private static final char SYM_PB_EMPTY = '\ue08d';
    private static final char SYM_PB_END = '\ue08e';
    private static final char SYM_BATT = '\ue090';
    private static final char SYM_BATT_5 = '\ue091';
    private static final char SYM_BATT_4 = '\ue092';
    private static final char SYM_BATT_3 = '\ue093';
    private static final char SYM_BATT_2 = '\ue094';
    private static final char SYM_BATT_1 = '\ue095';
    private static final char SYM_BATT_0 = '\ue096';
    private static final char SYM_GPS_LON = '\ue098';
    private static final char SYM_AMP = '\ue09a';
    private static final char SYM_ON_M = '\ue09b';
    private static final char SYM_FLY_M = '\ue09c';
    private static final char SYM_KPH = '\ue09e';
    private static final char SYM_MPS = '\ue09f';

    private OsdElementRegistry() {
    }

    public static void register(OsdElementType type) {
        if (!ELEMENTS.containsKey(type.getName())) {
            ELEMENTS.put(type.getName(), type);
            BY_CATEGORY.computeIfAbsent(type.getCategory(), k -> new ArrayList<>()).add(type);
        }
    }

    public static OsdElementType get(String name) {
        return ELEMENTS.get(name);
    }

    public static Collection<OsdElementType> getAll() {
        return Collections.unmodifiableCollection(ELEMENTS.values());
    }

    public static Map<String, List<OsdElementType>> getByCategory() {
        return Collections.unmodifiableMap(BY_CATEGORY);
    }

    public static void bindLiveData() {
        if (!liveDataBound) {
            liveDataBound = true;
            setTextRenderer("RSSI_VALUE", ctx -> {
                int rssiDbm = (int)(-25.0F - (1.0F - ctx.signal.getSignalStrength()) * 80.0F);
                return "\ue001" + Math.abs(rssiDbm);
            });
            setTextRenderer("LINK_QUALITY", ctx -> {
                int lq = (int)(ctx.signal.getSignalStrength() * 100.0F);
                return "\ue07b9:" + lq;
            });
            setTextRenderer("RSSI_DBM_VALUE", ctx -> {
                int rssiDbm = (int)(-25.0F - (1.0F - ctx.signal.getSignalStrength()) * 80.0F);
                return "\ue001" + rssiDbm;
            });
            setTextRenderer("MAIN_BATT_VOLTAGE", ctx -> {
                if (!ctx.battery.hasBattery()) {
                    return "\ue090-.--\ue006";
                } else {
                    char battSym = getBatterySymbol(ctx.battery.getBatteryPercent());
                    return battSym + String.format("%.1f", ctx.battery.getTotalVoltage()) + "\ue006";
                }
            });
            setTextRenderer("AVG_CELL_VOLTAGE", ctx -> {
                if (!ctx.battery.hasBattery()) {
                    return "\ue090-.--\ue006";
                } else {
                    char battSym = getBatterySymbol(ctx.battery.getBatteryPercent());
                    return battSym + String.format("%.2f", ctx.battery.getCellVoltage()) + "\ue006";
                }
            });
            setTextRenderer(
                "CURRENT_DRAW",
                ctx -> !ctx.battery.hasBattery() ? "\ue09a -.-\ue09a" : "\ue09a " + String.format("%.1f", ctx.battery.getCurrentAmps()) + "\ue09a"
            );
            setTextRenderer("MAH_DRAWN", ctx -> !ctx.battery.hasBattery() ? "\ue007---" : "\ue007" + ctx.battery.getUsedMah());
            setTextRenderer("BATT_REMAINING_PERCENT", ctx -> {
                if (!ctx.battery.hasBattery()) {
                    return "\ue090--%";
                } else {
                    char battSym = getBatterySymbol(ctx.battery.getBatteryPercent());
                    return "" + battSym + ctx.battery.getBatteryPercent() + "%";
                }
            });
            setTextRenderer("POWER", ctx -> {
                if (!ctx.battery.hasBattery()) {
                    return "W---W";
                } else {
                    float watts = ctx.battery.getCurrentAmps() * ctx.battery.getTotalVoltage();
                    return "W" + String.format("%.0f", watts) + "W";
                }
            });
            setTextRenderer("BATT_REMAINING_CAPACITY", ctx -> {
                if (!ctx.battery.hasBattery()) {
                    return "\ue007---";
                } else {
                    int remaining = ctx.battery.getCapacityMah() - ctx.battery.getUsedMah();
                    return "\ue007" + Math.max(0, remaining);
                }
            });
            setTextRenderer("OSD_EFFICIENCY", ctx -> {
                if (!ctx.battery.hasBattery()) {
                    return "---\ue007/\ue00c";
                } else {
                    float dist = (float)ctx.signal.getDistance();
                    if (dist < 1.0F) {
                        return "---\ue007/\ue00c";
                    } else {
                        int efficiency = (int)(ctx.battery.getUsedMah() / dist);
                        return efficiency + "\ue007/\ue00c";
                    }
                }
            });
            setTextRenderer("ALTITUDE", ctx -> "\ue07f" + String.format("%.1f", ctx.player.getY()) + "\ue00c");
            setTextRenderer("GPS_SPEED", ctx -> {
                if (ctx.physics == null) {
                    return "\ue070 0\ue09e";
                } else {
                    float speed = ctx.physics.getVelocity().length() * 3.6F;
                    return "\ue070 " + String.format("%.0f", speed) + "\ue09e";
                }
            });
            setTextRenderer("NUMERICAL_HEADING", ctx -> {
                float yaw = getYaw(ctx);
                return "\ue068" + String.format("%03.0f", yaw);
            });
            // Betaflight shows RESC during GPS Rescue (port addition); otherwise ACRO as in 1.1.8.
            setTextRenderer("FLYMODE", ctx -> {
                net.minecraft.client.Minecraft rescueMc = net.minecraft.client.Minecraft.getInstance();
                com.fpvdrone.physics.PhysicsState rescuePhysics = rescueMc.player != null ? com.fpvdrone.FPVMod.getPhysicsState(rescueMc.player.getUUID()) : null;
                return rescuePhysics != null && rescuePhysics.isGpsRescue() ? "RESC" : "ACRO";
            });
            setTextRenderer("THROTTLE_POSITION", ctx -> {
                if (ctx.physics != null && ctx.physics.isThrottleCut()) {
                    return "\ue004 0";
                } else {
                    float throttle = (ControllerReader.getThrottle() + 1.0F) / 2.0F * 100.0F;
                    return "\ue004 " + String.format("%.0f", throttle);
                }
            });
            setTextRenderer("FLIGHT_DIST", ctx -> "\ue071" + String.format("%d", (int)ctx.signal.getDistance()) + "\ue00c");
            setTextRenderer("NUMERICAL_VARIO", ctx -> {
                if (ctx.physics == null) {
                    return "\ue0750.0\ue09f";
                } else {
                    float vy = ctx.physics.getVelocity().y;
                    char arrow = (char)(vy >= 0.0F ? '\ue075' : '\ue076');
                    return arrow + String.format("%.1f", Math.abs(vy)) + "\ue09f";
                }
            });
            setTextRenderer("PITCH_ANGLE", ctx -> {
                if (ctx.physics == null) {
                    return "\ue015  0.0";
                } else {
                    Vector3f look = ctx.physics.getDroneLook();
                    Vector3f up = ctx.physics.getDroneUp();
                    float[] angles = Transforms.getWorldEulerAngles(look, up);
                    return "\ue015" + String.format("%5.1f", angles[1]);
                }
            });
            setTextRenderer("ROLL_ANGLE", ctx -> {
                if (ctx.physics == null) {
                    return "\ue014  0.0";
                } else {
                    Vector3f look = ctx.physics.getDroneLook();
                    Vector3f up = ctx.physics.getDroneUp();
                    float[] angles = Transforms.getWorldEulerAngles(look, up);
                    return "\ue014" + String.format("%5.1f", angles[2]);
                }
            });
            setTextRenderer("GPS_SATS", ctx -> "\ue01e\ue01f14");
            setTextRenderer("HOME_DIST", ctx -> {
                if (ctx.drone == null) {
                    return "\ue011---\ue00c";
                } else {
                    Vec3 home = FPVClientMod.getPilotBodyPosition(ctx.player.getUUID());
                    if (home == null) {
                        return "\ue011---\ue00c";
                    } else {
                        double dx = ctx.drone.getX() - home.x;
                        double dz = ctx.drone.getZ() - home.z;
                        int dist = (int)Math.sqrt(dx * dx + dz * dz);
                        return "\ue011" + dist + "\ue00c";
                    }
                }
            });
            setTextRenderer("HOME_DIR", ctx -> {
                if (ctx.drone != null && ctx.physics != null) {
                    Vec3 home = FPVClientMod.getPilotBodyPosition(ctx.player.getUUID());
                    if (home == null) {
                        return "\ue068";
                    } else {
                        double dx = home.x - ctx.drone.getX();
                        double dz = home.z - ctx.drone.getZ();
                        float bearingToHome = (float)Math.toDegrees(Math.atan2(-dx, dz));
                        if (bearingToHome < 0.0F) {
                            bearingToHome += 360.0F;
                        }

                        float droneYaw = getYaw(ctx);
                        float relative = (bearingToHome - droneYaw + 360.0F) % 360.0F;
                        relative = (360.0F - relative) % 360.0F;
                        int idx = ((int)((relative + 11.25F) / 22.5F) + 8) % 16;
                        return (char)(57440 + idx) + "";
                    }
                } else {
                    return "\ue068";
                }
            });
            setTextRenderer("CRAFT_NAME", ctx -> ctx.drone != null ? ctx.drone.getConfig().getDroneName() : "CRAFT");
            setTextRenderer("PILOT_NAME", ctx -> {
                if (ctx.drone != null) {
                    OsdElement elem = ctx.drone.getOsdLayout() != null ? ctx.drone.getOsdLayout().getElement("PILOT_NAME") : null;
                    if (elem != null && elem.getCustomText() != null && !elem.getCustomText().isEmpty()) {
                        return elem.getCustomText();
                    }
                }

                return ctx.player != null ? ctx.player.getGameProfile().name().toUpperCase() : "PILOT";
            });
            setTextRenderer("WARNINGS", ctx -> {
                if (BootStateManager.getInstance().getPhase() == BootStateManager.Phase.TRANSITION) {
                    long transitionMs = BootStateManager.getInstance().getTransitionElapsedMs();
                    if (transitionMs < 1000L) {
                        return "CALIBRATING";
                    }
                }

                // Betaflight shows CRASH FLIP while turtle mode is on.
                net.minecraft.client.Minecraft turtleMc = net.minecraft.client.Minecraft.getInstance();
                com.fpvdrone.physics.PhysicsState turtlePhysics = turtleMc.player != null ? com.fpvdrone.FPVMod.getPhysicsState(turtleMc.player.getUUID()) : null;
                if (turtlePhysics != null && turtlePhysics.isTurtleMode()) {
                    return blinkText("CRASH FLIP");
                }

                if (ctx.signal.getSignalStrength() < 0.15F) {
                    return blinkText("RSSI LOW");
                } else if (ctx.signal.getSignalStrength() < 0.3F) {
                    return blinkText("LINK QUALITY");
                } else if (ctx.battery.hasBattery() && ctx.battery.getBatteryPercent() <= 10) {
                    return blinkText("LAND NOW");
                } else if (ctx.battery.hasBattery() && ctx.battery.getBatteryPercent() <= 20) {
                    return blinkText("LOW BATTERY");
                } else {
                    return ctx.battery.hasBattery() && ctx.battery.getBatteryPercent() <= 0 ? blinkText("OVER CAP") : "";
                }
            });
            setTextRenderer("DISARMED", ctx -> {
                if (ctx.drone == null) {
                    return "DISARMED";
                } else if (!ctx.drone.areMotorsArmed()) {
                    return "DISARMED";
                } else {
                    long armedDuration = armElapsedMs;
                    return armedDuration < 3000L ? blinkText("ARMED") : "";
                }
            });
            setTextRenderer("TIMER_1", ctx -> {
                boolean armed = ctx.drone != null && ctx.drone.areMotorsArmed();
                if (armed && !wasArmed) {
                    armStartTimeMs = System.currentTimeMillis();
                }

                if (armed) {
                    armElapsedMs = System.currentTimeMillis() - armStartTimeMs;
                }

                wasArmed = armed;
                int totalSec = (int)(armElapsedMs / 1000L);
                int min = totalSec / 60;
                int sec = totalSec % 60;
                return "\ue09c" + String.format("%02d:%02d", min, sec);
            });
            setTextRenderer("TIMER_2", ctx -> {
                boolean inFpv = ctx.drone != null;
                if (inFpv && !sessionActive) {
                    sessionStartTimeMs = System.currentTimeMillis();
                    sessionActive = true;
                }

                if (!inFpv) {
                    sessionActive = false;
                }

                if (sessionActive) {
                    sessionElapsedMs = System.currentTimeMillis() - sessionStartTimeMs;
                }

                int totalSec = (int)(sessionElapsedMs / 1000L);
                int min = totalSec / 60;
                int sec = totalSec % 60;
                return "\ue09b" + String.format("%02d:%02d", min, sec);
            });
            setTextRenderer("REMAINING_TIME_ESTIMATE", ctx -> {
                if (!ctx.battery.hasBattery()) {
                    return "--:--";
                } else {
                    float amps = ctx.battery.getCurrentAmps();
                    if (amps < 0.5F) {
                        return "--:--";
                    } else {
                        int remainingMah = Math.max(0, ctx.battery.getCapacityMah() - ctx.battery.getUsedMah());
                        float hoursLeft = remainingMah / 1000.0F / amps;
                        int totalSeconds = (int)(hoursLeft * 3600.0F);
                        if (totalSeconds < 0) {
                            totalSeconds = 0;
                        }

                        int min = totalSeconds / 60;
                        int sec = totalSeconds % 60;
                        return String.format("%02d:%02d", min, sec);
                    }
                }
            });
            setTextRenderer("RTC_DATE_TIME", ctx -> {
                LocalDateTime now = LocalDateTime.now();
                return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            });
            setTextRenderer("LOG_STATUS", ctx -> {
                boolean armed = ctx.drone != null && ctx.drone.areMotorsArmed();
                if (armed && !logWasArmed) {
                    logSessionNumber++;
                }

                logWasArmed = armed;
                return "\ue010 " + logSessionNumber;
            });
            setTextRenderer("CROSSHAIRS", ctx -> "\ue072\ue073\ue074");
            setTextRenderer("VTX_CHANNEL", ctx -> ctx.drone != null ? "R:" + ctx.drone.getVideoChannel() + ":200" : "R:1:200");
        }
    }

    private static void setTextRenderer(String name, Function<OsdRenderContext, String> renderer) {
        OsdElementType type = ELEMENTS.get(name);
        if (type != null) {
            type.setTextRenderer(renderer);
        }
    }

    private static String blinkText(String text) {
        return System.currentTimeMillis() / 250L % 2L == 0L ? text : "";
    }

    private static char getBatterySymbol(int percent) {
        if (percent >= 83) {
            return '\ue090';
        } else if (percent >= 67) {
            return '\ue091';
        } else if (percent >= 50) {
            return '\ue092';
        } else if (percent >= 33) {
            return '\ue093';
        } else if (percent >= 17) {
            return '\ue094';
        } else {
            return (char)(percent > 0 ? '\ue095' : '\ue096');
        }
    }

    public static float getYaw(OsdRenderContext ctx) {
        if (ctx.physics == null) {
            return 0.0F;
        } else {
            Vector3f look = ctx.physics.getDroneLook();
            float yaw = (float)Math.toDegrees(Math.atan2(-look.x, look.z));
            if (yaw < 0.0F) {
                yaw += 360.0F;
            }

            return yaw;
        }
    }

    public static String getCompassDirection(float yaw) {
        int idx = (int)((yaw + 22.5F) / 45.0F) % 8;
        return COMPASS_DIRS[idx];
    }

    private static String buildHorizonPreview() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (row == 4) {
                    sb.append('\ue084');
                } else {
                    sb.append(' ');
                }
            }
        }

        return sb.toString();
    }

    private static String buildHorizonSidebarsPreview() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < 7; row++) {
            sb.append((char)(row == 3 ? '\ue003' : '\ue013'));

            for (int col = 0; col < 9; col++) {
                sb.append(' ');
            }

            sb.append((char)(row == 3 ? '\ue002' : '\ue013'));
        }

        return sb.toString();
    }

    private static void registerAll() {
        register(new OsdElementType("RSSI_VALUE", "RSSI Value", "Signal", "\ue00199", 27.0F, 0.0F, 3, 1, true, false));
        register(new OsdElementType("LINK_QUALITY", "Link Quality", "Signal", "\ue07b9:100", 21.0F, 0.0F, 6, 1, true, false));
        register(new OsdElementType("RSSI_DBM_VALUE", "RSSI dBm", "Signal", "\ue001-80", 23.0F, 1.0F, 4, 1, true, false));
        register(new OsdElementType("MAIN_BATT_VOLTAGE", "Main Batt Voltage", "Battery", "\ue09016.8\ue006", 12.0F, 2.0F, 6, 1, true, false));
        register(new OsdElementType("AVG_CELL_VOLTAGE", "Avg Cell Voltage", "Battery", "\ue0904.20\ue006", 12.0F, 1.0F, 6, 1, true, false));
        register(new OsdElementType("CURRENT_DRAW", "Current Draw", "Battery", "\ue09a 42.5\ue09a", 1.0F, 13.0F, 7, 1, true, false));
        register(new OsdElementType("MAH_DRAWN", "mAh Drawn", "Battery", "\ue0071245", 1.0F, 12.0F, 5, 1, true, false));
        register(new OsdElementType("BATT_REMAINING_PERCENT", "Batt %", "Battery", "\ue09072%", 12.0F, 3.0F, 4, 1, true, false));
        register(new OsdElementType("POWER", "Power", "Battery", "W125W", 23.0F, 14.0F, 4, 1, true, false));
        register(new OsdElementType("BATT_REMAINING_CAPACITY", "Batt Remaining", "Battery", "\ue007955", 1.0F, 11.0F, 4, 1, true, false));
        register(new OsdElementType("OSD_EFFICIENCY", "Efficiency", "Battery", "123\ue007/\ue00c", 1.0F, 14.0F, 6, 1, true, false));
        register(
            new OsdElementType(
                "MAIN_BATT_USAGE",
                "Batt Usage",
                "Battery",
                "\ue08a\ue08b\ue08b\ue08b\ue08b\ue08b\ue08b\ue08b\ue08d\ue08d\ue08d\ue08e",
                8.0F,
                14.0F,
                12,
                1,
                false,
                false
            )
        );
        register(new OsdElementType("ALTITUDE", "Altitude", "Flight", "\ue07f399.7\ue00c", 23.0F, 12.0F, 7, 1, true, false));
        register(new OsdElementType("GPS_SPEED", "GPS Speed", "Flight", "\ue070 42\ue09e", 24.0F, 10.0F, 5, 1, true, false));
        register(
            new OsdElementType("COMPASS_BAR", "Compass Bar", "Flight", "\ue01b\ue01d\ue01c\ue01d\ue018\ue01d\ue01c\ue01d\ue01a", 11.0F, 0.0F, 9, 1, true, true)
        );
        register(new OsdElementType("NUMERICAL_HEADING", "Heading", "Flight", "\ue068045", 14.0F, 1.0F, 4, 1, true, false));
        register(new OsdElementType("FLYMODE", "Fly Mode", "Flight", "ACRO", 13.0F, 11.0F, 4, 1, false, false));
        register(new OsdElementType("THROTTLE_POSITION", "Throttle", "Flight", "\ue004 69", 1.0F, 4.0F, 4, 1, true, false));
        register(new OsdElementType("FLIGHT_DIST", "Flight Dist", "Flight", "\ue071653\ue00c", 1.0F, 9.0F, 5, 1, true, false));
        register(new OsdElementType("NUMERICAL_VARIO", "Vario", "Flight", "\ue0758.7\ue09f", 26.0F, 10.0F, 5, 1, true, false));
        register(new OsdElementType("G_FORCE", "G-Force", "Flight", "1.0G", 13.0F, 10.0F, 4, 1, false, false));
        register(new OsdElementType("FLIP_ARROW", "Flip Arrow", "Flight", "\ue064", 15.0F, 8.0F, 1, 1, false, false));
        register(new OsdElementType("CROSSHAIRS", "Crosshairs", "Attitude", "\ue072\ue073\ue074", 14.0F, 7.0F, 3, 1, false, true));
        register(new OsdElementType("ARTIFICIAL_HORIZON", "Artificial Horizon", "Attitude", buildHorizonPreview(), 11.0F, 4.0F, 9, 9, true, true));
        register(new OsdElementType("HORIZON_SIDEBARS", "Horizon Sidebars", "Attitude", buildHorizonSidebarsPreview(), 10.0F, 5.0F, 11, 7, true, true));
        register(new OsdElementType("PITCH_ANGLE", "Pitch Angle", "Attitude", "\ue015 -12.5", 24.0F, 8.0F, 7, 1, true, false));
        register(new OsdElementType("ROLL_ANGLE", "Roll Angle", "Attitude", "\ue014  5.2", 24.0F, 9.0F, 6, 1, true, false));
        register(new OsdElementType("GPS_SATS", "GPS Sats", "GPS", "\ue01e\ue01f14", 24.0F, 2.0F, 4, 1, true, false));
        register(new OsdElementType("GPS_LAT", "GPS Lat", "GPS", "\ue089-55.7520000", 1.0F, 14.0F, 12, 1, false, false));
        register(new OsdElementType("GPS_LON", "GPS Lon", "GPS", "\ue098 037.6175000", 1.0F, 15.0F, 13, 1, false, false));
        register(new OsdElementType("HOME_DIR", "Home Direction", "GPS", "\ue068", 14.0F, 3.0F, 1, 1, false, false));
        register(new OsdElementType("HOME_DIST", "Home Distance", "GPS", "\ue011120\ue00c", 23.0F, 13.0F, 5, 1, true, false));
        register(new OsdElementType("CRAFT_NAME", "Craft Name", "Info", "CRAFT", 10.0F, 12.0F, 10, 1, true, false));
        register(new OsdElementType("PILOT_NAME", "Pilot Name", "Info", "PILOT", 10.0F, 13.0F, 10, 1, true, false));
        register(new OsdElementType("WARNINGS", "Warnings", "Warnings", "    WARNINGS    ", 11.0F, 7.0F, 16, 1, true, false));
        register(new OsdElementType("DISARMED", "Disarmed", "Warnings", "DISARMED", 11.0F, 8.0F, 8, 1, true, false));
        register(new OsdElementType("READY_MODE", "Ready Mode", "Warnings", "READY", 13.0F, 8.0F, 5, 1, false, false));
        register(new OsdElementType("TIMER_1", "Timer 1", "Timers", "\ue09c04:32", 1.0F, 0.0F, 6, 1, true, false));
        register(new OsdElementType("TIMER_2", "Timer 2", "Timers", "\ue09b04:32", 1.0F, 1.0F, 6, 1, true, false));
        register(new OsdElementType("REMAINING_TIME_ESTIMATE", "Remaining Time", "Timers", "01:13", 24.0F, 13.0F, 5, 1, true, false));
        register(new OsdElementType("RTC_DATE_TIME", "RTC Date/Time", "Timers", "2025-10-25 12:00:00", 20.0F, 15.0F, 19, 1, true, false));
        register(new OsdElementType("VTX_CHANNEL", "VTX Channel", "System", "R:2:200", 8.0F, 0.0F, 7, 1, false, false));
        register(new OsdElementType("LOG_STATUS", "Log Status", "System", "\ue010 16", 27.0F, 2.0F, 4, 1, true, false));
        register(new OsdElementType("PID_RATE_PROFILE", "PID/Rate Profile", "Profile", "1-1", 1.0F, 2.0F, 3, 1, false, false));
        register(new OsdElementType("ESC_TEMPERATURE", "ESC Temp", "Hardware", "\ue07a45\ue00e", 24.0F, 11.0F, 4, 1, false, false));
    }

    static {
        registerAll();
    }
}
