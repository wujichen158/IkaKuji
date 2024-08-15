package com.github.wujichen158.ikakuji.util;

import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.lib.Reference;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class CfgPostProcessUtil {

    /**
     * Clear and load all crates from data dir
     * Then update all registered names for cmd completion
     */
    public static void loadAllCrates() {
        CrateFactory.clear();

        try (Stream<Path> paths = Files.walk(Paths.get(Reference.CRATE_PATH))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(Reference.YAML_SUFFIX))
                    .forEach(path -> {
                        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                                .path(path)
                                .build();
                        try {
                            ConfigurationNode node = loader.load();
                            Optional.ofNullable(node.get(KujiObj.Crate.class)).ifPresent(crate -> {
                                CrateFactory.register(node, crate);
                            });
                        } catch (ConfigurateException ignored) {
                            IkaKuji.LOGGER.warn("Crate " + path + " has something wrong, please have a check");
                        }
                    });
            // Update all registered names for cmd completion
            CrateFactory.updateAllRegisteredNames();
        } catch (IOException ignored) {
            IkaKuji.LOGGER.warn("Reading dir: " + Reference.CRATE_PATH + " failed, please have a check");
        }
    }

    public static void loadAllGlobalData() {
        GlobalKujiFactory.clear();

        try (Stream<Path> paths = Files.walk(Paths.get(Reference.GLOBAL_DATA_PATH))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(Reference.YAML_SUFFIX))
                    .forEach(path -> {
                        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                                .path(path)
                                .build();
                        try {
                            ConfigurationNode node = loader.load();
                            Optional.ofNullable(node.get(KujiObj.GlobalData.class)).ifPresent(globalData -> {
                                String globalKujiName = path.getFileName().toString().replace(Reference.YAML_SUFFIX, "");
                                // Only add to cache if it's not ended.
                                if (!TimeUtil.isEndTimePassed(globalData.getEndTime())) {
                                    // Calculate time to date to ensure fast processing
                                    globalData.calDates();
                                    // Calculate current drawn count
                                    globalData.setDrawnCount((int) globalData.getData().stream()
                                            .filter(KujiObj.GlobalDataEntry::isSettled)
                                            .count());
                                    // The last shot index is been persisted, don't need to recalculate here
                                    GlobalKujiFactory.register(globalKujiName, globalData);
                                }
                                GlobalKujiFactory.markExisted(globalKujiName);
                            });
                        } catch (ConfigurateException ignored) {
                            IkaKuji.LOGGER.warn("Global Kuji " + path + " has something wrong, please have a check");
                        }
                    });
        } catch (IOException ignored) {
            IkaKuji.LOGGER.warn("Reading dir: " + Reference.GLOBAL_DATA_PATH + " failed, please have a check");
        }
    }

    /**
     * Find if there're any abnormal data in player kujis when all crates are reloaded
     */
    public static void cleanUpCurrentKuji() {
        try (Stream<Path> paths = Files.walk(Paths.get(Reference.DATA_PATH))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(Reference.YAML_SUFFIX))
                    .forEach(path -> {
                        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                                .path(path)
                                .build();
                        try {
                            ConfigurationNode node = loader.load();
                            KujiObj.PlayerData playerData = node.get(KujiObj.PlayerData.class);
                            if (Optional.ofNullable(playerData).isPresent()) {
                                // refresh current caches if there're crate file updates
                                for (String crateName : playerData.getKujiData().keySet()) {
                                    KujiObj.Crate crate = CrateFactory.get(crateName);
                                    if (Optional.ofNullable(crate).isPresent()) {
                                        playerData.getKujiData().computeIfPresent(crateName, (key, oldList) -> KujiExecutor.calIntersect(oldList, crate));
                                    }
                                }

                                // Update cache and file
                                // TODO: May cause async issue when IO in same time. Claim in docs
                                PlayerKujiFactory.updateIfPresent(
                                        UUID.fromString(path.getFileName().toString().replace(Reference.YAML_SUFFIX, "")),
                                        playerData, loader, node);
                            }
                        } catch (ConfigurateException ignored) {
                            IkaKuji.LOGGER.warn("Player data " + path.getFileName() + " has something wrong or isn't a player data, please have a check");
                        }
                    });
        } catch (IOException ignored) {
            IkaKuji.LOGGER.warn("Reading dir: " + Reference.DATA_PATH + " failed, please have a check");
        }
    }
}
