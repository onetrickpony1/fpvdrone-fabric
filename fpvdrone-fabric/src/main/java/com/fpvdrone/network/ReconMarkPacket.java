package com.fpvdrone.network;

import com.fpvdrone.client.map.ReconMarks;
import com.fpvdrone.data.FlightRoute;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import com.fpvdrone.network.compat.NetworkContext;

public class ReconMarkPacket {
   private final int v5suI;
   private final boolean ibzfxic;
   private final String wuhB;
   private final double dOfFwvo;
   private final double rsDd50tfV;
   private final double uSUb;
   private final Identifier jGhI2;
   private final FlightRoute wEJx3Itb;
   private final Vec3 bAcdvq9;
   private final float c1jHYPb4;
   private final int v8ZN;

   private ReconMarkPacket(
      int var1, boolean var2, String var3, double var4, double var6, double var8, Identifier var10, FlightRoute var11, Vec3 var12, float var13, int var14
   ) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
      this.dOfFwvo = var4;
      this.rsDd50tfV = var6;
      this.uSUb = var8;
      this.jGhI2 = var10;
      this.wEJx3Itb = var11;
      this.bAcdvq9 = var12;
      this.c1jHYPb4 = var13;
      this.v8ZN = var14;
   }

   public static ReconMarkPacket v5suI(
      int var0, String var1, double var2, double var4, double var6, Identifier var8, FlightRoute var9, Vec3 var10, float var11, int var12
   ) {
      return new ReconMarkPacket(var0, false, var1, var2, var4, var6, var8, var9, var10, var11, var12);
   }

   public static ReconMarkPacket v5suI(int var0) {
      return new ReconMarkPacket(var0, true, "", 0.0, 0.0, 0.0, null, null, null, 0.0F, 0);
   }

   public static void encode(ReconMarkPacket var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.v5suI);
      var1.writeBoolean(var0.ibzfxic);
      if (!var0.ibzfxic) {
         var1.writeUtf(var0.wuhB);
         var1.writeDouble(var0.dOfFwvo);
         var1.writeDouble(var0.rsDd50tfV);
         var1.writeDouble(var0.uSUb);
         var1.writeIdentifier(var0.jGhI2);
         boolean var2 = var0.wEJx3Itb != null && !var0.wEJx3Itb.dOfFwvo();
         var1.writeBoolean(var2);
         if (var2) {
            var0.wEJx3Itb.v5suI(var1);
         }

         boolean var3 = var0.bAcdvq9 != null;
         var1.writeBoolean(var3);
         if (var3) {
            var1.writeDouble(var0.bAcdvq9.x);
            var1.writeDouble(var0.bAcdvq9.y);
            var1.writeDouble(var0.bAcdvq9.z);
         }

         var1.writeFloat(var0.c1jHYPb4);
         var1.writeVarInt(var0.v8ZN);
      }
   }

   public static ReconMarkPacket decode(FriendlyByteBuf var0) {
      int var1 = var0.readVarInt();
      boolean var2 = var0.readBoolean();
      if (var2) {
         return v5suI(var1);
      }

      String var3 = var0.readUtf();
      double var4 = var0.readDouble();
      double var6 = var0.readDouble();
      double var8 = var0.readDouble();
      Identifier var10 = var0.readIdentifier();
      FlightRoute var11 = var0.readBoolean() ? FlightRoute.ibzfxic(var0) : null;
      Vec3 var12 = var0.readBoolean() ? new Vec3(var0.readDouble(), var0.readDouble(), var0.readDouble()) : null;
      float var13 = var0.readFloat();
      int var14 = var0.readVarInt();
      return v5suI(var1, var3, var4, var6, var8, var10, var11, var12, var13, var14);
   }

   public static void handle(ReconMarkPacket var0, Supplier<NetworkContext> var1) {
      var1.get()
         .enqueueWork(
            () -> {
                  if (var0.ibzfxic) {
                     ReconMarks.v5suI().wuhB(var0.v5suI);
                  } else {
                     ReconMarks.v5suI()
                        .v5suI(
                           var0.v5suI, var0.wuhB, var0.dOfFwvo, var0.rsDd50tfV, var0.uSUb, var0.jGhI2, var0.wEJx3Itb, var0.bAcdvq9, var0.c1jHYPb4, var0.v8ZN
                        );
                  }
               }
         );
      var1.get().setPacketHandled(true);
   }
}
