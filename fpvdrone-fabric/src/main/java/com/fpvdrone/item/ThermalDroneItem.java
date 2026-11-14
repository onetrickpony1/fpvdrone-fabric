package com.fpvdrone.item;

import com.fpvdrone.entity.DroneEntity;
import net.minecraft.world.item.Item.Properties;

public class ThermalDroneItem extends DroneItem {
    public ThermalDroneItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void configureDrone(DroneEntity drone) {
        drone.setThermal(true);
    }

    /** 1.1.8: the thermal drone is a data file (data/fpvdrone/drones/thermal.json) on the normal drone entity. */
    @Override
    protected String droneType() {
        return "thermal";
    }
}
