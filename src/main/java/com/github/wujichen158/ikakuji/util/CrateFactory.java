package com.github.wujichen158.ikakuji.util;

import com.envyful.api.forge.world.UtilWorld;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.CrateTypeObj;
import com.github.wujichen158.ikakuji.config.IkaKujiObj;
import com.google.common.collect.Maps;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class CrateFactory {

    private static final Map<String, IkaKujiObj.Crate> LOADED_CRATES = Maps.newHashMap();


    private static final Map<String, String> ITEM_CRATE_MAP = Maps.newHashMap();
    private static final Map<String, Map<Triple<Integer, Integer, Integer>, String>> WORLD_POS_CRATE_MAP = Maps.newHashMap();
    private static final Map<String, String> ENTITY_CRATE_MAP = Maps.newHashMap();

    //TODO: Currently only support item name

    private static void registerItemCrate(String itemName, String crateName) {
        ITEM_CRATE_MAP.put(itemName, crateName);
    }

    private static void registerWorldPosCrate(CrateTypeObj.PositionCrate positionCrate, String crateName) {
        //TODO: End and Nether's name are NONE
        WORLD_POS_CRATE_MAP.computeIfAbsent(positionCrate.getWorld(), k -> Maps.newHashMap()).put(
                Triple.of(positionCrate.getX(), positionCrate.getY(), positionCrate.getZ()), crateName);
    }

    private static void registerEntityCrate(String entityName, String crateName) {
        ENTITY_CRATE_MAP.put(entityName, crateName);
    }

    private static void registerResponseCrate(ConfigurationNode node, IkaKujiObj.Crate crate) {
        String crateName = crate.getDisplayName();
        switch (crate.getCrateType()) {
            case item:
                crate.getTypeData().forEach(elem -> {
                    boolean valid = false;
                    for (String k : elem.keySet()) {
                        if ("type".equals(k)) {
                            CrateFactory.registerItemCrate(elem.get(k), crateName);
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) {
                        IkaKuji.LOGGER.warn(String.format("Item Kuji info %s has something wrong, please have a check.", crateName));
                    }
                });
                break;
            case position:
                try {
                    Optional.ofNullable(node.node("type-data").getList(CrateTypeObj.PositionCrate.class)).ifPresent(positions ->
                            positions.forEach(position ->
                                    CrateFactory.registerWorldPosCrate(position, crateName)));
                } catch (SerializationException ignored) {
                    IkaKuji.LOGGER.warn(String.format("Position Kuji info %s has something wrong, please have a check.", crateName));
                }
                break;
            case entity:
                crate.getTypeData().forEach(elem -> {
                    boolean valid = false;
                    for (String k : elem.keySet()) {
                        if ("type".equals(k)) {
                            CrateFactory.registerEntityCrate(elem.get(k), crateName);
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) {
                        IkaKuji.LOGGER.warn(String.format("Entity Kuji info %s has something wrong, please have a check.", crateName));
                    }
                });
                break;
            default:
        }
    }

    public static IkaKujiObj.Crate tryGetItemCrate(String itemName) {
        return LOADED_CRATES.getOrDefault(ITEM_CRATE_MAP.getOrDefault(itemName, null), null);
    }

    public static IkaKujiObj.Crate tryGetWorldPosCrate(World world, int x, int y, int z) {
        WORLD_POS_CRATE_MAP.get(UtilWorld.getName(world));
        return Optional.ofNullable(WORLD_POS_CRATE_MAP.get(UtilWorld.getName(world)))
                .map(map -> map.get(Triple.of(x, y, z)))
                .map(LOADED_CRATES::get)
                .orElse(null);
    }

    public static IkaKujiObj.Crate tryGetEntityCrate(String entityName) {
        return LOADED_CRATES.getOrDefault(ENTITY_CRATE_MAP.getOrDefault(entityName, null), null);
    }

    private CrateFactory() {
        throw new UnsupportedOperationException("Static factory");
    }

    public static void register(ConfigurationNode node, IkaKujiObj.Crate crate) {
        LOADED_CRATES.put(crate.getDisplayName(), crate);
        registerResponseCrate(node, crate);
    }

    public static void clear() {
        LOADED_CRATES.clear();
        ITEM_CRATE_MAP.clear();
        WORLD_POS_CRATE_MAP.clear();
        ENTITY_CRATE_MAP.clear();
    }

    public static IkaKujiObj.Crate get(String crateName) {
        return LOADED_CRATES.get(crateName.toLowerCase(Locale.ROOT));
    }

    public static Map<String, IkaKujiObj.Crate> getAll() {
        return LOADED_CRATES;
    }
}