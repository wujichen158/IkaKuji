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
     * Load all crates from data dir
     * Then update all registered names for cmd completion
     */
    public static void loadAllCrates() {
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
            IkaKuji.LOGGER.warn("Reading dir: " + Reference.CRATE_PATH + " failed, please have a check");
        }
    }
}
