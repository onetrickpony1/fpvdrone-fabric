package com.fpvdrone.item;

/** 1.1.8 DJI Mavic 3 Pro: the normal drone entity with the "mavic" definition (GPS flight, gimbal, bomb bay). */
public class MavicDroneItem extends DroneItem {
    public MavicDroneItem(Properties properties) {
        super(properties);
    }

    @Override
    protected String droneType() {
        return "mavic";
    }
}
