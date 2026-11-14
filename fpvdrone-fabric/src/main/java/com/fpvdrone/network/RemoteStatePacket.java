package com.fpvdrone.network;

import com.fpvdrone.sync.KinematicState;
import com.fpvdrone.sync.RemoteSmoothed;

import com.fpvdrone.sync.SmoothingMode;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import com.fpvdrone.network.compat.NetworkContext;

public class RemoteStatePacket {
   public static final int v5suI = 1;
   public static final int ibzfxic = 2;
   public static final int wuhB = 4;
   public static final int dOfFwvo = 8;
   private final int rsDd50tfV;
   private final long uSUb;
   private final int jGhI2;
   private final double wEJx3Itb;
   private final double bAcdvq9;
   private final double c1jHYPb4;
   private final double v8ZN;
   private final double zTT3nw6;
   private final double vmpmU;
   private final float b69s;
   private final float cXHvq;
   private final float rxYnaa;
   private final float r6s7FL3;
   private final float iAKNPAj;
   private final float qlv6NKWnH;
   private final float hOKMYHOaC;

   public RemoteStatePacket(
      int var1,
      long var2,
      int var4,
      double var5,
      double var7,
      double var9,
      double var11,
      double var13,
      double var15,
      float var17,
      float var18,
      float var19,
      float var20,
      float var21,
      float var22,
      float var23
   ) {
      this.rsDd50tfV = var1;
      this.uSUb = var2;
      this.jGhI2 = var4;
      this.wEJx3Itb = var5;
      this.bAcdvq9 = var7;
      this.c1jHYPb4 = var9;
      this.v8ZN = var11;
      this.zTT3nw6 = var13;
      this.vmpmU = var15;
      this.b69s = var17;
      this.cXHvq = var18;
      this.rxYnaa = var19;
      this.r6s7FL3 = var20;
      this.iAKNPAj = var21;
      this.qlv6NKWnH = var22;
      this.hOKMYHOaC = var23;
   }

   public static RemoteStatePacket v5suI(int var0, long var1) {
      return new RemoteStatePacket(var0, var1, 2, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public static RemoteStatePacket v5suI(int var0, long var1, boolean var3, KinematicState var4) {
      int var5 = (var3 ? 1 : 0) | (var4.v8ZN ? 4 : 0) | (var4.zTT3nw6 ? 8 : 0);
      return new RemoteStatePacket(
         var0,
         var1,
         var5,
         var4.v5suI,
         var4.ibzfxic,
         var4.wuhB,
         var4.dOfFwvo,
         var4.rsDd50tfV,
         var4.uSUb,
         var4.jGhI2,
         var4.wEJx3Itb,
         var4.bAcdvq9,
         var4.c1jHYPb4.x,
         var4.c1jHYPb4.y,
         var4.c1jHYPb4.z,
         var4.c1jHYPb4.w
      );
   }

   public boolean v5suI() {
      return (this.jGhI2 & 2) != 0;
   }

   public static void encode(RemoteStatePacket var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.rsDd50tfV);
      var1.writeLong(var0.uSUb);
      var1.writeByte(var0.jGhI2);
      if (!var0.v5suI()) {
         var1.writeDouble(var0.wEJx3Itb);
         var1.writeDouble(var0.bAcdvq9);
         var1.writeDouble(var0.c1jHYPb4);
         var1.writeDouble(var0.v8ZN);
         var1.writeDouble(var0.zTT3nw6);
         var1.writeDouble(var0.vmpmU);
         if ((var0.jGhI2 & 4) != 0) {
            var1.writeFloat(var0.b69s);
            var1.writeFloat(var0.cXHvq);
            var1.writeFloat(var0.rxYnaa);
         }

         if ((var0.jGhI2 & 8) != 0) {
            var1.writeFloat(var0.r6s7FL3);
            var1.writeFloat(var0.iAKNPAj);
            var1.writeFloat(var0.qlv6NKWnH);
            var1.writeFloat(var0.hOKMYHOaC);
         }
      }
   }

   public static RemoteStatePacket decode(FriendlyByteBuf var0) {
      int var1 = var0.readVarInt();
      long var2 = var0.readLong();
      int var4 = var0.readByte() & 255;
      if ((var4 & 2) != 0) {
         return new RemoteStatePacket(var1, var2, var4, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
      }

      double var5 = var0.readDouble();
      double var7 = var0.readDouble();
      double var9 = var0.readDouble();
      double var11 = var0.readDouble();
      double var13 = var0.readDouble();
      double var15 = var0.readDouble();
      float var17 = 0.0F;
      float var18 = 0.0F;
      float var19 = 0.0F;
      if ((var4 & 4) != 0) {
         var17 = var0.readFloat();
         var18 = var0.readFloat();
         var19 = var0.readFloat();
      }

      float var20 = 0.0F;
      float var21 = 0.0F;
      float var22 = 0.0F;
      float var23 = 1.0F;
      if ((var4 & 8) != 0) {
         var20 = var0.readFloat();
         var21 = var0.readFloat();
         var22 = var0.readFloat();
         var23 = var0.readFloat();
      }

      return new RemoteStatePacket(var1, var2, var4, var5, var7, var9, var11, var13, var15, var17, var18, var19, var20, var21, var22, var23);
   }

   public static void handle(RemoteStatePacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         Minecraft var1x = Minecraft.getInstance();
         if (var1x.level != null) {
            Entity var2 = var1x.level.getEntity(var0.rsDd50tfV);
            if (var2 instanceof RemoteSmoothed var3) {
               if (var0.v5suI()) {
                  var2.discard();
               } else if (var3.b69s() != SmoothingMode.LOCAL_PILOT) {
                  KinematicState var4 = new KinematicState();
                  var4.v5suI(var0.wEJx3Itb, var0.bAcdvq9, var0.c1jHYPb4);
                  var4.ibzfxic(var0.v8ZN, var0.zTT3nw6, var0.vmpmU);
                  if ((var0.jGhI2 & 4) != 0) {
                     var4.v5suI(var0.b69s, var0.cXHvq, var0.rxYnaa);
                  }

                  if ((var0.jGhI2 & 8) != 0) {
                     var4.v5suI(var0.r6s7FL3, var0.iAKNPAj, var0.qlv6NKWnH, var0.hOKMYHOaC);
                  }

                  var3.cXHvq().v5suI(var4, var0.uSUb, (var0.jGhI2 & 1) != 0, var3.r6s7FL3());
               }
            }
         }
      });
      var1.get().setPacketHandled(true);
   }
}
