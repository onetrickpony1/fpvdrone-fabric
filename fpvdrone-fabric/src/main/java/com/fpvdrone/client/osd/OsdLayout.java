package com.fpvdrone.client.osd;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class OsdLayout {
    public static final int GRID_COLS = 30;
    public static final int GRID_ROWS = 16;
    private final Map<String, OsdElement> elements;

    public OsdLayout() {
        this.elements = new LinkedHashMap<>();
    }

    private OsdLayout(Map<String, OsdElement> elements) {
        this.elements = new LinkedHashMap<>(elements);
    }

    public OsdElement getElement(String name) {
        return this.elements.get(name);
    }

    public Collection<OsdElement> getVisibleElements() {
        return this.elements.values().stream().filter(OsdElement::isVisible).collect(Collectors.toList());
    }

    public void setElement(String name, OsdElement element) {
        this.elements.put(name, element);
    }

    public Collection<OsdElement> getAllElements() {
        return Collections.unmodifiableCollection(this.elements.values());
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("gridCols", 30);
        tag.putInt("gridRows", 16);
        ListTag elementList = new ListTag();

        for (OsdElement elem : this.elements.values()) {
            elementList.add(elem.toNbt());
        }

        tag.put("elements", elementList);
        return tag;
    }

    public static OsdLayout fromNbt(CompoundTag tag) {
        if (tag != null && !tag.isEmpty()) {
            OsdLayout layout = new OsdLayout();
            ListTag list = tag.getListOrEmpty("elements");

            for (int i = 0; i < list.size(); i++) {
                CompoundTag elemTag = list.getCompoundOrEmpty(i);
                String name = elemTag.getStringOr("name", "");
                if (OsdElementRegistry.get(name) != null) {
                    OsdElement elem = OsdElement.fromNbt(elemTag);
                    OsdElementType type = OsdElementRegistry.get(name);
                    float maxX = 30 - type.getWidthChars();
                    float maxY = 16 - type.getHeightLines();
                    float clampedX = Math.max(0.0F, Math.min(maxX, elem.getX()));
                    float clampedY = Math.max(0.0F, Math.min(maxY, elem.getY()));
                    if (clampedX != elem.getX() || clampedY != elem.getY()) {
                        elem.setX(clampedX);
                        elem.setY(clampedY);
                    }

                    layout.elements.put(name, elem);
                }
            }

            return layout;
        } else {
            return defaults();
        }
    }

    public static OsdLayout defaults() {
        OsdLayout layout = new OsdLayout();

        for (OsdElementType type : OsdElementRegistry.getAll()) {
            boolean visible = isDefaultVisible(type.getName());
            float[] pos = getDefaultPosition(type.getName());
            float x = pos != null ? pos[0] : type.getDefaultX();
            float y = pos != null ? pos[1] : type.getDefaultY();
            layout.elements.put(type.getName(), new OsdElement(type.getName(), x, y, visible));
        }

        return layout;
    }

    private static boolean isDefaultVisible(String name) {
        return switch (name) {
            case "RSSI_VALUE", "LINK_QUALITY", "MAIN_BATT_VOLTAGE", "AVG_CELL_VOLTAGE", "CURRENT_DRAW", "MAH_DRAWN", "COMPASS_BAR", "FLYMODE", "THROTTLE_POSITION", "CROSSHAIRS", "ARTIFICIAL_HORIZON", "WARNINGS", "DISARMED", "TIMER_1", "CRAFT_NAME", "PILOT_NAME" -> true;
            default -> false;
        };
    }

    private static float[] getDefaultPosition(String name) {
        return switch (name) {
            case "RSSI_VALUE" -> new float[]{23.0F, 2.0F};
            case "LINK_QUALITY" -> new float[]{23.0F, 3.0F};
            case "RSSI_DBM_VALUE" -> new float[]{23.0F, 1.0F};
            case "MAIN_BATT_VOLTAGE" -> new float[]{1.0F, 11.0F};
            case "AVG_CELL_VOLTAGE" -> new float[]{1.0F, 12.0F};
            case "CURRENT_DRAW" -> new float[]{1.0F, 10.0F};
            case "MAH_DRAWN" -> new float[]{1.0F, 9.0F};
            case "BATT_REMAINING_PERCENT" -> new float[]{12.0F, 3.0F};
            case "POWER" -> new float[]{23.0F, 14.0F};
            case "BATT_REMAINING_CAPACITY" -> new float[]{1.0F, 11.0F};
            case "OSD_EFFICIENCY" -> new float[]{1.0F, 14.0F};
            case "MAIN_BATT_USAGE" -> new float[]{8.0F, 14.0F};
            case "ALTITUDE" -> new float[]{23.0F, 12.0F};
            case "GPS_SPEED" -> new float[]{24.0F, 10.0F};
            case "COMPASS_BAR" -> new float[]{11.0F, 2.0F};
            case "NUMERICAL_HEADING" -> new float[]{14.0F, 1.0F};
            case "FLYMODE" -> new float[]{13.5F, 11.0F};
            case "THROTTLE_POSITION" -> new float[]{1.0F, 7.0F};
            case "FLIGHT_DIST" -> new float[]{1.0F, 9.0F};
            case "NUMERICAL_VARIO" -> new float[]{25.0F, 10.0F};
            case "G_FORCE" -> new float[]{13.0F, 10.0F};
            case "FLIP_ARROW" -> new float[]{15.0F, 8.0F};
            case "CROSSHAIRS" -> new float[]{14.0F, 7.0F};
            case "ARTIFICIAL_HORIZON" -> new float[]{11.0F, 4.0F};
            case "HORIZON_SIDEBARS" -> new float[]{10.0F, 5.0F};
            case "PITCH_ANGLE" -> new float[]{23.0F, 8.0F};
            case "ROLL_ANGLE" -> new float[]{24.0F, 9.0F};
            case "GPS_SATS" -> new float[]{24.0F, 2.0F};
            case "GPS_LAT" -> new float[]{1.0F, 14.0F};
            case "GPS_LON" -> new float[]{1.0F, 15.0F};
            case "HOME_DIR" -> new float[]{23.0F, 11.0F};
            case "HOME_DIST" -> new float[]{23.0F, 12.0F};
            case "CRAFT_NAME" -> new float[]{7.0F, 12.0F};
            case "PILOT_NAME" -> new float[]{10.5F, 3.0F};
            case "WARNINGS" -> new float[]{7.5F, 8.0F};
            case "DISARMED" -> new float[]{11.5F, 6.0F};
            case "READY_MODE" -> new float[]{13.0F, 8.0F};
            case "TIMER_1" -> new float[]{1.0F, 3.0F};
            case "TIMER_2" -> new float[]{1.0F, 4.0F};
            case "REMAINING_TIME_ESTIMATE" -> new float[]{23.5F, 13.0F};
            case "RTC_DATE_TIME" -> new float[]{11.0F, 15.0F};
            case "VTX_CHANNEL" -> new float[]{8.0F, 0.0F};
            case "LOG_STATUS" -> new float[]{23.0F, 4.0F};
            case "PID_RATE_PROFILE" -> new float[]{1.0F, 2.0F};
            case "ESC_TEMPERATURE" -> new float[]{24.0F, 11.0F};
            default -> null;
        };
    }

    public static float snapToHalf(float value) {
        return Math.round(value * 2.0F) / 2.0F;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return o instanceof OsdLayout that ? Objects.equals(this.elements, that.elements) : false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.elements);
    }
}
