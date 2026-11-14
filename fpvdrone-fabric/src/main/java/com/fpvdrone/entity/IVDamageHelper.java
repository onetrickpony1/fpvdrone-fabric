package com.fpvdrone.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class IVDamageHelper {
    private static boolean reflectionFailed = false;
    private static boolean reflectionInitialized = false;
    private static Class<?> builderClass;
    private static Field entityField;
    private static Class<?> multipartClass;
    private static Class<?> aJSONInteractableEntityClass;
    private static Field definitionField;
    private static Field generalField;
    private static Field generalHealthField;
    private static Field damageVarField;
    private static Field outOfHealthField;
    private static Field computedVariableCurrentValueField;
    private static Method computedVariableSetToMethod;
    private static Constructor<?> packetVariableIncrement3Ctor;
    private static Constructor<?> packetVariableSetCtor;
    private static Field interfaceManagerPacketField;
    private static Method sendToAllClientsMethod;

    private IVDamageHelper() {
    }

    private static void initReflection() {
        if (!reflectionInitialized) {
            reflectionInitialized = true;

            try {
                builderClass = Class.forName("mcinterface1201.BuilderEntityExisting");
                entityField = builderClass.getDeclaredField("entity");
                entityField.setAccessible(true);
                multipartClass = Class.forName("minecrafttransportsimulator.entities.components.AEntityF_Multipart");
                aJSONInteractableEntityClass = Class.forName("minecrafttransportsimulator.jsondefs.AJSONInteractableEntity");
                Class<?> aJSONItemClass = Class.forName("minecrafttransportsimulator.jsondefs.AJSONItem");
                generalField = aJSONItemClass.getDeclaredField("general");
                generalField.setAccessible(true);
                Class<?> aJSONItemGeneralClass = Class.forName("minecrafttransportsimulator.jsondefs.AJSONItem$General");
                generalHealthField = aJSONItemGeneralClass.getDeclaredField("health");
                generalHealthField.setAccessible(true);
                Class<?> aEntityDDefinableClass = Class.forName("minecrafttransportsimulator.entities.components.AEntityD_Definable");
                definitionField = aEntityDDefinableClass.getDeclaredField("definition");
                definitionField.setAccessible(true);
                Class<?> interactableClass = Class.forName("minecrafttransportsimulator.entities.components.AEntityE_Interactable");
                damageVarField = interactableClass.getDeclaredField("damageVar");
                damageVarField.setAccessible(true);
                outOfHealthField = interactableClass.getDeclaredField("outOfHealth");
                outOfHealthField.setAccessible(true);
                Class<?> computedVariableClass = Class.forName("minecrafttransportsimulator.baseclasses.ComputedVariable");
                computedVariableCurrentValueField = computedVariableClass.getDeclaredField("currentValue");
                computedVariableCurrentValueField.setAccessible(true);
                computedVariableSetToMethod = computedVariableClass.getMethod("setTo", double.class, boolean.class);
                Class<?> packetIncrClass = Class.forName("minecrafttransportsimulator.packets.instances.PacketEntityVariableIncrement");
                packetVariableIncrement3Ctor = packetIncrClass.getConstructor(computedVariableClass, double.class, double.class, double.class);
                Class<?> packetSetClass = Class.forName("minecrafttransportsimulator.packets.instances.PacketEntityVariableSet");
                packetVariableSetCtor = packetSetClass.getConstructor(computedVariableClass, double.class);
                Class<?> interfaceManagerClass = Class.forName("minecrafttransportsimulator.mcinterface.InterfaceManager");
                interfaceManagerPacketField = interfaceManagerClass.getDeclaredField("packetInterface");
                interfaceManagerPacketField.setAccessible(true);
                Class<?> iInterfacePacketClass = Class.forName("minecrafttransportsimulator.mcinterface.IInterfacePacket");
                Class<?> aPacketBaseClass = Class.forName("minecrafttransportsimulator.packets.components.APacketBase");
                sendToAllClientsMethod = iInterfacePacketClass.getMethod("sendToAllClients", aPacketBaseClass);
            } catch (ClassNotFoundException var10) {
                reflectionFailed = true;
            } catch (Exception var11) {
                reflectionFailed = true;
            }
        }
    }

    public static boolean isIVAvailable() {
        initReflection();
        return !reflectionFailed;
    }

    public static boolean isIVEntity(Entity entity) {
        initReflection();
        return !reflectionFailed && builderClass.isInstance(entity);
    }

    public static boolean dealRawHpDamage(Entity ivEntity, float amount) {
        initReflection();
        if (reflectionFailed) {
            return false;
        } else {
            try {
                Object internalEntity = entityField.get(ivEntity);
                if (internalEntity == null || !multipartClass.isInstance(internalEntity)) {
                    return false;
                } else if ((Boolean)outOfHealthField.get(internalEntity)) {
                    return false;
                } else {
                    Object damageVar = damageVarField.get(internalEntity);
                    Object definition = definitionField.get(internalEntity);
                    if (definition != null && aJSONInteractableEntityClass.isInstance(definition)) {
                        Object general = generalField.get(definition);
                        int maxHealth = (Integer)generalHealthField.get(general);
                        if (maxHealth == 0) {
                            return false;
                        } else {
                            double currentDamage = (Double)computedVariableCurrentValueField.get(damageVar);
                            double newDamage = currentDamage + amount;
                            Object packetInterface = interfaceManagerPacketField.get(null);
                            if (newDamage >= maxHealth) {
                                newDamage = maxHealth;
                                Object packet = packetVariableSetCtor.newInstance(damageVar, (double)maxHealth);
                                sendToAllClientsMethod.invoke(packetInterface, packet);
                            } else {
                                Object packet = packetVariableIncrement3Ctor.newInstance(damageVar, (double)amount, 0.0, (double)maxHealth);
                                sendToAllClientsMethod.invoke(packetInterface, packet);
                            }

                            computedVariableSetToMethod.invoke(damageVar, newDamage, false);
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
            } catch (Exception var13) {
                return false;
            }
        }
    }

    public static List<Entity> findIVEntities(Level level, AABB area) {
        initReflection();
        return reflectionFailed ? List.of() : level.getEntities((Entity)null, area, e -> e.isAlive() && builderClass.isInstance(e));
    }
}
