package com.fpvdrone.data;

import com.fpvdrone.data.WeaponDamageRule;
import com.fpvdrone.data.IvHitInfo;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WeaponDamageRules {
   private static final Logger dOfFwvo = LoggerFactory.getLogger(WeaponDamageRules.class);
   public static final String v5suI = "ocp_762";
   public static final String ibzfxic = "iv_fuze";
   static final String wuhB = "weapon_damage";
   private static final Map<String, WeaponDamageRule> rsDd50tfV = new LinkedHashMap<>();

   private WeaponDamageRules() {
   }

   public static Optional<WeaponDamageRule> v5suI(IvHitInfo.KKeMvpuBgEY var0) {
      if (var0 == null) {
         return Optional.empty();
      }

      for (WeaponDamageRule var2 : rsDd50tfV.values()) {
         if (var2.v5suI(var0)) {
            return Optional.of(var2);
         }
      }

      return Optional.empty();
   }

   public static Map<String, WeaponDamageRule> v5suI() {
      return Collections.unmodifiableMap(rsDd50tfV);
   }

   public static void ibzfxic() {
      Map<String, JsonObject> var0 = DefinitionFiles.v5suI("weapon_damage");
      rsDd50tfV.clear();
      rsDd50tfV.put("ocp_762", wuhB());
      rsDd50tfV.put("iv_fuze", dOfFwvo());

      for (Entry<?, ?> var2 : var0.entrySet()) {
         DefinitionFiles.<WeaponDamageRule>v5suI(WeaponDamageRule.CODEC, (JsonObject)var2.getValue(), "weapon_damage", (String)var2.getKey())
            .ifPresent(var1 -> rsDd50tfV.put((String)var2.getKey(), var1.v5suI((String)var2.getKey())));
      }
   }

   private static WeaponDamageRule wuhB() {
      return new WeaponDamageRule("ocp_762", new WeaponDamageRule.Match(7.62F, false), "flat", 12.0F, 0.0F);
   }

   private static WeaponDamageRule dOfFwvo() {
      return new WeaponDamageRule("iv_fuze", new WeaponDamageRule.Match(0.0F, true), "velocity", 0.0F, 0.15F);
   }

   static {
      rsDd50tfV.put("ocp_762", wuhB());
      rsDd50tfV.put("iv_fuze", dOfFwvo());
   }
}
