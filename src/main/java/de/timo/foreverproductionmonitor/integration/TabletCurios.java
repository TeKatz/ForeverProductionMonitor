/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package de.timo.foreverproductionmonitor.integration;

import de.timo.foreverproductionmonitor.ForeverProductionMonitor;
import de.timo.foreverproductionmonitor.ModContent;
import de.timo.foreverproductionmonitor.item.ProductionTabletItem;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class TabletCurios {
    public static final String SLOT_ID = "production_tablet";
    private static final Method GET_CURIOS_INVENTORY;
    private static final Method FIND_FIRST_CURIO;
    private static final Method SLOT_RESULT_STACK;

    private TabletCurios() {
    }

    public static Optional<ItemStack> findEquippedTablet(LivingEntity entity) {
        try {
            Optional inventory = (Optional)GET_CURIOS_INVENTORY.invoke(null, entity);
            if (inventory.isEmpty()) {
                return Optional.empty();
            }
            Predicate<ItemStack> predicate = stack -> stack.is((Item)ModContent.PRODUCTION_TABLET.get());
            Optional result = (Optional)FIND_FIRST_CURIO.invoke(inventory.get(), predicate);
            return result.map(slot -> {
                try {
                    return (ItemStack)SLOT_RESULT_STACK.invoke(slot, new Object[0]);
                }
                catch (ReflectiveOperationException exception) {
                    ForeverProductionMonitor.LOGGER.error("Could not read the equipped Production Tablet", (Throwable)exception);
                    return ItemStack.EMPTY;
                }
            }).filter(stack -> !stack.isEmpty());
        }
        catch (ReflectiveOperationException exception) {
            ForeverProductionMonitor.LOGGER.error("Could not access the Curios inventory", (Throwable)exception);
            return Optional.empty();
        }
    }

    public static Optional<ProductionTabletItem.MonitorLink> findEquippedLink(LivingEntity entity) {
        return TabletCurios.findEquippedTablet(entity).flatMap(ProductionTabletItem::getLink);
    }

    static {
        try {
            Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Class<?> handler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
            Class<?> slotResult = Class.forName("top.theillusivec4.curios.api.SlotResult");
            GET_CURIOS_INVENTORY = curiosApi.getMethod("getCuriosInventory", LivingEntity.class);
            FIND_FIRST_CURIO = handler.getMethod("findFirstCurio", Predicate.class);
            SLOT_RESULT_STACK = slotResult.getMethod("stack", new Class[0]);
        }
        catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}

