package com.github.wujichen158.ikakuji.util;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.player.EnvyPlayer;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.google.common.collect.*;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

public class GlobalKujiFactory {
    /**
     * Contains all kujis that aren't expired
     * a name -> GlobalData map
     */
    private static final BiMap<String, KujiObj.GlobalData> LOADED_GLOBAL_KUJI = HashBiMap.create();

    /**
     * Contains all kujis' names that are on going
     */
    private static final Set<String> ON_GOING_GLOBAL_KUJIS = Sets.newHashSet();

    /**
     * Contains all existed kujis' names in data folder
     */
    private static final Set<String> EXISTED_GLOBAL_KUJIS = Sets.newHashSet();


    private GlobalKujiFactory() {
        throw new UnsupportedOperationException("Static factory");
    }


    public static void create(String globalKujiName, KujiObj.GlobalData globalKujiData) {
        register(globalKujiName, globalKujiData);
        markExisted(globalKujiName);
        createGlobalKujiFile(globalKujiName, globalKujiData);
    }

    public static void register(String globalKujiName, KujiObj.GlobalData globalKujiData) {
        LOADED_GLOBAL_KUJI.put(globalKujiName, globalKujiData);
    }

    public static void markExisted(String globalKujiName) {
        EXISTED_GLOBAL_KUJIS.add(globalKujiName);
    }

    public static void updateGlobalKuji() {
        LocalDateTime now = LocalDateTime.now();

        // Update on going kujis. Use iterator to avoid potential updating danger
        Iterator<String> iterator = ON_GOING_GLOBAL_KUJIS.iterator();
        while (iterator.hasNext()) {
            String globalKujiName = iterator.next();
            if (hasGlobalKuji(globalKujiName)) {
                if (Optional.ofNullable(LOADED_GLOBAL_KUJI.get(globalKujiName).getEndDateTime())
                        .map(dateTime -> dateTime.isBefore(now)).orElse(false)) {
                    iterator.remove();
                    unregister(globalKujiName);
                }
            } else {
                iterator.remove();
            }
        }

        // Check and start kujis
        LOADED_GLOBAL_KUJI.forEach((globalKujiName, globalKujiData) -> {
            if (!ON_GOING_GLOBAL_KUJIS.contains(globalKujiName)) {
                if (globalKujiData.getStartDateTime().isBefore(now)
                        && globalKujiData.getEndDateTime().isAfter(now)) {
                    start(globalKujiName);
                }
            }
        });
    }

    public static void start(String globalKujiName) {
        ON_GOING_GLOBAL_KUJIS.add(globalKujiName);
        ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastMessage(
                MsgUtil.prefixedColorMsg(IkaKuji.getInstance().getLocale().getMessages().getGlobalKujiStartMsg()),
                ChatType.CHAT, Util.NIL_UUID
        );
    }

    // Don't write `end()` since it's done via iterator

    public static boolean forceStop(String globalKujiName) {
        if (hasGlobalKuji(globalKujiName)) {
            // Set end time to now
            KujiObj.GlobalData globalData = LOADED_GLOBAL_KUJI.get(globalKujiName);
            globalData.setEndTime(TimeUtil.parseDateTime(LocalDateTime.now()));
            updateGlobalDataFile(globalKujiName, globalData);

            // Remove the kuji from collections
            ON_GOING_GLOBAL_KUJIS.remove(globalKujiName);
            unregister(globalKujiName);

            return true;
        }
        return false;
    }

    public static KujiObj.GlobalData get(String globalKujiName) {
        return LOADED_GLOBAL_KUJI.get(globalKujiName);
    }

    public static String getName(KujiObj.GlobalData globalData) {
        return LOADED_GLOBAL_KUJI.inverse().get(globalData);
    }

    public static boolean isHolding(String globalKujiName) {
        return ON_GOING_GLOBAL_KUJIS.contains(globalKujiName);
    }

    public static boolean isExisted(String globalKujiName) {
        return EXISTED_GLOBAL_KUJIS.contains(globalKujiName);
    }

    public static void updateDrawn(KujiObj.GlobalData globalData, UUID playerUuid, String playerName,
                                   int rewardIndex, String rewardId, boolean isLast) {
        // Win time needs to be more precise, so use another format
        globalData.updateData(rewardIndex, playerUuid, playerName, rewardId, TimeUtil.formatDateTime(LocalDateTime.now()));
        globalData.addDrawnCount();
        // Write last shot index updating here to avoid extra file update
        if (isLast) {
            globalData.setLastShotIndex(rewardIndex);
        }
        updateGlobalDataFile(getName(globalData), globalData);
    }

    public static boolean hasGlobalKuji(String globalKujiName) {
        return LOADED_GLOBAL_KUJI.containsKey(globalKujiName);
    }

    public static List<String> getOngoingGloballyKuji() {
        return Lists.newArrayList(ON_GOING_GLOBAL_KUJIS);
    }

    public static Map<String, KujiObj.GlobalData> getAll() {
        return LOADED_GLOBAL_KUJI;
    }

    public static void unregister(String globalKujiName) {
        LOADED_GLOBAL_KUJI.remove(globalKujiName);
    }

    public static void clear() {
        LOADED_GLOBAL_KUJI.clear();
    }

    public static void createGlobalKujiFile(String globalKujiName, KujiObj.GlobalData globalKujiData) {
        UtilConcurrency.runAsync(() -> {
            String globalKujiFileName = globalKujiName + Reference.YAML_SUFFIX;
            Path playerFile = Paths.get(Reference.DATA_PATH).resolve(globalKujiFileName);
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(playerFile)
                    .build();
            ConfigurationNode rootNode;
            try {
                // Must not exist here, so no extra judgment is needed
                rootNode = loader.createNode();
                // Write to file
                updateGlobalDataFileWithNode(globalKujiData, loader, rootNode);
            } catch (ConfigurateException ignored) {
                IkaKuji.LOGGER.warn("Global Kuji data file " + globalKujiFileName + " has something wrong, please have a check.");
            }
        });
    }

    private static void updateGlobalDataFile(String globalKujiName, KujiObj.GlobalData globalKujiData) {
        UtilConcurrency.runAsync(() -> {
            String globalKujiFileName = globalKujiName + Reference.YAML_SUFFIX;
            Path globalKujiFile = Paths.get(Reference.GLOBAL_DATA_PATH).resolve(globalKujiFileName);
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(globalKujiFile)
                    .build();
            ConfigurationNode rootNode;
            try {
                if (Files.exists(globalKujiFile)) {
                    // Read from file
                    rootNode = loader.load();
                } else {
                    rootNode = loader.createNode();
                }
                // Write to file
                updateGlobalDataFileWithNode(globalKujiData, loader, rootNode);
            } catch (ConfigurateException ignored) {
                IkaKuji.LOGGER.warn("Global Kuji data file " + globalKujiFileName + " has something wrong, please have a check.");
            }
        });
    }

    private static void updateGlobalDataFileWithNode(KujiObj.GlobalData globalKujiData, YamlConfigurationLoader loader, ConfigurationNode node) throws ConfigurateException {
        node.set(KujiObj.GlobalData.class, globalKujiData);
        loader.save(node);
    }
}
