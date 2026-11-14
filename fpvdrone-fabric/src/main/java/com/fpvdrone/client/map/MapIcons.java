package com.fpvdrone.client.map;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.CRC32;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class MapIcons {
   private static final String c1jHYPb4 = "yPorMoD5z7c541xiwk/3dZla1H5NyjwRYC4YeweyW0AAeAuL5vMN9oWH55EXqSS+bRoVi3ZZ9cSEjg4/Gw==";
   private static final long v8ZN = -5515021787802344119L;
   private static final String zTT3nw6 = "du7jma1VQRzYuMI7+OiAKuoeFjLiA86u/1XHarBLtbadAZnpRrKeHjStnlFtUROheA==";
   private static final long vmpmU = -7803840394074020926L;
   private static final String b69s = "aLwOlq+xokLUlEeDuV3AQ7oaVSPmLofeJqz5";
   private static final long cXHvq = 3368097570437866599L;
   public static final Component v5suI = Component.literal("Это тестовая версия мода FPV Drone 1.1.8");
   public static final Component ibzfxic = Component.literal("This is a test version of the FPV Drone mod 1.1.8");
   public static final Component wuhB = Component.literal("https://boosty.to/pmckinder");
   public static final String dOfFwvo = "https://boosty.to/pmckinder";
   static volatile long rsDd50tfV = 0L;
   private static final String rxYnaa = "I2NHqjJBBxro/OA9i0bm7anFtkE8SdluLGs=";
   private static final long r6s7FL3 = -7781292184987826768L;
   private static final String iAKNPAj = "3QzyFwow1Y3VfASosPZeIuiMKJA1BVAFP3h9ouA=";
   private static final long qlv6NKWnH = 3491467782145512332L;
   private static final String hOKMYHOaC = "qmL7W/BFIj7au7DaR1vYrN0YHMsVjVJSbmcpoBorwNFF0A==";
   private static final long iS1JnZov = -2118152890662853429L;
   private static final String lfyfLwuw = "yKmWqMrR1YmT3EHsbMsIsGPoGNQFtxCz+ide0jeeeOgTAA==";
   private static final long wyNDWDB = -6594139602261316760L;
   public static final Identifier uSUb = Identifier.fromNamespaceAndPath("fpvdrone", "textures/gui/map_point.png");
   public static final Identifier jGhI2 = Identifier.fromNamespaceAndPath("fpvdrone", "textures/gui/attack_point.png");
   public static final Identifier wEJx3Itb = Identifier.fromNamespaceAndPath("fpvdrone", "textures/gui/move_to_sky_point.png");
   public static final Identifier bAcdvq9 = Identifier.fromNamespaceAndPath("fpvdrone", "textures/gui/move_to_low_point.png");

   private MapIcons() {
   }

   public static Component v5suI() {
      String var0 = Minecraft.getInstance().getLanguageManager().getSelected();
      boolean var1 = var0 != null && var0.toLowerCase(Locale.ROOT).startsWith("ru");
      return var1 ? v5suI : ibzfxic;
   }

   public static void ibzfxic() {
      rsDd50tfV = System.currentTimeMillis();
   }

   public static long wuhB() {
      CRC32 var0 = new CRC32();
      var0.update((v5suI.getString() + "|" + ibzfxic.getString() + "|" + dOfFwvo).getBytes(StandardCharsets.UTF_8));
      return var0.getValue();
   }
}
