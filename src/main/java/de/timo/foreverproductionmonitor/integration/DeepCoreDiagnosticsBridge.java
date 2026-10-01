package de.timo.foreverproductionmonitor.integration;

import de.timo.foreverproductionmonitor.network.MonitorNetwork;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Optional, reflection-only adapter for ForeverDeepCore 0.7+ diagnostics.
 * This class intentionally has no compile-time Deep Core dependency.
 */
public final class DeepCoreDiagnosticsBridge {
    private DeepCoreDiagnosticsBridge() {
    }

    public static MonitorNetwork.DeepCoreDiagnostic inspect(ResourceLocation dimension,
                                                            BlockPos pos,
                                                            BlockEntity target,
                                                            String fallbackName,
                                                            MonitorNetwork.DeepCoreFacilityStatus availability) {
        if (target == null) {
            return MonitorNetwork.DeepCoreDiagnostic.unavailable(
                    dimension, pos, fallbackName, availability);
        }

        try {
            Object diagnostics = target.getClass().getMethod("diagnostics").invoke(target);
            if (diagnostics == null) {
                return MonitorNetwork.DeepCoreDiagnostic.unavailable(
                        dimension, pos, fallbackName, availability);
            }

            String name = stringValue(call(diagnostics, "facilityName"), fallbackName);
            String facilityState = enumName(call(diagnostics, "state"));
            boolean structureValid = booleanValue(call(diagnostics, "structureValid"));
            boolean subnetOnline = booleanValue(call(diagnostics, "subnetOnline"));
            int physicalTier = intValue(call(diagnostics, "physicalTier"));
            int surveyTier = intValue(call(diagnostics, "surveyTier"));
            int installedBores = intValue(call(diagnostics, "installedBores"));
            int runningBores = intValue(call(diagnostics, "runningBores"));
            int itemsLastMinute = intValue(call(diagnostics, "itemsLastMinute"));
            long totalProduced = longValue(call(diagnostics, "totalProduced"));
            double estimatedAePerMinute = doubleValue(call(diagnostics, "estimatedAePerMinute"));

            MonitorNetwork.DeepCoreStorageDiagnostic storage =
                    readStorage(call(diagnostics, "storage"));
            List<MonitorNetwork.DeepCoreBoreDiagnostic> bores =
                    readBores(call(diagnostics, "bores"));

            return new MonitorNetwork.DeepCoreDiagnostic(
                    dimension, pos, name, availability, true,
                    facilityState, structureValid, subnetOnline,
                    physicalTier, surveyTier, installedBores, runningBores,
                    itemsLastMinute, totalProduced, estimatedAePerMinute,
                    storage, bores);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return MonitorNetwork.DeepCoreDiagnostic.unavailable(
                    dimension, pos, fallbackName, availability);
        }
    }

    private static MonitorNetwork.DeepCoreStorageDiagnostic readStorage(Object storage)
            throws ReflectiveOperationException {
        if (storage == null) return MonitorNetwork.DeepCoreStorageDiagnostic.unknown();
        return new MonitorNetwork.DeepCoreStorageDiagnostic(
                enumName(call(storage, "state")),
                longValue(call(storage, "usedBytes")),
                longValue(call(storage, "totalBytes")),
                longValue(call(storage, "storedItems")),
                longValue(call(storage, "usedTypes")),
                longValue(call(storage, "totalTypes")),
                intValue(call(storage, "finiteCells")),
                intValue(call(storage, "infiniteCells")),
                intValue(call(storage, "unknownCells")));
    }

    private static List<MonitorNetwork.DeepCoreBoreDiagnostic> readBores(Object raw)
            throws ReflectiveOperationException {
        if (!(raw instanceof List<?> list)) return List.of();
        ArrayList<MonitorNetwork.DeepCoreBoreDiagnostic> result = new ArrayList<>();
        for (Object bore : list) {
            if (bore == null || result.size() >= 3) continue;
            Object resourceRaw = call(bore, "resource");
            ResourceLocation resource = resourceRaw instanceof ResourceLocation id ? id : null;
            result.add(new MonitorNetwork.DeepCoreBoreDiagnostic(
                    intValue(call(bore, "number")),
                    booleanValue(call(bore, "installed")),
                    enumName(call(bore, "state")),
                    enumName(call(bore, "stallReason")),
                    resource,
                    longValue(call(bore, "remaining")),
                    longValue(call(bore, "initialReserve")),
                    intValue(call(bore, "depth")),
                    intValue(call(bore, "purity")),
                    intValue(call(bore, "requiredTier")),
                    intValue(call(bore, "effectiveTier")),
                    intValue(call(bore, "progressPermille")),
                    intValue(call(bore, "effectiveIntervalTicks")),
                    doubleValue(call(bore, "aePerItem")),
                    intValue(call(bore, "itemsLastMinute")),
                    doubleValue(call(bore, "theoreticalItemsPerMinute")),
                    longValue(call(bore, "totalProduced")),
                    longValue(call(bore, "estimatedDepletionMinutes")),
                    doubleValue(call(bore, "precisionChance")),
                    readUpgrades(call(bore, "upgrades"))));
        }
        return List.copyOf(result);
    }

    private static List<MonitorNetwork.DeepCoreUpgradeDiagnostic> readUpgrades(Object raw)
            throws ReflectiveOperationException {
        if (!(raw instanceof List<?> list)) return List.of();
        ArrayList<MonitorNetwork.DeepCoreUpgradeDiagnostic> result = new ArrayList<>();
        for (Object upgrade : list) {
            if (upgrade == null || result.size() >= 3) continue;
            Object itemRaw = call(upgrade, "itemId");
            if (!(itemRaw instanceof ResourceLocation itemId)) continue;
            result.add(new MonitorNetwork.DeepCoreUpgradeDiagnostic(
                    itemId,
                    stringValue(call(upgrade, "family"), ""),
                    intValue(call(upgrade, "tier"))));
        }
        return List.copyOf(result);
    }

    private static Object call(Object target, String methodName) throws ReflectiveOperationException {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private static String enumName(Object value) {
        return value instanceof Enum<?> e ? e.name() : stringValue(value, "UNKNOWN");
    }

    private static String stringValue(Object value, String fallback) {
        return value instanceof String s ? s : value == null ? fallback : value.toString();
    }

    private static boolean booleanValue(Object value) {
        return value instanceof Boolean b && b;
    }

    private static int intValue(Object value) {
        return value instanceof Number n ? n.intValue() : 0;
    }

    private static long longValue(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }

    private static double doubleValue(Object value) {
        return value instanceof Number n ? n.doubleValue() : 0.0;
    }
}
