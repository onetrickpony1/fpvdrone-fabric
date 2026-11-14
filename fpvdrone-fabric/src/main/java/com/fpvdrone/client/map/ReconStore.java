package com.fpvdrone.client.map;

import com.fpvdrone.client.map.ReconMarks;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ReconStore {
   private static final Logger rsDd50tfV = LoggerFactory.getLogger("FPVDrone/Recon");
   private static final ReconStore uSUb = new ReconStore();
   private static final int jGhI2 = 5;
   private static final int wEJx3Itb = 4;
   private static final int bAcdvq9 = 3;
   private static final int c1jHYPb4 = 2;
   private static final int v8ZN = 1;
   private static final int zTT3nw6 = 2;
   private static final int vmpmU = 1;
   private static final String b69s = "color_sig.txt";
   static final int v5suI = -16777216;
   public static final int ibzfxic = Integer.MIN_VALUE;
   public static final int wuhB = 32;
   public static final int dOfFwvo = 4;
   private final Map<Identifier, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod>> cXHvq = new HashMap<>();
   private final Map<Identifier, Long2LongOpenHashMap> rxYnaa = new HashMap<>();
   private final Map<Identifier, Long2LongOpenHashMap> r6s7FL3 = new HashMap<>();
   private final Map<Identifier, LongOpenHashSet> iAKNPAj = new HashMap<>();
   private final List<Path> qlv6NKWnH = new ArrayList<>();
   private ExecutorService hOKMYHOaC;
   private String iS1JnZov;
   private boolean lfyfLwuw;
   private volatile long wyNDWDB;
   private Path iRWc2Pi;
   private Object tpzIHm;
   private final ReentrantReadWriteLock eJDY = new ReentrantReadWriteLock();
   private final ArrayDeque<ReconStore.KKeMvpuBgEY> zHBAQgZP = new ArrayDeque<>();
   private static final int we1GYY = 32768;
   private static final long pxmzA = 3000000L;
   private static final Pattern iElAbEZe = Pattern.compile(".*\\.r\\.-?\\d+\\.-?\\d+\\.dat");

   public static ReconStore v5suI() {
      return uSUb;
   }

   private ReconStore() {
   }

   public void ibzfxic() {
      this.eJDY.readLock().lock();
   }

   public void wuhB() {
      this.eJDY.readLock().unlock();
   }

   public ReconStore.Rdl0X5jeod v5suI(Identifier var1, int var2, int var3) {
      this.eJDY.readLock().lock();

      try {
         Long2ObjectOpenHashMap var4 = this.cXHvq.get(var1);
         return var4 == null ? null : (ReconStore.Rdl0X5jeod)var4.get(ChunkPos.pack(var2, var3));
      } finally {
         this.eJDY.readLock().unlock();
      }
   }

   public int ibzfxic(Identifier var1, int var2, int var3) {
      this.eJDY.readLock().lock();

      try {
         Long2ObjectOpenHashMap var4 = this.cXHvq.get(var1);
         if (var4 == null) {
            return Integer.MIN_VALUE;
         }

         ReconStore.Rdl0X5jeod var5 = (ReconStore.Rdl0X5jeod)var4.get(ChunkPos.pack(var2 >> 4, var3 >> 4));
         return var5 == null ? Integer.MIN_VALUE : var5.ibzfxic[(var3 & 15) * 16 + (var2 & 15)];
      } finally {
         this.eJDY.readLock().unlock();
      }
   }

   public long wuhB(Identifier var1, int var2, int var3) {
      this.eJDY.readLock().lock();

      try {
         Long2LongOpenHashMap var4 = this.rxYnaa.get(var1);
         return var4 == null ? 0L : var4.get(ChunkPos.pack(var2, var3));
      } finally {
         this.eJDY.readLock().unlock();
      }
   }

   public long dOfFwvo(Identifier var1, int var2, int var3) {
      this.eJDY.readLock().lock();

      try {
         Long2LongOpenHashMap var4 = this.r6s7FL3.get(var1);
         return var4 == null ? 0L : var4.get(ChunkPos.pack(var2, var3));
      } finally {
         this.eJDY.readLock().unlock();
      }
   }

   public void v5suI(Identifier var1, int var2, int var3, int var4, int var5, long[] var6) {
      this.eJDY.readLock().lock();

      try {
         Long2LongOpenHashMap var7 = this.r6s7FL3.get(var1);
         if (var7 == null) {
            Arrays.fill(var6, 0, var4 * var5, 0L);
            return;
         }

         for (int var8 = 0; var8 < var5; var8++) {
            for (int var9 = 0; var9 < var4; var9++) {
               var6[var8 * var4 + var9] = var7.get(ChunkPos.pack(var2 + var9, var3 + var8));
            }
         }
      } finally {
         this.eJDY.readLock().unlock();
      }
   }

   public int v5suI(Identifier var1, int var2, int var3, int var4) {
      this.eJDY.readLock().lock();

      try {
         Long2ObjectOpenHashMap var5 = this.cXHvq.get(var1);
         if (var5 == null) {
            return 0;
         }

         int var6 = 0;

         for (int var7 = 0; var7 < var4; var7++) {
            for (int var8 = 0; var8 < var4; var8++) {
               if (var5.get(ChunkPos.pack(var2 + var8, var3 + var7)) != null) {
                  var6++;
               }
            }
         }

         return var6;
      } finally {
         this.eJDY.readLock().unlock();
      }
   }

   public void v5suI(Identifier var1, int var2, int var3, short[] var4, int[] var5, int[] var6) {
      synchronized (this.zHBAQgZP) {
         if (this.zHBAQgZP.size() < 32768) {
            this.zHBAQgZP.add(new ReconStore.KKeMvpuBgEY(var1, var2, var3, var4, var5, var6));
         }
      }
   }

   public void rsDd50tfV(Identifier var1, int var2, int var3) {
      synchronized (this.zHBAQgZP) {
         this.zHBAQgZP.add(new ReconStore.KKeMvpuBgEY(var1, var2, var3, null, null, null));
      }
   }

   public void dOfFwvo() {
      long var1 = System.nanoTime() + 3000000L;

      while (System.nanoTime() < var1) {
         ReconStore.KKeMvpuBgEY var3;
         synchronized (this.zHBAQgZP) {
            var3 = this.zHBAQgZP.poll();
         }

         if (var3 == null) {
            return;
         }

         if (var3.dOfFwvo() == null) {
            ReconLayer.v5suI().v5suI(var3.v5suI(), var3.ibzfxic(), var3.wuhB());
         } else {
            this.ibzfxic(var3.v5suI(), var3.ibzfxic(), var3.wuhB(), var3.dOfFwvo(), var3.rsDd50tfV(), var3.uSUb());
         }
      }
   }

   public void ibzfxic(Identifier var1, int var2, int var3, short[] var4, int[] var5, int[] var6) {
      byte var7 = 17;
      short[] var8 = new short[256];

      for (int var9 = 0; var9 < 16; var9++) {
         for (int var10 = 0; var10 < 16; var10++) {
            var8[var9 * 16 + var10] = var4[(var9 + 1) * var7 + var10 + 1];
         }
      }

      this.eJDY.writeLock().lock();

      try {
         this.ibzfxic(var1, var2, var3, new ReconStore.Rdl0X5jeod(null, var8, var5, var6, true, false, 0));
      } finally {
         this.eJDY.writeLock().unlock();
      }
   }

   public void v5suI(Identifier var1, int var2, int var3, ReconStore.Rdl0X5jeod var4) {
      this.v5suI(var1, var2, var3, var4, this.wyNDWDB);
   }

   public void v5suI(Identifier var1, int var2, int var3, ReconStore.Rdl0X5jeod var4, long var5) {
      this.eJDY.writeLock().lock();

      try {
         if (this.wyNDWDB == var5) {
            this.ibzfxic(var1, var2, var3, var4);
            return;
         }
      } finally {
         this.eJDY.writeLock().unlock();
      }
   }

   public long rsDd50tfV() {
      return this.wyNDWDB;
   }

   private void v5suI(Identifier var1, long var2) {
      this.lfyfLwuw = true;
      this.iAKNPAj.computeIfAbsent(var1, var0 -> new LongOpenHashSet()).add(var2);
   }

   private static long v5suI(int var0, int var1) {
      return ChunkPos.pack(Math.floorDiv(var0, 32), Math.floorDiv(var1, 32));
   }

   private void ibzfxic(Identifier var1, int var2, int var3, ReconStore.Rdl0X5jeod var4) {
      Long2ObjectOpenHashMap var5 = this.cXHvq.computeIfAbsent(var1, var0 -> new Long2ObjectOpenHashMap());
      long var6 = ChunkPos.pack(var2, var3);
      ReconStore.Rdl0X5jeod var8 = (ReconStore.Rdl0X5jeod)var5.get(var6);
      if (var8 == null || !v5suI(var8, var4)) {
         if (var8 == null || !var8.uSUb || var4.uSUb || !Arrays.equals(var8.ibzfxic, var4.ibzfxic)) {
            var5.put(var6, var4);
            this.uSUb(var1, var2, var3);
            this.jGhI2(var1, var2, var3);
            this.v5suI(var1, v5suI(var2, var3));
         }
      }
   }

   private static boolean v5suI(ReconStore.Rdl0X5jeod var0, ReconStore.Rdl0X5jeod var1) {
      if (!Arrays.equals(var0.ibzfxic, var1.ibzfxic)) {
         return false;
      } else {
         return var0.v5suI != null && var1.v5suI != null
            ? Arrays.equals(var0.v5suI, var1.v5suI)
            : var0.v5suI == null && var1.v5suI == null && Arrays.equals(var0.wuhB, var1.wuhB) && Arrays.equals(var0.dOfFwvo, var1.dOfFwvo);
      }
   }

   private void uSUb(Identifier var1, int var2, int var3) {
      long var4 = ChunkPos.pack(Math.floorDiv(var2, 32), Math.floorDiv(var3, 32));
      Long2LongOpenHashMap var6 = this.rxYnaa.computeIfAbsent(var1, var0 -> new Long2LongOpenHashMap());
      var6.addTo(var4, 1L);
      int var7 = Math.floorDiv(var2, 32);
      int var8 = Math.floorDiv(var3, 32);
      boolean var9 = Math.floorMod(var2, 32) == 0;
      boolean var10 = Math.floorMod(var3, 32) == 0;
      boolean var11 = Math.floorMod(var2, 32) == 31;
      boolean var12 = Math.floorMod(var3, 32) == 31;
      if (var9) {
         var6.addTo(ChunkPos.pack(var7 - 1, var8), 1L);
      }

      if (var10) {
         var6.addTo(ChunkPos.pack(var7, var8 - 1), 1L);
      }

      if (var9 && var10) {
         var6.addTo(ChunkPos.pack(var7 - 1, var8 - 1), 1L);
      }

      if (var11) {
         var6.addTo(ChunkPos.pack(var7 + 1, var8), 1L);
      }

      if (var12) {
         var6.addTo(ChunkPos.pack(var7, var8 + 1), 1L);
      }

      if (var11 && var12) {
         var6.addTo(ChunkPos.pack(var7 + 1, var8 + 1), 1L);
      }
   }

   private void jGhI2(Identifier var1, int var2, int var3) {
      int var4 = Math.floorDiv(var2, 4);
      int var5 = Math.floorDiv(var3, 4);
      Long2LongOpenHashMap var6 = this.r6s7FL3.computeIfAbsent(var1, var0 -> new Long2LongOpenHashMap());
      var6.addTo(ChunkPos.pack(var4, var5), 1L);
      boolean var7 = Math.floorMod(var2, 4) == 0;
      boolean var8 = Math.floorMod(var3, 4) == 0;
      boolean var9 = Math.floorMod(var2, 4) == 3;
      boolean var10 = Math.floorMod(var3, 4) == 3;
      if (var7) {
         var6.addTo(ChunkPos.pack(var4 - 1, var5), 1L);
      }

      if (var8) {
         var6.addTo(ChunkPos.pack(var4, var5 - 1), 1L);
      }

      if (var7 && var8) {
         var6.addTo(ChunkPos.pack(var4 - 1, var5 - 1), 1L);
      }

      if (var9) {
         var6.addTo(ChunkPos.pack(var4 + 1, var5), 1L);
      }

      if (var10) {
         var6.addTo(ChunkPos.pack(var4, var5 + 1), 1L);
      }

      if (var9 && var10) {
         var6.addTo(ChunkPos.pack(var4 + 1, var5 + 1), 1L);
      }
   }

   private void wEJx3Itb(Identifier var1, int var2, int var3) {
      byte var4 = 8;
      int var5 = var2 * var4;
      int var6 = var3 * var4;
      Long2LongOpenHashMap var7 = this.r6s7FL3.computeIfAbsent(var1, var0 -> new Long2LongOpenHashMap());

      for (int var8 = -1; var8 <= var4; var8++) {
         for (int var9 = -1; var9 <= var4; var9++) {
            var7.addTo(ChunkPos.pack(var5 + var9, var6 + var8), 1L);
         }
      }
   }

   public void v5suI(Object var1, Supplier<String> var2) {
      if (var1 == null) {
         if (this.tpzIHm != null) {
            this.uSUb();
            this.c1jHYPb4();
            this.eJDY.writeLock().lock();

            try {
               this.wEJx3Itb();
               this.iS1JnZov = null;
               this.tpzIHm = null;
            } finally {
               this.eJDY.writeLock().unlock();
            }
         } else {
            this.tpzIHm = null;
         }
      } else {
         if (var1 != this.tpzIHm) {
            this.uSUb();
            this.c1jHYPb4();
            this.eJDY.writeLock().lock();

            try {
               this.tpzIHm = var1;
               this.wEJx3Itb();
               this.iS1JnZov = null;
               this.iRWc2Pi = null;
            } finally {
               this.eJDY.writeLock().unlock();
            }

            ReconMarks.v5suI().ibzfxic();
         }

         if (this.iS1JnZov == null) {
            String var3 = (String)var2.get();
            if (var3 != null) {
               this.v5suI(var3);
            }
         }
      }
   }

   private void wEJx3Itb() {
      this.wyNDWDB++;
      this.cXHvq.clear();
      this.rxYnaa.clear();
      this.r6s7FL3.clear();
      this.iAKNPAj.clear();
      this.qlv6NKWnH.clear();
      synchronized (this.zHBAQgZP) {
         this.zHBAQgZP.clear();
      }

      this.lfyfLwuw = false;
   }

   public void v5suI(String var1) {
      String var2 = v8ZN();
      this.eJDY.writeLock().lock();

      Object var3;
      Path var4;
      try {
         this.iS1JnZov = var1;
         this.iRWc2Pi = wuhB(var1);
         this.wEJx3Itb();
         var4 = this.iRWc2Pi;
         var3 = this.tpzIHm;
      } finally {
         this.eJDY.writeLock().unlock();
      }

      if (var4 != null && Files.isDirectory(var4)) {
         this.bAcdvq9().execute(() -> {
            var var4x = new HashMap<Identifier, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod>>();
            var var5 = new ArrayList<Path>();

            try (Stream<Path> var6 = Files.list(var4)) {
               var6.filter(var0 -> var0.getFileName().toString().endsWith(".dat")).forEach(var4xx -> {
                  if (!ibzfxic(var4xx.getFileName().toString())) {
                     var5.add(var4xx);
                  }

                  this.v5suI(var4xx, var4x, var2);
               });
            } catch (Exception var11) {
            }

            this.v5suI(var3, var4x, var5);
         });
      }
   }

   private void v5suI(Object var1, Map<Identifier, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod>> var2, List<Path> var3) {
      this.eJDY.writeLock().lock();

      try {
         if (var1 != this.tpzIHm) {
            return;
         }

         for (Entry<?, ?> var5 : var2.entrySet()) {
            Long2ObjectOpenHashMap var6 = this.cXHvq.computeIfAbsent((Identifier)var5.getKey(), var0 -> new Long2ObjectOpenHashMap());
            ObjectIterator var7 = ((Long2ObjectOpenHashMap)var5.getValue()).long2ObjectEntrySet().iterator();

            while (var7.hasNext()) {
               it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry var8 = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry)var7.next();
               long var9 = var8.getLongKey();
               if (var6.putIfAbsent(var9, (ReconStore.Rdl0X5jeod)var8.getValue()) == null) {
                  this.uSUb((Identifier)var5.getKey(), ChunkPos.getX(var9), ChunkPos.getZ(var9));
                  this.jGhI2((Identifier)var5.getKey(), ChunkPos.getX(var9), ChunkPos.getZ(var9));
               }
            }
         }

         if (!var3.isEmpty()) {
            this.qlv6NKWnH.addAll(var3);

            for (Entry<?, ?> var15 : var2.entrySet()) {
               LongIterator var16 = ((Long2ObjectOpenHashMap)var15.getValue()).keySet().iterator();

               while (var16.hasNext()) {
                  long var17 = (Long)var16.next();
                  this.v5suI((Identifier)var15.getKey(), v5suI(ChunkPos.getX(var17), ChunkPos.getZ(var17)));
               }
            }
         }

         if (!var2.isEmpty()) {
         }
      } finally {
         this.eJDY.writeLock().unlock();
      }
   }

   private static boolean ibzfxic(String var0) {
      return iElAbEZe.matcher(var0).matches();
   }

   private void v5suI(Path var1, Map<Identifier, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod>> var2, String var3) {
      try (DataInputStream var4 = new DataInputStream(new BufferedInputStream(Files.newInputStream(var1)))) {
         int var5 = var4.readInt();
         if (var5 != 5) {
            if (var5 == 4) {
               this.v5suI(var4, var2, false);
            } else if (var5 == 3) {
               this.ibzfxic(var4, var2, false);
            } else if (var5 == 2) {
               this.v5suI(var4, var2);
            }
         } else {
            String var6 = var4.readUTF();
            this.v5suI(var4, var2, var3 != null && var3.equals(var6));
         }
      } catch (Exception var9) {
      }
   }

   private void v5suI(DataInputStream var1, Map<Identifier, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod>> var2, boolean var3) throws java.io.IOException {
      Identifier var4 = Identifier.tryParse(var1.readUTF());
      int var5 = var1.readInt();
      if (var4 != null && var5 >= 0) {
         int var6 = var3 ? BlockMapColors.v5suI() : 0;
         Long2ObjectOpenHashMap var7 = new Long2ObjectOpenHashMap(Math.max(16, var5));

         for (int var8 = 0; var8 < var5; var8++) {
            long var9 = var1.readLong();
            int var11 = var1.readByte() & 255;
            int[] var12 = null;
            if ((var11 & 1) != 0) {
               var12 = new int[256];

               for (int var13 = 0; var13 < 256; var13++) {
                  var12[var13] = var1.readInt();
               }
            }

            short[] var17 = new short[256];

            for (int var14 = 0; var14 < 256; var14++) {
               var17[var14] = var1.readShort();
            }

            int[] var18 = new int[256];

            for (int var15 = 0; var15 < 256; var15++) {
               var18[var15] = var1.readInt();
            }

            int[] var19 = new int[256];

            for (int var16 = 0; var16 < 256; var16++) {
               var19[var16] = var1.readInt();
            }

            boolean var20 = (var11 & 2) != 0;
            var7.put(var9, new ReconStore.Rdl0X5jeod(var12, var17, var18, var19, var20, true, var12 != null ? var6 : 0));
         }

         v5suI(var2, var4, var7);
      }
   }

   private void ibzfxic(DataInputStream var1, Map<Identifier, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod>> var2, boolean var3) throws java.io.IOException {
      int var4 = var3 ? BlockMapColors.v5suI() : 0;
      Identifier var5 = Identifier.tryParse(var1.readUTF());
      int var6 = var1.readInt();
      if (var5 != null && var6 >= 0) {
         Long2ObjectOpenHashMap var7 = new Long2ObjectOpenHashMap(Math.max(16, var6));

         for (int var8 = 0; var8 < var6; var8++) {
            long var9 = var1.readLong();
            int[] var11 = new int[256];
            short[] var12 = new short[256];
            int[] var13 = new int[256];
            int[] var14 = new int[256];

            for (int var15 = 0; var15 < 256; var15++) {
               var11[var15] = var1.readInt();
            }

            for (int var16 = 0; var16 < 256; var16++) {
               var12[var16] = var1.readShort();
            }

            for (int var17 = 0; var17 < 256; var17++) {
               var13[var17] = var1.readInt();
            }

            for (int var18 = 0; var18 < 256; var18++) {
               var14[var18] = var1.readInt();
            }

            var7.put(var9, new ReconStore.Rdl0X5jeod(var11, var12, var13, var14, true, true, var4));
         }

         v5suI(var2, var5, var7);
      }
   }

   private void v5suI(DataInputStream var1, Map<Identifier, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod>> var2) throws java.io.IOException {
      Identifier var3 = Identifier.tryParse(var1.readUTF());
      int var4 = var1.readInt();
      if (var3 != null && var4 >= 0) {
         Long2ObjectOpenHashMap var5 = new Long2ObjectOpenHashMap(Math.max(16, var4));

         for (int var6 = 0; var6 < var4; var6++) {
            long var7 = var1.readLong();
            int[] var9 = new int[256];
            short[] var10 = new short[256];

            for (int var11 = 0; var11 < 256; var11++) {
               var9[var11] = var1.readInt();
            }

            for (int var13 = 0; var13 < 256; var13++) {
               var10[var13] = var1.readShort();
            }

            int[] var14 = new int[256];
            int[] var12 = new int[256];
            Arrays.fill(var14, -1);
            Arrays.fill(var12, -1);
            var5.put(var7, new ReconStore.Rdl0X5jeod(var9, var10, var14, var12, false));
         }

         v5suI(var2, var3, var5);
      }
   }

   private static void v5suI(
      Map<Identifier, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod>> var0, Identifier var1, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod> var2
   ) {
      var0.merge(var1, var2, (var0x, var1x) -> {
         var0x.putAll(var1x);
         return (Long2ObjectOpenHashMap)var0x;
      });
   }

   public void uSUb() {
      String var1 = v8ZN();
      this.eJDY.writeLock().lock();

      Path var2;
      ArrayList var3;
      List var4;
      label150: {
         try {
            if (!this.lfyfLwuw || this.iAKNPAj.isEmpty()) {
               this.lfyfLwuw = false;
               return;
            }

            Path var5 = this.iRWc2Pi;
            if (var5 == null) {
               return;
            }

            var3 = new ArrayList();

            for (Entry<?, ?> var7 : this.iAKNPAj.entrySet()) {
               Long2ObjectOpenHashMap var8 = this.cXHvq.get(var7.getKey());
               if (var8 != null && !var8.isEmpty()) {
                  LongIterator var9 = ((LongOpenHashSet)var7.getValue()).iterator();

                  while (var9.hasNext()) {
                     long var10 = (Long)var9.next();
                     Long2ObjectOpenHashMap var12 = new Long2ObjectOpenHashMap(256);
                     int var13 = ChunkPos.getX(var10) * 32;
                     int var14 = ChunkPos.getZ(var10) * 32;

                     for (int var15 = 0; var15 < 32; var15++) {
                        for (int var16 = 0; var16 < 32; var16++) {
                           long var17 = ChunkPos.pack(var13 + var16, var14 + var15);
                           ReconStore.Rdl0X5jeod var19 = (ReconStore.Rdl0X5jeod)var8.get(var17);
                           if (var19 != null) {
                              var12.put(var17, var19);
                           }
                        }
                     }

                     if (!var12.isEmpty()) {
                        var3.add(new ReconStore.Ol3TEDSog((Identifier)var7.getKey(), var10, var12));
                     }
                  }
               }
            }

            this.iAKNPAj.clear();
            this.lfyfLwuw = false;
            var4 = this.qlv6NKWnH.isEmpty() ? List.of() : List.copyOf(this.qlv6NKWnH);
            this.qlv6NKWnH.clear();
            if (!var3.isEmpty()) {
               var2 = var5;
               break label150;
            }
         } finally {
            this.eJDY.writeLock().unlock();
         }

         return;
      }

      this.bAcdvq9().execute(() -> this.v5suI(var2, var3, var1, var4));
   }

   private void v5suI(Path var1, List<ReconStore.Ol3TEDSog> var2, String var3, List<Path> var4) {
      try {
         Files.createDirectories(var1);
      } catch (Exception var21) {
         return;
      }

      v5suI(var1);

      for (ReconStore.Ol3TEDSog var6 : var2) {
         String var7 = ibzfxic(var6.v5suI(), var6.ibzfxic());
         Path var8 = var1.resolve(var7 + ".dat");
         Path var9 = var1.resolve(var7 + ".dat.tmp");

         try (DataOutputStream var10 = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(var9)))) {
            var10.writeInt(5);
            var10.writeUTF(var3);
            var10.writeUTF(var6.v5suI().toString());
            var10.writeInt(var6.wuhB().size());
            ObjectIterator var11 = var6.wuhB().long2ObjectEntrySet().iterator();

            while (var11.hasNext()) {
               it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry var12 = (it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry)var11.next();
               var10.writeLong(var12.getLongKey());
               ReconStore.Rdl0X5jeod var13 = (ReconStore.Rdl0X5jeod)var12.getValue();
               boolean var14 = var13.v5suI != null && var13.v5suI();
               int var15 = (var14 ? 1 : 0) | (var13.rsDd50tfV ? 2 : 0);
               var10.writeByte(var15);
               if (var14) {
                  for (int var16 = 0; var16 < 256; var16++) {
                     var10.writeInt(var13.v5suI[var16]);
                  }
               }

               for (int var26 = 0; var26 < 256; var26++) {
                  var10.writeShort(var13.ibzfxic[var26]);
               }

               for (int var27 = 0; var27 < 256; var27++) {
                  var10.writeInt(var13.wuhB[var27]);
               }

               for (int var28 = 0; var28 < 256; var28++) {
                  var10.writeInt(var13.dOfFwvo[var28]);
               }
            }
         } catch (Exception var23) {
            continue;
         }

         try {
            Files.move(var9, var8, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         } catch (Exception var20) {
            try {
               Files.move(var9, var8, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception var19) {
            }
         }
      }

      for (Path var25 : var4) {
         try {
            Files.deleteIfExists(var25);
         } catch (Exception var17) {
         }
      }
   }

   private static String ibzfxic(Identifier var0, long var1) {
      return dOfFwvo(var0.toString()) + ".r." + ChunkPos.getX(var1) + "." + ChunkPos.getZ(var1);
   }

   private ExecutorService bAcdvq9() {
      if (this.hOKMYHOaC == null) {
         this.hOKMYHOaC = Executors.newSingleThreadExecutor(var0 -> {
            Thread var1 = new Thread(var0, "FPVDrone-ReconSave");
            var1.setDaemon(true);
            return var1;
         });
      }

      return this.hOKMYHOaC;
   }

   private void c1jHYPb4() {
      ExecutorService var1 = this.hOKMYHOaC;
      if (var1 != null) {
         try {
            var1.submit(() -> {}).get(5L, TimeUnit.SECONDS);
         } catch (Exception var3) {
         }
      }
   }

   public void jGhI2() {
      this.uSUb();
      this.c1jHYPb4();
      this.eJDY.writeLock().lock();

      try {
         this.wEJx3Itb();
         this.iS1JnZov = null;
         this.iRWc2Pi = null;
         this.tpzIHm = null;
      } finally {
         this.eJDY.writeLock().unlock();
      }
   }

   private static Path wuhB(String var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.hasSingleplayerServer() && var1.getSingleplayerServer() != null) {
         try {
            return var1.getSingleplayerServer().getWorldPath(LevelResource.ROOT).resolve("fpvdrone_recon");
         } catch (Exception var3) {
         }
      }

      return var0 == null ? null : var1.gameDirectory.toPath().resolve("fpvdrone").resolve("recon").resolve(dOfFwvo(var0));
   }

   private static String dOfFwvo(String var0) {
      return var0.replaceAll("[^a-zA-Z0-9._-]", "_");
   }

   private static String v8ZN() {
      StringBuilder var0 = new StringBuilder(64);
      var0.append(1).append('|');

      try {
         for (String var2 : Minecraft.getInstance().getResourcePackRepository().getSelectedIds()) {
            var0.append(var2).append(',');
         }
      } catch (Exception var3) {
      }

      return var0.toString();
   }

   private static void v5suI(Path var0) {
      try {
         Files.deleteIfExists(var0.resolve("color_sig.txt"));
      } catch (Exception var2) {
      }
   }

   private static final class KKeMvpuBgEY {
      private final Identifier v5suI;
      private final int ibzfxic;
      private final int wuhB;
      private final short[] dOfFwvo;
      private final int[] rsDd50tfV;
      private final int[] uSUb;

      private KKeMvpuBgEY(Identifier var1, int var2, int var3, short[] var4, int[] var5, int[] var6) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var5;
         this.uSUb = var6;
      }


      public Identifier v5suI() {

         return this.v5suI;

      }

   
      public int ibzfxic() {
         return this.ibzfxic;
      }

      public int wuhB() {
         return this.wuhB;
      }

      public short[] dOfFwvo() {
         return this.dOfFwvo;
      }

      public int[] rsDd50tfV() {
         return this.rsDd50tfV;
      }

      public int[] uSUb() {
         return this.uSUb;
      }
   }

   private static final class Ol3TEDSog {
      private final Identifier v5suI;
      private final long ibzfxic;
      private final Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod> wuhB;

      private Ol3TEDSog(Identifier var1, long var2, Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod> var4) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var4;
      }


      public Identifier v5suI() {

         return this.v5suI;

      }

   
      public long ibzfxic() {
         return this.ibzfxic;
      }

      public Long2ObjectOpenHashMap<ReconStore.Rdl0X5jeod> wuhB() {
         return this.wuhB;
      }
   }

   public static final class Rdl0X5jeod {
      public final int[] v5suI;
      public final short[] ibzfxic;
      public final int[] wuhB;
      public final int[] dOfFwvo;
      public final boolean rsDd50tfV;
      public final boolean uSUb;
      public final int jGhI2;

      public Rdl0X5jeod(int[] var1, short[] var2, int[] var3, int[] var4) {
         this(var1, var2, var3, var4, true, true, BlockMapColors.v5suI());
      }

      public Rdl0X5jeod(int[] var1, short[] var2, int[] var3, int[] var4, boolean var5) {
         this(var1, var2, var3, var4, var5, true, BlockMapColors.v5suI());
      }

      public Rdl0X5jeod(int[] var1, short[] var2, int[] var3, int[] var4, boolean var5, boolean var6) {
         this(var1, var2, var3, var4, var5, var6, BlockMapColors.v5suI());
      }

      public Rdl0X5jeod(int[] var1, short[] var2, int[] var3, int[] var4, boolean var5, boolean var6, int var7) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var5;
         this.uSUb = var6;
         this.jGhI2 = var7;
      }

      public boolean v5suI() {
         return !this.rsDd50tfV || this.jGhI2 == BlockMapColors.v5suI();
      }
   }
}
