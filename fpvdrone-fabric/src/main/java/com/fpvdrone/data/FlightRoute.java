package com.fpvdrone.data;

import com.fpvdrone.data.RouteSpeed;
import com.fpvdrone.data.TerminalMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public final class FlightRoute {
   public static final int v5suI = 64;
   private final List<Waypoint> ibzfxic;
   private final TerminalMode wuhB;

   public FlightRoute(List<Waypoint> var1, TerminalMode var2) {
      this.ibzfxic = List.copyOf(var1);
      this.wuhB = var2 == null ? TerminalMode.LOITER : var2;
   }

   public static FlightRoute v5suI() {
      return new FlightRoute(Collections.emptyList(), TerminalMode.LOITER);
   }

   public List<Waypoint> ibzfxic() {
      return this.ibzfxic;
   }

   public Waypoint v5suI(int var1) {
      return this.ibzfxic.get(var1);
   }

   public int wuhB() {
      return this.ibzfxic.size();
   }

   public boolean dOfFwvo() {
      return this.ibzfxic.isEmpty();
   }

   public TerminalMode rsDd50tfV() {
      return this.wuhB;
   }

   public void v5suI(FriendlyByteBuf var1) {
      var1.writeByte(this.wuhB.ordinal());
      int var2 = Math.min(this.ibzfxic.size(), 64);
      var1.writeVarInt(var2);

      for (int var3 = 0; var3 < var2; var3++) {
         Waypoint var4 = this.ibzfxic.get(var3);
         var1.writeDouble(var4.v5suI);
         var1.writeDouble(var4.ibzfxic);
         var1.writeDouble(var4.wuhB);
         var1.writeDouble(var4.dOfFwvo);
         var1.writeDouble(var4.rsDd50tfV);
      }
   }

   public static FlightRoute ibzfxic(FriendlyByteBuf var0) {
      TerminalMode var1 = TerminalMode.v5suI(var0.readByte());
      int var2 = Math.min(var0.readVarInt(), 64);
      ArrayList var3 = new ArrayList(Math.max(0, var2));

      for (int var4 = 0; var4 < var2; var4++) {
         var3.add(new Waypoint(var0.readDouble(), var0.readDouble(), var0.readDouble(), var0.readDouble(), var0.readDouble()));
      }

      return new FlightRoute(var3, var1);
   }

   public CompoundTag uSUb() {
      CompoundTag var1 = new CompoundTag();
      var1.putByte("Terminal", (byte)this.wuhB.ordinal());
      ListTag var2 = new ListTag();

      for (Waypoint var4 : this.ibzfxic) {
         CompoundTag var5 = new CompoundTag();
         var5.putDouble("X", var4.v5suI);
         var5.putDouble("Y", var4.ibzfxic);
         var5.putDouble("Z", var4.wuhB);
         var5.putDouble("LoiterRadius", var4.dOfFwvo);
         var5.putDouble("Speed", var4.rsDd50tfV);
         var2.add(var5);
      }

      var1.put("Waypoints", var2);
      return var1;
   }

   public static FlightRoute v5suI(CompoundTag var0) {
      TerminalMode var1 = TerminalMode.v5suI(var0.getByteOr("Terminal", (byte)0));
      ListTag var2 = var0.getListOrEmpty("Waypoints");
      ArrayList var3 = new ArrayList(var2.size());

      for (int var4 = 0; var4 < var2.size() && var4 < 64; var4++) {
         CompoundTag var5 = var2.getCompoundOrEmpty(var4);
         double var6 = var5.contains("LoiterRadius") ? var5.getDoubleOr("LoiterRadius", 0.0) : -1.0;
         double var8 = var5.contains("Speed") ? var5.getDoubleOr("Speed", 0.0) : RouteSpeed.CRUISE.dOfFwvo;
         var3.add(new Waypoint(var5.getDoubleOr("X", 0.0), var5.getDoubleOr("Y", 0.0), var5.getDoubleOr("Z", 0.0), var6, var8));
      }

      return new FlightRoute(var3, var1);
   }
}
