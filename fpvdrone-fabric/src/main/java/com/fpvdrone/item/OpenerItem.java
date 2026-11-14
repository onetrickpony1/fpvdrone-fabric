package com.fpvdrone.item;

import com.fpvdrone.data.ArmorDefinition;
import com.fpvdrone.data.ArmorDefinitions;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.util.GeckoLibUtil;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 "Can Opener" mine vest: an iron-grade chestplate (armour values set in ModItems) defined by
 * data/fpvdrone/armor/opener.json. A TM-62 mine can be loaded into it; on a lethal hit it cooks off.
 * 1.1.8-PENDING: loading the mine (right-click with a TM-62) and the cook-off on a lethal hit (1.1.8 YlH0uqiuN9:
 *   detonate the vest's projectile at the wearer or his pilot body, explosion packet) wait for the explosions /
 *   entity work; the TM-62 drawn on the vest's payload_mount bone waits for the bedrock-model attachment renderer.
 */
public class OpenerItem extends UsageTooltipItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String armorId = "opener";

    public OpenerItem(Properties properties) {
        super(properties);
    }

    /** The vest's armour definition (model, texture, mount bone, accepted projectiles). Null before definitions load. */
    public @Nullable ArmorDefinition getDefinition() {
        return ArmorDefinitions.ibzfxic(this.armorId);
    }

    /** 1.1.8: lists the projectiles the vest takes as "Optional" item icons. */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return DroneTooltipIcons.forOpener(this);
    }

    /** Client only: GeckoLib asks for this once, on the physical client, to find the worn-vest renderer. */
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private com.fpvdrone.client.renderer.@Nullable OpenerArmorRenderer renderer;

            @Override
            public @Nullable GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                if (this.renderer == null) {
                    this.renderer = new com.fpvdrone.client.renderer.OpenerArmorRenderer();
                }

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
