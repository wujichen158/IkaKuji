package com.github.wujichen158.ikakuji.util;

import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.world.UtilWorld;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.KujiCrateType;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CrateFactory {

    private static final Map<String, KujiObj.Crate> LOADED_CRATES = Maps.newHashMap();
    private static final Map<KujiCrateType.ItemWrapper, Pair<String, Integer>> ITEM_CRATE_MAP = Maps.newHashMap();
    private static final Map<String, List<ItemStack>> CRATE_ITEMS_MAP = Maps.newHashMap();
    private static final Map<String, Map<Triple<Integer, Integer, Integer>, String>> WORLD_POS_CRATE_MAP = Maps.newHashMap();
    private static final Map<String, String> ENTITY_CRATE_MAP = Maps.newHashMap();
    private static List<String> crateNameList;

    private CrateFactory() {
        throw new UnsupportedOperationException("Static factory");
    }

    private static void registerItemCrate(ItemStack itemStack, String crateName) {
        KujiCrateType.ItemWrapper itemWrapper = new KujiCrateType.ItemWrapper(itemStack);
        ITEM_CRATE_MAP.put(itemWrapper, new Pair<>(crateName, itemStack.getCount()));
        CRATE_ITEMS_MAP.computeIfAbsent(crateName, key -> Lists.newArrayList()).add(itemStack);
    }

    private static void registerWorldPosCrate(KujiCrateType.PositionCrate positionCrate, String crateName) {
        //TODO: End and Nether's name are NONE
        WORLD_POS_CRATE_MAP.computeIfAbsent(positionCrate.getWorld(), k -> Maps.newHashMap()).put(
                Triple.of(positionCrate.getX(), positionCrate.getY(), positionCrate.getZ()), crateName);
    }

    private static void registerEntityCrate(String entityName, String crateName) {
        ENTITY_CRATE_MAP.put(entityName, crateName);
    }

    private static void registerResponseCrate(ConfigurationNode node, KujiObj.Crate crate) {
        String crateName = crate.getDisplayName();
        switch (crate.getCrateType()) {
            case item:
                try {
                    Optional.ofNullable(node.node("type-data").getList(ExtendedConfigItem.class)).ifPresent(extendedConfigItems -> {
                        extendedConfigItems.forEach(extendedConfigItem ->
                                CrateFactory.registerItemCrate(UtilConfigItem.fromConfigItem(extendedConfigItem), crateName));
                    });
                } catch (SerializationException ignored) {
                    IkaKuji.LOGGER.warn(String.format("Item Kuji info %s has something wrong, please have a check.", crateName));
                }
                break;
            case position:
                try {
                    Optional.ofNullable(node.node("type-data").getList(KujiCrateType.PositionCrate.class)).ifPresent(positions ->
                            positions.forEach(position ->
                                    CrateFactory.registerWorldPosCrate(position, crateName)));
                } catch (SerializationException ignored) {
                    IkaKuji.LOGGER.warn(String.format("Position Kuji info %s has something wrong, please have a check.", crateName));
                }
                break;
            case entity:
                try {
                    Optional.ofNullable(node.node("type-data").getList(KujiCrateType.EntityCrate.class)).ifPresent(entities ->
                            entities.forEach(entity ->
                                    CrateFactory.registerEntityCrate(entity.getName(), crateName)));
                } catch (SerializationException ignored) {
                    IkaKuji.LOGGER.warn(String.format("Entity Kuji info %s has something wrong, please have a check.", crateName));
                }
                break;
            default:
        }
    }

    public static int getItemCrateCountRequired(ItemStack itemStack) {
        return Optional.ofNullable(ITEM_CRATE_MAP.get(new KujiCrateType.ItemWrapper(itemStack))).map(Pair::getSecond).orElse(1);
    }

    public static KujiObj.Crate tryGetItemCrate(ItemStack itemStack) {
        return LOADED_CRATES.getOrDefault(Optional.ofNullable(ITEM_CRATE_MAP.get(new KujiCrateType.ItemWrapper(itemStack))).map(Pair::getFirst).orElse(null), null);
    }

    public static KujiObj.Crate tryGetWorldPosCrate(Level world, int x, int y, int z) {
        WORLD_POS_CRATE_MAP.get(UtilWorld.getName(world));
        return Optional.ofNullable(WORLD_POS_CRATE_MAP.get(UtilWorld.getName(world)))
                .map(map -> map.get(Triple.of(x, y, z)))
                .map(LOADED_CRATES::get)
                .orElse(null);
    }

    public static KujiObj.Crate tryGetEntityCrate(String entityName) {
        return LOADED_CRATES.getOrDefault(ENTITY_CRATE_MAP.getOrDefault(entityName, null), null);
    }

    public static void register(ConfigurationNode node, KujiObj.Crate crate) {
        LOADED_CRATES.put(crate.getDisplayName(), crate);
        registerResponseCrate(node, crate);
    }

    public static KujiObj.Crate get(String crateName) {
        return LOADED_CRATES.get(crateName);
    }

    public static Map<String, KujiObj.Crate> getAll() {
        return LOADED_CRATES;
    }

    public static void clear() {
        LOADED_CRATES.clear();

        ITEM_CRATE_MAP.clear();
        CRATE_ITEMS_MAP.clear();
        WORLD_POS_CRATE_MAP.clear();
        ENTITY_CRATE_MAP.clear();
    }

    public static void updateAllRegisteredNames() {
        crateNameList = new ArrayList<>(LOADED_CRATES.keySet());
    }

    public static List<String> getAllRegisteredNames() {
        return crateNameList;
    }

    public static List<ItemStack> getItemsFromName(String crateName) {
        return CRATE_ITEMS_MAP.get(crateName);
    }
}