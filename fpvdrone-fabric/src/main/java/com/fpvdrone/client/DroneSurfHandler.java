package com.fpvdrone.client;

import com.fpvdrone.entity.DroneEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class DroneSurfHandler {
    private static final double MAX_CARRY_DIST_SQ = 100.0;
    private static final double FEET_SEARCH_BELOW = 0.25;
    private static final double FEET_ON_TOP_TOLERANCE = 0.15;
    private static final double STEP_UP_HEIGHT = 0.6;
    private static UUID lastSupportingDroneId = null;
    private static double lastDroneX;
    private static double lastDroneY;
    private static double lastDroneZ;

    public static void onClientTick() {
        {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player != null) {
                if (player.level().isClientSide()) {
                    // While the player flies a drone (or flies in creative) there is no riding. Forget the last drone
                    // stood on, or on landing back at the pilot spot the player would be "carried" by however far that
                    // drone moved during the flight (the original kept it and could fling the player into the air).
                    if (player.noPhysics || player.getAbilities().flying) {
                        lastSupportingDroneId = null;
                    }
                    if (!player.noPhysics) {
                        if (!player.getAbilities().flying) {
                            DroneEntity sticky = lastSupportingDroneId == null ? null : findDroneByUuid(player, lastSupportingDroneId);
                            if (sticky != null) {
                                double dx = sticky.getX() - lastDroneX;
                                double dy = sticky.getY() - lastDroneY;
                                double dz = sticky.getZ() - lastDroneZ;
                                double lenSq = dx * dx + dy * dy + dz * dz;
                                if (lenSq > 1.0E-8 && lenSq <= 100.0) {
                                    player.setPos(player.getX() + dx, player.getY() + dy, player.getZ() + dz);
                                }

                                lastDroneX = sticky.getX();
                                lastDroneY = sticky.getY();
                                lastDroneZ = sticky.getZ();
                            }

                            DroneEntity intersecting = findIntersectingDrone(player);
                            if (intersecting != null) {
                                resolveDroneIntersection(player, intersecting);
                            }

                            DroneEntity support = findSupportDrone(player);
                            if (support != null) {
                                UUID newId = support.getUUID();
                                if (lastSupportingDroneId == null || !lastSupportingDroneId.equals(newId)) {
                                    lastDroneX = support.getX();
                                    lastDroneY = support.getY();
                                    lastDroneZ = support.getZ();
                                }

                                lastSupportingDroneId = newId;
                                player.fallDistance = 0.0F;
                            } else {
                                lastSupportingDroneId = null;
                            }
                        }
                    }
                }
            }
        }
    }

    private static DroneEntity findDroneByUuid(Player player, UUID id) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        } else {
            for (Entity e : level.entitiesForRendering()) {
                if (e instanceof DroneEntity drone && !drone.isRemoved() && drone.canBeCollidedWith() && drone.getUUID().equals(id)) {
                    return drone;
                }
            }

            return null;
        }
    }

    private static DroneEntity findIntersectingDrone(Player player) {
        AABB pb = player.getBoundingBox();
        List<DroneEntity> candidates = player.level().getEntitiesOfClass(DroneEntity.class, pb, d -> !d.isRemoved() && d.canBeCollidedWith());
        DroneEntity best = null;
        double bestTop = Double.NEGATIVE_INFINITY;

        for (DroneEntity drone : candidates) {
            double droneTop = drone.getBoundingBox().maxY;
            if (droneTop > bestTop) {
                bestTop = droneTop;
                best = drone;
            }
        }

        return best;
    }

    private static void resolveDroneIntersection(Player player, DroneEntity drone) {
        AABB db = drone.getBoundingBox();
        AABB pb = player.getBoundingBox();
        double droneTop = db.maxY;
        double lift = droneTop - player.getY();
        if (lift > 0.0 && lift <= 0.6) {
            player.setPos(player.getX(), droneTop, player.getZ());
            player.fallDistance = 0.0F;
        } else {
            double overlapNegX = pb.maxX - db.minX;
            double overlapPosX = db.maxX - pb.minX;
            double overlapNegZ = pb.maxZ - db.minZ;
            double overlapPosZ = db.maxZ - pb.minZ;
            double minOverlap = Double.POSITIVE_INFINITY;
            double pushX = 0.0;
            double pushZ = 0.0;
            if (overlapNegX > 0.0 && overlapNegX < minOverlap) {
                minOverlap = overlapNegX;
                pushX = -overlapNegX;
                pushZ = 0.0;
            }

            if (overlapPosX > 0.0 && overlapPosX < minOverlap) {
                minOverlap = overlapPosX;
                pushX = overlapPosX;
                pushZ = 0.0;
            }

            if (overlapNegZ > 0.0 && overlapNegZ < minOverlap) {
                minOverlap = overlapNegZ;
                pushX = 0.0;
                pushZ = -overlapNegZ;
            }

            if (overlapPosZ > 0.0 && overlapPosZ < minOverlap) {
                minOverlap = overlapPosZ;
                pushX = 0.0;
                pushZ = overlapPosZ;
            }

            if (Double.isFinite(minOverlap) && minOverlap > 0.0) {
                double eps = 0.001;
                player.setPos(player.getX() + pushX + Math.signum(pushX) * eps, player.getY(), player.getZ() + pushZ + Math.signum(pushZ) * eps);
            }
        }
    }

    private static DroneEntity findSupportDrone(Player player) {
        double py = player.getY();
        AABB searchBox = new AABB(
            player.getBoundingBox().minX, py - 0.25, player.getBoundingBox().minZ, player.getBoundingBox().maxX, py + 0.15, player.getBoundingBox().maxZ
        );
        List<DroneEntity> candidates = player.level().getEntitiesOfClass(DroneEntity.class, searchBox, d -> !d.isRemoved() && d.canBeCollidedWith());
        DroneEntity best = null;
        double bestTop = Double.NEGATIVE_INFINITY;

        for (DroneEntity drone : candidates) {
            double droneTop = drone.getBoundingBox().maxY;
            if (!(droneTop < py - 0.25) && !(droneTop > py + 0.15) && droneTop > bestTop) {
                bestTop = droneTop;
                best = drone;
            }
        }

        return best;
    }
}
