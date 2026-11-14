package com.fpvdrone.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class BombItem extends Item {
    private final int tier;

    public BombItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }
}
