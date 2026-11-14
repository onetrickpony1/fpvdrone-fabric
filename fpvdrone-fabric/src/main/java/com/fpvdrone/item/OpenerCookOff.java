package com.fpvdrone.item;

import com.fpvdrone.data.ArmorDefinition;
import com.fpvdrone.data.AttachableEntry;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.entity.PilotBodyEntity;
import com.fpvdrone.entity.PilotBodyManager;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.ProjectileBurstPacket;
import com.fpvdrone.network.compat.ServerHolder;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 (YlH0uqiuN9): the "Can Opener" vest's mine. A hit that would kill the wearer (a drone collision or the mod's
 * explosion) sets the vest's TM-62 off instead of the plain damage; the drop key sets it off on purpose. The mine goes
 * off at chest height of the wearer, or of his pilot body while he flies; the vest is used up, and a wearer standing in
 * person dies in the blast.
 */
public final class OpenerCookOff {
   private OpenerCookOff() {
   }

   /**
    * Called before a hit of {@code damage} (after armour) lands. True when the vest went off, in which case the caller
    * skips its own damage.
    */
   public static boolean onLethalHit(@Nullable LivingEntity target, float damage) {
      if (target == null || target.level().isClientSide()) {
         return false;
      }

      ItemStack chest = target.getItemBySlot(EquipmentSlot.CHEST);
      if (!(chest.getItem() instanceof OpenerItem opener)) {
         return false;
      }

      ArmorDefinition definition = opener.getDefinition();
      if (definition == null || !definition.c1jHYPb4()) {
         return false;
      }

      if (damage < target.getHealth()) {
         return false;
      }

      ServerPlayer wearer = wearerOf(target);
      return wearer != null && detonate(wearer);
   }

   /** 1.1.8 v5suI(ServerPlayer, float, DamageSource): a hit's damage after absorption, armour and protection. */
   public static float damageAfterArmour(ServerPlayer player, float damage, DamageSource source) {
      float remaining = damage;
      float absorbed = Math.min(player.getAbsorptionAmount(), remaining);
      remaining -= absorbed;
      float armour = player.getArmorValue();
      float toughness = (float)player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
      remaining = CombatRules.getDamageAfterAbsorb(player, remaining, source, armour, toughness);
      float protection = EnchantmentHelper.getDamageProtection(player.level(), player, source);
      if (protection > 0.0F) {
         remaining = CombatRules.getDamageAfterMagicAbsorb(remaining, protection);
      }

      return Math.max(remaining, 0.0F);
   }

   /** The player behind a hit: the player himself, or the owner of a pilot body (he is flying). */
   private static @Nullable ServerPlayer wearerOf(LivingEntity target) {
      if (target instanceof ServerPlayer player) {
         return player;
      }

      if (target instanceof PilotBodyEntity body) {
         UUID owner = body.getOwnerUUID();
         MinecraftServer server = ServerHolder.server();
         if (owner != null && server != null) {
            return server.getPlayerList().getPlayer(owner);
         }
      }

      return null;
   }

   /** Sets the worn vest's mine off (1.1.8 v5suI(ServerPlayer), also used by the drop key). True when it went off. */
   public static boolean detonate(@Nullable ServerPlayer player) {
      if (player == null) {
         return false;
      }

      ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
      if (!(chest.getItem() instanceof OpenerItem opener)) {
         return false;
      }

      ArmorDefinition definition = opener.getDefinition();
      Optional<AttachableEntry> mine = definition != null ? definition.ibzfxic() : Optional.empty();
      if (mine.isEmpty()) {
         return false;
      }

      String projectileId = mine.get().v5suI();
      ProjectileDefinition projectile = ProjectileDefinitions.getOrDefault(projectileId);
      ServerLevel level = player.level();
      PilotBodyEntity body = PilotBodyManager.getPilotBody(player.getUUID());
      boolean flying = body != null && body.isAlive();
      LivingEntity carrier = flying ? body : player;
      Vec3 pos = carrier.position().add(0.0, carrier.getBbHeight() * 0.72, 0.0);
      player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
      if (flying) {
         body.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
      }

      projectile.detonator()
         .detonate(level, pos, projectile.explosionPower(), projectile.fragmentCount(), player, true, null, projectile.stats(), projectile.id(), false);
      // 1.1.8 ModNetwork.v5suI(level, x, y, z, msg): to everyone within the server view distance (+1 chunk).
      // (master has ModNetwork.sendNearViewDistance for this; inlined here until the branches merge.)
      double range = (level.getServer().getPlayerList().getViewDistance() + 1) * 16.0;
      ModNetwork.sendNear(level, pos.x, pos.y, pos.z, range, new ProjectileBurstPacket(projectileId, pos));
      if (!flying) {
         DamageSource source = level.damageSources().explosion(null, null);
         if (!player.isInvulnerableTo(level, source) && !player.getAbilities().invulnerable) {
            player.setHealth(0.0F);
            player.die(source);
         }
      }

      return true;
   }
}
