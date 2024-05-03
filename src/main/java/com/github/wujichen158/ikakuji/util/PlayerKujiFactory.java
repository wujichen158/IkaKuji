package com.github.wujichen158.ikakuji.util;

import com.envyful.api.concurrency.UtilConcurrency;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.google.common.collect.Maps;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerKujiFactory {
    private static final Map<UUID, KujiObj.PlayerData> LOADED_PLAYER_KUJI = Maps.newHashMap();

    private PlayerKujiFactory() {
        throw new UnsupportedOperationException("Static factory");
    }


    public static void register(UUID playerUuid) {
        Optional.ofNullable(getOrCreatePlayerDataFile(playerUuid)).ifPresent(playerData ->
                LOADED_PLAYER_KUJI.put(playerUuid, playerData));
    }

    public static void updateIfPresent(UUID playerUuid, KujiObj.PlayerData playerData, YamlConfigurationLoader loader, ConfigurationNode node) throws ConfigurateException {
        if (LOADED_PLAYER_KUJI.containsKey(playerUuid)) {
            LOADED_PLAYER_KUJI.put(playerUuid, playerData);
            updatePlayerDataFileWithNode(playerData, loader, node);
        }
    }

    public static KujiObj.PlayerData get(UUID playerUuid) {
        return LOADED_PLAYER_KUJI.get(playerUuid);
    }

    public static void updatePlayerDrawn(List<String> playerDrawn, UUID playerUuid, String crateName) {
        Optional.ofNullable(LOADED_PLAYER_KUJI.get(playerUuid)).ifPresent(playerData -> {
            playerData.getKujiData().put(crateName, playerDrawn);
            updatePlayerDataFile(playerUuid, playerData);
        });
    }

    public static boolean hasPlayer(UUID playerUuid) {
        return LOADED_PLAYER_KUJI.containsKey(playerUuid);
    }

    public static Map<UUID, KujiObj.PlayerData> getAll() {
        return LOADED_PLAYER_KUJI;
    }

    public static void unregister(UUID playerUuid) {
        LOADED_PLAYER_KUJI.remove(playerUuid);
    }

    public static void clear() {
        LOADED_PLAYER_KUJI.clear();
    }

    /**
     * To make sure this process is safe enough for data registration,
     * don't use async operation here
     *
     */
    private static KujiObj.PlayerData getOrCreatePlayerDataFile(UUID playerUuid) {
        String playerFileName = playerUuid + Reference.YAML_SUFFIX;
        Path playerFile = Paths.get(Reference.DATA_PATH).resolve(playerFileName);
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(playerFile)
                .build();
        ConfigurationNode rootNode;
        try {
            if (Files.exists(playerFile)) {
                // Read from file
                rootNode = loader.load();

                // Return cache
                return rootNode.get(KujiObj.PlayerData.class);
            } else {
                rootNode = loader.createNode();
                KujiObj.PlayerData newPlayerData = new KujiObj.PlayerData(Maps.newHashMap());

                // Write to file
                updatePlayerDataFileWithNode(newPlayerData, loader, rootNode);

                // Return cache
                return newPlayerData;
            }
        } catch (ConfigurateException ignored) {
            IkaKuji.LOGGER.warn("The data of player " + playerFileName + " has something wrong while creating/reading, please have a check.");
        }
        return null;
    }

    private static void updatePlayerDataFile(UUID playerUuid, KujiObj.PlayerData playerData) {
        UtilConcurrency.runAsync(() -> {
            String playerFileName = playerUuid + Reference.YAML_SUFFIX;
            Path playerFile = Paths.get(Reference.DATA_PATH).resolve(playerFileName);
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(playerFile)
                    .build();
            ConfigurationNode rootNode;
            try {
                if (Files.exists(playerFile)) {
                    // Read from file
                    rootNode = loader.load();
                } else {
                    rootNode = loader.createNode();
                }
                // Write to file
                updatePlayerDataFileWithNode(playerData, loader, rootNode);
            } catch (ConfigurateException ignored) {
                IkaKuji.LOGGER.warn("Player file " + playerFileName + " has something wrong, please have a check.");
            }
        });
    }

    private static void updatePlayerDataFileWithNode(KujiObj.PlayerData playerData, YamlConfigurationLoader loader, ConfigurationNode node) throws ConfigurateException {
        node.set(KujiObj.PlayerData.class, playerData);
        loader.save(node);
    }
}
