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
}
