package com.fpvdrone.client.thermal;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1.1.8 FallbackManager: how the thermal picture is made.
 * <ul>
 *   <li>{@link Mode#VANILLA_ATLAS}: vanilla draws the chunks; their shader and atlas are swapped for thermal ones.</li>
 *   <li>{@link Mode#SCREENSPACE_FALLBACK}: another mod draws the chunks (Sodium) or the shaders (Iris); the normal
 *   picture is turned into a temperature picture afterwards from its brightness and depth.</li>
 * </ul>
 * 1.1.8 also had an Embeddium mode (its own chunk-shader hooks). Embeddium does not exist on Fabric 26.3, and 1.1.8
 * itself chose the fallback for Sodium, Rubidium, Oculus and Iris, so this port does the same for Sodium and Iris.
 */
public final class FallbackManager {
   private static final Logger LOGGER = LoggerFactory.getLogger(FallbackManager.class);
   private static Mode mode;

   private FallbackManager() {
   }

   public static Mode getMode() {
      if (mode == null) {
         FabricLoader loader = FabricLoader.getInstance();
         if (loader.isModLoaded("iris") || loader.isModLoaded("oculus") || loader.isModLoaded("sodium") || loader.isModLoaded("rubidium")) {
            mode = Mode.SCREENSPACE_FALLBACK;
         } else {
            mode = Mode.VANILLA_ATLAS;
         }
         LOGGER.info("[thermal] render mode {}", mode);
      }
      return mode;
   }

   /** The temperature picture comes from the normally drawn scene. */
   public static boolean isScreenspace() {
      return getMode() == Mode.SCREENSPACE_FALLBACK;
   }

   /** Vanilla chunks draw with the thermal shaders and atlas. */
   public static boolean isVanillaAtlas() {
      return getMode() == Mode.VANILLA_ATLAS;
   }

   /** 1.1.8 v5suI(String): drop to the fallback after a failure (bad atlas, shader that does not compile). */
   public static void forceFallback(String reason) {
      if (getMode() != Mode.SCREENSPACE_FALLBACK) {
         LOGGER.warn("[thermal] switching to the screen-space fallback: {}", reason);
         mode = Mode.SCREENSPACE_FALLBACK;
      }
   }

   public enum Mode {
      VANILLA_ATLAS,
      SCREENSPACE_FALLBACK
   }
}
