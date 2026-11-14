package com.fpvdrone.client.map;

import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.MapTileRequestPacket;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.blaze3d.platform.NativeImage.Format;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap.BasicEntry;
import it.unimi.dsi.fastutil.longs.Long2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReconLayer {
   private static final ReconLayer v5suI = new ReconLayer();
   private static final int ibzfxic = 32;
   private static final int wuhB = 512;
   private static final int dOfFwvo = 4;
   private static final int rsDd50tfV = 64;
   private static final int uSUb = 8;
   private static final int jGhI2 = 8;
   private static final int wEJx3Itb = 16;
   private static final long bAcdvq9 = 1500000L;
   private static final long c1jHYPb4 = 1000000L;
   private static final int v8ZN = 4096;
   private static final int zTT3nw6 = 16;
   private static final int vmpmU = 256;
   private static final int b69s = 1024;
   private static final int cXHvq = 16;
   private static final int rxYnaa = 256;
   private static final int r6s7FL3 = 1200;
   private static final int iAKNPAj = 1;
   private static final long qlv6NKWnH = 5000L;
   private static final int hOKMYHOaC = 2;
   private static final int iS1JnZov = 2;
   private static final long lfyfLwuw = 30000L;
   private static final int wyNDWDB = 40;
   private final Long2ObjectOpenHashMap<ReconLayer.Sy7j08t> iRWc2Pi = new Long2ObjectOpenHashMap();
   private final Long2ObjectOpenHashMap<ReconLayer.HrdCkfoU9x> tpzIHm = new Long2ObjectOpenHashMap(256);
   private final LongArrayList eJDY = new LongArrayList();
   private final LongOpenHashSet zHBAQgZP = new LongOpenHashSet();
   private final LongOpenHashSet we1GYY = new LongOpenHashSet();
   private final Long2LongOpenHashMap pxmzA = new Long2LongOpenHashMap();
   private int iElAbEZe;
   private final List<ReconLayer.KKeMvpuBgEY> sTrE = new ArrayList<>();
   private int ifpFOCaAs;
   private Identifier uQdlyiwB;
   private int ivDR;
   private long n9lX8A;
   private int wBXn;
   private int mM7DsB9AR;
   private final long[] uKpZYj = new long[64];
   private int l2IWOAGO = Integer.MAX_VALUE;
   private int xYttSy;
   private int kNoG;
   private int fvsAOg;
   private int lFt1ckP;
   private int qU1QioDiC;
   private static final Logger zQOhlhTZx = LoggerFactory.getLogger("FPVDrone/ReconLayer");
   private final ConcurrentHashMap<Long, Long> lvk8rWUGD = new ConcurrentHashMap<>();
   private final ConcurrentLinkedQueue<ReconLayer.Rdl0X5jeod> pyHY5 = new ConcurrentLinkedQueue<>();
   private final ArrayBlockingQueue<NativeImage> idXga = new ArrayBlockingQueue<>(Math.max(4, MapExecutors.wuhB() * 4));
   private static final int i263D9 = 20;

   public static ReconLayer v5suI() {
      return v5suI;
   }

   private ReconLayer() {
   }

   public void v5suI(Identifier var1, int var2, int var3) {
      if (this.uQdlyiwB == null || this.uQdlyiwB.equals(var1)) {
         long var4 = ChunkPos.pack(var2, var3);
         this.v5suI(var4);
      }
   }

   private void v5suI(long var1) {
      if (this.pxmzA.remove(var1) != 0L && this.iElAbEZe > 0) {
         this.iElAbEZe--;
      }

      if (this.we1GYY.add(var1)) {
         int var3 = ChunkPos.getX(var1) * 8;
         int var4 = ChunkPos.getZ(var1) * 8;

         for (int var5 = 0; var5 < 8; var5++) {
            for (int var6 = 0; var6 < 8; var6++) {
               ReconLayer.HrdCkfoU9x var7 = (ReconLayer.HrdCkfoU9x)this.tpzIHm.get(ChunkPos.pack(var3 + var6, var4 + var5));
               if (var7 != null) {
                  var7.jGhI2 = Long.MIN_VALUE;
               }
            }
         }
      }
   }

   public void v5suI(GuiGraphicsExtractor var1, Identifier var2, double var3, double var5, double var7, double var9, double var11, int var13, int var14, int var15) {
      if (var2 != null) {
         if (!var2.equals(this.uQdlyiwB)) {
            this.ibzfxic();
            this.uQdlyiwB = var2;
         }

         this.ivDR++;
         long var16 = System.currentTimeMillis();
         this.n9lX8A = System.nanoTime() + 1500000L;
         this.wBXn = 0;
         this.mM7DsB9AR = 0;
         if (this.ivDR % 40 == 0) {
            this.ibzfxic(var16);
         }

         long var18 = System.nanoTime() + 1000000L;

         ReconLayer.Rdl0X5jeod var20;
         while ((var20 = this.pyHY5.poll()) != null) {
            this.lvk8rWUGD.remove(var20.v5suI(), var20.ibzfxic());
            ReconLayer.HrdCkfoU9x var21 = (ReconLayer.HrdCkfoU9x)this.tpzIHm.get(var20.v5suI());
            if (var21 != null && this.v5suI(var21, var20.dOfFwvo())) {
               var21.wuhB = var20.ibzfxic();
               var21.dOfFwvo = var20.wuhB();
            }

            this.v5suI(var20.dOfFwvo());
            if (System.nanoTime() >= var18) {
               break;
            }
         }

         double var48 = var3 + (0.0 - var9) / var7;
         double var23 = var3 + (var15 - var9) / var7;
         double var25 = var5 + (var13 - var11) / var7;
         double var27 = var5 + (var14 - var11) / var7;
         int var29 = Math.floorDiv(Mth.floor(var48), 512);
         int var30 = Math.floorDiv(Mth.floor(var23), 512);
         int var31 = Math.floorDiv(Mth.floor(var25), 512);
         int var32 = Math.floorDiv(Mth.floor(var27), 512);
         int var33 = Math.floorDiv(Mth.floor(var3), 512);
         int var34 = Math.floorDiv(Mth.floor(var5), 512);
         this.v5suI(var29, var30, var31, var32, var33, var34);
         ReconStore var35 = ReconStore.v5suI();
         Matrix4f var36 = null; // 26.3: quads are drawn through GuiGraphicsExtractor, which applies its own pose
         int var37 = 0;
         int var38 = 0;

         for (int var39 = this.eJDY.size(); var38 < var39; var38++) {
            long var40 = this.eJDY.getLong(var38);
            int var42 = ChunkPos.getX(var40);
            int var43 = ChunkPos.getZ(var40);
            ReconLayer.Sy7j08t var44 = (ReconLayer.Sy7j08t)this.iRWc2Pi.get(var40);
            if (var44 == null) {
               var44 = new ReconLayer.Sy7j08t();
               this.iRWc2Pi.put(var40, var44);
            }

            var44.jGhI2 = this.ivDR;
            if (this.ivDR >= var44.ibzfxic && this.wBXn < 16) {
               var44.v5suI = var35.wuhB(var2, var42, var43);
               var44.ibzfxic = this.ivDR + 8;
               this.wBXn++;
            }

            if (var44.v5suI > 0L) {
               this.v5suI(var2, var40, var42, var43, var16);
               boolean var45 = !var44.dOfFwvo && var44.wuhB != var44.v5suI;
               boolean var46 = var45 && var37 < 1;
               if (var46) {
                  var37++;
                  if (var44.rsDd50tfV == 0L) {
                     var44.rsDd50tfV = var16;
                  }
               }

               boolean var47 = this.v5suI(
                  var1, var36, var35, var2, var44, var40, var42, var43, var46, var48, var23, var25, var27, var3, var5, var7, var9, var11, var13, var14, var15
               );
               if (var46 && (var47 || var16 - var44.rsDd50tfV >= 5000L)) {
                  var44.wuhB = var44.v5suI;
                  var44.dOfFwvo = true;
               }
            }
         }

         this.v5suI(var1, var36);
         this.dOfFwvo();
      }
   }

   private void v5suI(int var1, int var2, int var3, int var4, int var5, int var6) {
      if (var1 != this.l2IWOAGO || var2 != this.xYttSy || var3 != this.kNoG || var4 != this.fvsAOg || var5 != this.lFt1ckP || var6 != this.qU1QioDiC) {
         this.l2IWOAGO = var1;
         this.xYttSy = var2;
         this.kNoG = var3;
         this.fvsAOg = var4;
         this.lFt1ckP = var5;
         this.qU1QioDiC = var6;
         this.eJDY.clear();

         for (int var7 = var3; var7 <= var4; var7++) {
            for (int var8 = var1; var8 <= var2; var8++) {
               this.eJDY.add(ChunkPos.pack(var8, var7));
            }
         }

         int var9 = var5;
         int var10 = var6;
         LongArrays.quickSort(this.eJDY.elements(), 0, this.eJDY.size(), (var2x, var4x) -> Long.compare(v5suI(var2x, var9, var10), v5suI(var4x, var9, var10)));
      }
   }

   private static long v5suI(long var0, int var2, int var3) {
      long var4 = ChunkPos.getX(var0) - var2;
      long var6 = ChunkPos.getZ(var0) - var3;
      return var4 * var4 + var6 * var6;
   }

   private void v5suI(Identifier var1, long var2, int var4, int var5, long var6) {
      if (!this.zHBAQgZP.contains(var2) && this.iElAbEZe < 2 && this.mM7DsB9AR < 2) {
         this.zHBAQgZP.add(var2);
         this.pxmzA.put(var2, var6);
         this.iElAbEZe++;
         this.mM7DsB9AR++;
         ModNetwork.CHANNEL.sendToServer(new MapTileRequestPacket(var1, var4, var5));
      }
   }

   private void ibzfxic(long var1) {
      if (!this.pxmzA.isEmpty()) {
         LongArrayList var3 = null;
         ObjectIterator var4 = this.pxmzA.long2LongEntrySet().iterator();

         while (var4.hasNext()) {
            var var5 = (Entry)var4.next();
            if (var1 - var5.getLongValue() >= 30000L) {
               if (var3 == null) {
                  var3 = new LongArrayList();
               }

               var3.add(var5.getLongKey());
            }
         }

         if (var3 != null) {
            for (int var6 = 0; var6 < var3.size(); var6++) {
               this.v5suI(var3.getLong(var6));
            }
         }
      }
   }

   private boolean v5suI(
      GuiGraphicsExtractor var1,
      Matrix4f var2,
      ReconStore var3,
      Identifier var4,
      ReconLayer.Sy7j08t var5,
      long var6,
      int var8,
      int var9,
      boolean var10,
      double var11,
      double var13,
      double var15,
      double var17,
      double var19,
      double var21,
      double var23,
      double var25,
      double var27,
      int var29,
      int var30,
      int var31
   ) {
      int var32 = var8 * 512;
      int var33 = var9 * 512;
      int var34 = v5suI(Math.floorDiv(Mth.floor(var11) - var32, 64));
      int var35 = v5suI(Math.floorDiv(Mth.floor(var13) - var32, 64));
      int var36 = v5suI(Math.floorDiv(Mth.floor(var15) - var33, 64));
      int var37 = v5suI(Math.floorDiv(Mth.floor(var17) - var33, 64));
      int var38 = var35 - var34 + 1;
      int var39 = var37 - var36 + 1;
      int var40 = var8 * 8;
      int var41 = var9 * 8;
      boolean var42 = var10 || this.ivDR >= var5.uSUb;
      if (var42) {
         var3.v5suI(var4, var40 + var34, var41 + var36, var38, var39, this.uKpZYj);
         var5.uSUb = this.ivDR + 8;
      }

      boolean var43 = true;

      for (int var44 = 0; var44 < var39; var44++) {
         for (int var45 = 0; var45 < var38; var45++) {
            int var46 = var40 + var34 + var45;
            int var47 = var41 + var36 + var44;
            long var48 = ChunkPos.pack(var46, var47);
            ReconLayer.HrdCkfoU9x var50 = (ReconLayer.HrdCkfoU9x)this.tpzIHm.get(var48);
            if (var42) {
               long var51 = this.uKpZYj[var44 * var38 + var45];
               if (var50 == null) {
                  if (var51 == 0L) {
                     continue;
                  }

                  var50 = new ReconLayer.HrdCkfoU9x();
                  this.tpzIHm.put(var48, var50);
               }

               var50.rsDd50tfV = var51;
            } else if (var50 == null) {
               continue;
            }

            var50.uSUb = this.ivDR;
            if (!this.v5suI(var3, var4, var6, var46, var47, var50)) {
               var43 = false;
            }

            this.v5suI(var1, var2, var50, var46, var47, var19, var21, var23, var25, var27, var29, var30, var31);
         }
      }

      return var43;
   }

   private static int v5suI(int var0) {
      return var0 < 0 ? 0 : Math.min(var0, 7);
   }

   private void v5suI(
      GuiGraphicsExtractor var1,
      Matrix4f var2,
      ReconLayer.HrdCkfoU9x var3,
      int var4,
      int var5,
      double var6,
      double var8,
      double var10,
      double var12,
      double var14,
      int var16,
      int var17,
      int var18
   ) {
      if (var3.v5suI >= 0) {
         double var19 = var4 * 64.0;
         double var21 = var5 * 64.0;
         int var23 = (int)Math.round(var12 + (var19 - var6) * var10);
         int var24 = (int)Math.round(var14 + (var21 - var8) * var10);
         int var25 = (int)Math.round(var12 + (var19 + 64.0 - var6) * var10);
         int var26 = (int)Math.round(var14 + (var21 + 64.0 - var8) * var10);
         if (var25 > var23 && var26 > var24 && var25 >= 0 && var23 <= var18 && var26 >= var16 && var24 <= var17) {
            ReconLayer.KKeMvpuBgEY var27 = this.sTrE.get(var3.v5suI);
            float var28 = var3.ibzfxic % 16 * 64.0F / 1024.0F;
            float var29 = var3.ibzfxic / 16 * 64.0F / 1024.0F;
            float var30 = 0.0625F;
            var27.dOfFwvo.add(var23);
            var27.dOfFwvo.add(var24);
            var27.dOfFwvo.add(var25);
            var27.dOfFwvo.add(var26);
            var27.dOfFwvo.add(var28);
            var27.dOfFwvo.add(var29);
            var27.dOfFwvo.add(var28 + var30);
            var27.dOfFwvo.add(var29 + var30);
         }
      }
   }

   private void v5suI(GuiGraphicsExtractor var1, Matrix4f var2) {
      // 1.1.8 wrote these quads into a text render type (no depth test, full light); 26.3 GUI drawing is a textured
      // blit. Blits take whole-pixel corners, so each quad is drawn as a unit square scaled into its exact float box.
      for (ReconLayer.KKeMvpuBgEY var4 : this.sTrE) {
         if (!var4.dOfFwvo.isEmpty()) {
            float[] var6 = var4.dOfFwvo.elements();

            for (int var7 = 0, var8 = var4.dOfFwvo.size(); var7 < var8; var7 += 8) {
               float var9 = var6[var7];
               float var10 = var6[var7 + 1];
               float var11 = var6[var7 + 2];
               float var12 = var6[var7 + 3];
               var1.pose().pushMatrix();
               var1.pose().translate(var9, var10);
               var1.pose().scale(var11 - var9, var12 - var10);
               var1.blit(var4.v5suI, 0, 0, 1, 1, var6[var7 + 4], var6[var7 + 6], var6[var7 + 5], var6[var7 + 7]);
               var1.pose().popMatrix();
            }

            var4.dOfFwvo.clear();
         }
      }
   }

   private boolean v5suI(ReconStore var1, Identifier var2, long var3, int var5, int var6, ReconLayer.HrdCkfoU9x var7) {
      int var8 = BlockMapColors.v5suI();
      boolean var9 = var7.rsDd50tfV != 0L && (var7.wuhB != var7.rsDd50tfV || var7.dOfFwvo != var8);
      if (!var9) {
         return true;
      }

      if (!this.ibzfxic(var1, var2, var3, var5, var6, var7)) {
         return false;
      }

      if (var7.v5suI < 0 && this.ifpFOCaAs >= 4096) {
         return false;
      }

      long var10 = ChunkPos.pack(var5, var6);
      long var12 = var7.rsDd50tfV;
      Long var14 = this.lvk8rWUGD.get(var10);
      if (var14 != null && var14 == var12) {
         return false;
      }

      if (System.nanoTime() >= this.n9lX8A) {
         return false;
      }

      for (int var15 = 0; var15 < 4; var15++) {
         for (int var16 = 0; var16 < 4; var16++) {
            ReconPixelPainter.v5suI(var1.v5suI(var2, var5 * 4 + var16, var6 * 4 + var15));
         }
      }

      this.lvk8rWUGD.put(var10, var12);
      MapExecutors.ibzfxic().execute(() -> {
         NativeImage var10x = null;

         try {
            int[] var11 = this.v5suI(var1, var2, var5, var6);
            var10x = this.wuhB();

            for (int var12x = 0; var12x < 64; var12x++) {
               for (int var13 = 0; var13 < 64; var13++) {
                  var10x.setPixelABGR(var13, var12x, var11[var12x * 64 + var13]); // 1.20 setPixelRGBA took the same ABGR int
               }
            }

            this.pyHY5.add(new ReconLayer.Rdl0X5jeod(var10, var12, var8, var10x));
         } catch (Exception var14x) {
            zQOhlhTZx.debug("recon subgroup compute skipped", var14x);
            if (var10x != null) {
               this.v5suI(var10x);
            }

            this.lvk8rWUGD.remove(var10, var12);
         }
      });
      return false;
   }

   private boolean ibzfxic(ReconStore var1, Identifier var2, long var3, int var5, int var6, ReconLayer.HrdCkfoU9x var7) {
      if (var7.jGhI2 == var7.rsDd50tfV) {
         return var7.wEJx3Itb;
      }

      int var8 = var1.v5suI(var2, var5 * 4, var6 * 4, 4);
      byte var9 = 16;
      var7.wEJx3Itb = var8 == var9 || var8 > 0 && this.we1GYY.contains(var3);
      var7.jGhI2 = var7.rsDd50tfV;
      return var7.wEJx3Itb;
   }

   private int[] v5suI(ReconStore var1, Identifier var2, int var3, int var4) {
      int var5 = var3 * 64;
      int var6 = var4 * 64;
      int var7 = var3 * 4;
      int var8 = var4 * 4;
      float var9 = 1.0F;
      float var10 = 1.0F;
      float var11 = 1.0F;
      if (var2.equals(Level.OVERWORLD.identifier())) {
         var9 = 0.518F;
         var10 = 0.678F;
         var11 = 1.0F;
      } else if (var2.equals(Level.NETHER.identifier())) {
         var9 = 1.0F;
         var10 = 0.0F;
         var11 = 0.0F;
      }

      byte var12 = 65;
      int[] var13 = new int[4225];
      Arrays.fill(var13, Integer.MIN_VALUE);
      int[] var14 = new int[4096];
      ReconStore.Rdl0X5jeod[] var15 = new ReconStore.Rdl0X5jeod[16];
      var1.ibzfxic();

      try {
         for (int var16 = 0; var16 < var15.length; var16++) {
            var15[var16] = var1.v5suI(var2, var7 + var16 % 4, var8 + var16 / 4);
         }

         for (int var28 = 0; var28 < 64; var28++) {
            var13[(var28 + 1) * 65] = var1.ibzfxic(var2, var5 - 1, var6 + var28);
         }

         for (int var29 = 0; var29 < 64; var29++) {
            var13[var29 + 1] = var1.ibzfxic(var2, var5 + var29, var6 - 1);
         }

         var13[0] = var1.ibzfxic(var2, var5 - 1, var6 - 1);
      } finally {
         var1.wuhB();
      }

      int[] var30 = null;

      for (int var17 = 0; var17 < var15.length; var17++) {
         ReconStore.Rdl0X5jeod var18 = var15[var17];
         if (var18 != null) {
            int var19 = var17 % 4;
            int var20 = var17 / 4;
            int var21 = var19 * 16;
            int var22 = var20 * 16;
            int[] var23;
            if (var18.v5suI()) {
               var23 = var18.v5suI;
            } else {
               if (var30 == null) {
                  var30 = new int[256];
               }

               ReconPixelPainter.v5suI(var18, var7 + var19, var8 + var20, var30);
               var23 = var30;
            }

            for (int var24 = 0; var24 < 16; var24++) {
               for (int var25 = 0; var25 < 16; var25++) {
                  var13[(var22 + var24 + 1) * 65 + var21 + var25 + 1] = var18.ibzfxic[var24 * 16 + var25];
                  var14[(var22 + var24) * 64 + var21 + var25] = var23[var24 * 16 + var25];
               }
            }
         }
      }

      return MapShading.v5suI(var14, var13, 65, 64, var9, var10, var11);
   }

   private boolean v5suI(ReconLayer.HrdCkfoU9x var1, NativeImage var2) {
      if (var1.v5suI < 0 && !this.v5suI(var1)) {
         return false;
      }

      ReconLayer.KKeMvpuBgEY var3 = this.sTrE.get(var1.v5suI);
      int var4 = var1.ibzfxic % 16 * 64;
      int var5 = var1.ibzfxic / 16 * 64;
      RenderSystem.getDevice().createCommandEncoder().writeToTexture(var3.ibzfxic.getTexture(), var2, 0, 0, var4, var5);
      return true;
   }

   private NativeImage wuhB() {
      NativeImage var1 = this.idXga.poll();
      return var1 != null ? var1 : new NativeImage(Format.RGBA, 64, 64, false);
   }

   private void v5suI(NativeImage var1) {
      if (var1 != null && !this.idXga.offer(var1)) {
         var1.close();
      }
   }

   private boolean v5suI(ReconLayer.HrdCkfoU9x var1) {
      for (int var2 = 0; var2 < this.sTrE.size(); var2++) {
         ReconLayer.KKeMvpuBgEY var3 = this.sTrE.get(var2);
         if (!var3.wuhB.isEmpty()) {
            var1.v5suI = var2;
            var1.ibzfxic = var3.wuhB.popInt();
            this.ifpFOCaAs++;
            return true;
         }
      }

      if (this.sTrE.size() >= 16) {
         return false;
      }

      ReconLayer.Ol3TEDSog var5 = new ReconLayer.Ol3TEDSog(1024);
      Identifier var6 = Identifier.fromNamespaceAndPath("fpvdrone", "recon_layer/page/" + this.sTrE.size());
      Minecraft.getInstance().getTextureManager().register(var6, var5);
      ReconLayer.KKeMvpuBgEY var4 = new ReconLayer.KKeMvpuBgEY(var6, var5);
      this.sTrE.add(var4);
      var1.v5suI = this.sTrE.size() - 1;
      var1.ibzfxic = var4.wuhB.popInt();
      this.ifpFOCaAs++;
      return true;
   }

   private void dOfFwvo() {
      if (this.ivDR % 20 == 0) {
         ObjectIterator var1 = this.tpzIHm.long2ObjectEntrySet().iterator();

         while (var1.hasNext()) {
            it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry var2 = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry)var1.next();
            if (this.ivDR - ((ReconLayer.HrdCkfoU9x)var2.getValue()).uSUb > 1200) {
               this.ibzfxic((ReconLayer.HrdCkfoU9x)var2.getValue());
               var1.remove();
            }
         }

         this.iRWc2Pi.values().removeIf(var1x -> this.ivDR - var1x.jGhI2 > 1200);
         int var6 = this.ifpFOCaAs - 3840;
         if (var6 > 0) {
            var var3 = new ArrayList();
            ObjectIterator var4 = this.tpzIHm.long2ObjectEntrySet().iterator();

            while (var4.hasNext()) {
               it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry var5 = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry)var4.next();
               if (((ReconLayer.HrdCkfoU9x)var5.getValue()).v5suI >= 0 && ((ReconLayer.HrdCkfoU9x)var5.getValue()).uSUb != this.ivDR) {
                  var3.add(new BasicEntry(var5.getLongKey(), (ReconLayer.HrdCkfoU9x)var5.getValue()));
               }
            }

            var3.sort(Comparator.comparingInt(var0 -> ((ReconLayer.HrdCkfoU9x)((it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry<?>)var0).getValue()).uSUb));

            for (int var7 = 0; var7 < var6 && var7 < var3.size(); var7++) {
               it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry var8 = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry)var3.get(var7);
               this.ibzfxic((ReconLayer.HrdCkfoU9x)var8.getValue());
               this.tpzIHm.remove(var8.getLongKey());
            }
         }
      }
   }

   private void ibzfxic(ReconLayer.HrdCkfoU9x var1) {
      if (var1.v5suI >= 0) {
         this.sTrE.get(var1.v5suI).wuhB.add(var1.ibzfxic);
         var1.v5suI = -1;
         var1.ibzfxic = -1;
         this.ifpFOCaAs--;
      }
   }

   public void ibzfxic() {
      this.tpzIHm.clear();

      for (ReconLayer.KKeMvpuBgEY var2 : this.sTrE) {
         Minecraft.getInstance().getTextureManager().release(var2.v5suI);
      }

      this.sTrE.clear();
      this.ifpFOCaAs = 0;
      this.iRWc2Pi.clear();
      this.zHBAQgZP.clear();
      this.we1GYY.clear();
      this.pxmzA.clear();
      this.iElAbEZe = 0;
      this.l2IWOAGO = Integer.MAX_VALUE;
      this.eJDY.clear();
      this.lvk8rWUGD.clear();

      ReconLayer.Rdl0X5jeod var3;
      while ((var3 = this.pyHY5.poll()) != null) {
         this.v5suI(var3.dOfFwvo());
      }
   }

   private static final class HrdCkfoU9x {
      int v5suI = -1;
      int ibzfxic = -1;
      long wuhB = -1L;
      int dOfFwvo = -1;
      long rsDd50tfV;
      int uSUb = -1;
      long jGhI2 = Long.MIN_VALUE;
      boolean wEJx3Itb;
   }

   private static final class KKeMvpuBgEY {
      final Identifier v5suI;
      final ReconLayer.Ol3TEDSog ibzfxic;
      final IntArrayList wuhB = new IntArrayList(256);
      final FloatArrayList dOfFwvo = new FloatArrayList();

      KKeMvpuBgEY(Identifier var1, ReconLayer.Ol3TEDSog var2) {
         this.v5suI = var1;
         this.ibzfxic = var2;

         for (int var3 = 255; var3 >= 0; var3--) {
            this.wuhB.add(var3);
         }
      }
   }

   private static final class Ol3TEDSog extends AbstractTexture {
      /** A blank square atlas page (1.1.8: TextureUtil.prepareImage + an all-zero upload; nearest filter, clamped). */
      Ol3TEDSog(int var1) {
         GpuDevice var2 = RenderSystem.getDevice();
         this.texture = var2.createTexture(() -> "fpvdrone recon atlas", 5, GpuFormat.RGBA8_UNORM, var1, var1, 1, 1);
         this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
         this.textureView = var2.createTextureView(this.texture);

         try (NativeImage var3 = new NativeImage(Format.RGBA, var1, var1, true)) {
            var2.createCommandEncoder().writeToTexture(this.texture, var3);
         }
      }
   }

   private static final class Rdl0X5jeod {
      private final long v5suI;
      private final long ibzfxic;
      private final int wuhB;
      private final NativeImage dOfFwvo;

      private Rdl0X5jeod(long var1, long var3, int var5, NativeImage var6) {
         this.v5suI = var1;
         this.ibzfxic = var3;
         this.wuhB = var5;
         this.dOfFwvo = var6;
      }


      public long v5suI() {

         return this.v5suI;

      }

   
      public long ibzfxic() {
         return this.ibzfxic;
      }

      public int wuhB() {
         return this.wuhB;
      }

      public NativeImage dOfFwvo() {
         return this.dOfFwvo;
      }
   }

   private static final class Sy7j08t {
      long v5suI = -1L;
      int ibzfxic = -1;
      long wuhB = -2L;
      boolean dOfFwvo;
      long rsDd50tfV;
      int uSUb = -1;
      int jGhI2 = -1;
   }
}
