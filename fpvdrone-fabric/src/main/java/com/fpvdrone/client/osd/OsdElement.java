package com.fpvdrone.client.osd;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;

public class OsdElement {
    private final String name;
    private float x;
    private float y;
    private boolean visible;
    private String customText;

    public OsdElement(String name, float x, float y, boolean visible) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.visible = visible;
        this.customText = null;
    }

    public OsdElement(String name, float x, float y, boolean visible, String customText) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.visible = visible;
        this.customText = customText;
    }

    public String getName() {
        return this.name;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public String getCustomText() {
        return this.customText;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setCustomText(String customText) {
        this.customText = customText;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", this.name);
        tag.putFloat("x", this.x);
        tag.putFloat("y", this.y);
        tag.putBoolean("visible", this.visible);
        if (this.customText != null) {
            tag.putString("customText", this.customText);
        }

        return tag;
    }

    public static OsdElement fromNbt(CompoundTag tag) {
        OsdElement elem = new OsdElement(tag.getStringOr("name", ""), tag.getFloatOr("x", 0.0F), tag.getFloatOr("y", 0.0F), tag.getBooleanOr("visible", false));
        if (tag.contains("customText")) {
            elem.setCustomText(tag.getStringOr("customText", ""));
        }

        return elem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else {
            return !(o instanceof OsdElement that)
                ? false
                : Float.compare(that.x, this.x) == 0
                    && Float.compare(that.y, this.y) == 0
                    && this.visible == that.visible
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.customText, that.customText);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.x, this.y, this.visible, this.customText);
    }

    @Override
    public String toString() {
        return "OsdElement{"
            + this.name
            + " ("
            + this.x
            + ","
            + this.y
            + ") visible="
            + this.visible
            + (this.customText != null ? " text=" + this.customText : "")
            + "}";
    }
}
