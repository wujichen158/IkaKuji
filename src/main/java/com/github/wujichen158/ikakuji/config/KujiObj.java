package com.github.wujichen158.ikakuji.config;

import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.config.ConfigSound;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.server.UtilForgeServer;
import com.github.wujichen158.ikakuji.kuji.EnumCrateType;
import com.github.wujichen158.ikakuji.kuji.gui.EnumGuiPattern;
import com.github.wujichen158.ikakuji.lib.Placeholders;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.*;
import java.util.stream.Collectors;

public class KujiObj {
    @ConfigSerializable
    public static class Crate {
        private String displayName;
        private EnumCrateType crateType;
        private List<Map<String, String>> typeData;
        private ExtendedConfigItem key;
        private List<Reward> rewards;
        private transient Map<String, Integer> rewardAmountMap;
        private transient List<String> rewardNames;
        private transient Integer rewardTotal;

        private ConfigInterface previewGuiSettings;
        private ConfigInterface displayGuiSettings;
        private List<Integer> previewSlots = Lists.newArrayList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);
        private List<Integer> displaySlots = Lists.newArrayList(10, 11, 12, 13, 14, 15, 16);

        private ExtendedConfigItem previewNextPage;
        private ExtendedConfigItem previewPreviousPage;
        private ExtendedConfigItem placeholderButton;

        private EnumGuiPattern guiChangePattern;
        /**
         * Times are all in second
         */
        private Double initialDelay = 2d;
        private Double repeatDelay = 1d;
        private Double spinDuration = 10d;
        private Integer finalRewardPosition = 22;
        private ConfigSound rollSound;

        private ExtendedConfigItem coverItem;
        private ExtendedConfigItem casinoMarkLeft;
        private ExtendedConfigItem casinoMarkRight;

        private Integer oneDrawLimit = 0;
        private Reward lastShot;
        private Boolean oneRound = true;
        private double chance = 100d;
        private Reward consolationReward;
        private List<String> preCrates;

        public String getDisplayName() {
            return displayName;
        }

        public EnumCrateType getCrateType() {
            return crateType;
        }

        public List<Map<String, String>> getTypeData() {
            return typeData;
        }

        public ExtendedConfigItem getKey() {
            return key;
        }

        public List<Reward> getRewards() {
            return rewards;
        }

        public Long calTotalWeight() {
            return rewards.stream().mapToLong(reward -> reward.totalWeight).sum();
        }

        // This reward map will be refreshed (i.e., set to null) after the Reward obj is updated
        public Map<String, Integer> getRewardAmountMapLazy() {
            if (Optional.ofNullable(rewardAmountMap).isEmpty()) {
                rewardAmountMap = rewards.stream()
                        .collect(Collectors.toMap(KujiObj.Reward::getId, KujiObj.Reward::getAmountPerKuji));
            }
            return rewardAmountMap;
        }

        public List<String> getRewardNamesLazy() {
            if (Optional.ofNullable(rewardNames).isEmpty()) {
                rewardNames = rewards.stream()
                        .flatMap(reward -> Collections.nCopies(reward.getAmountPerKuji(), reward.getId()).stream())
                        .collect(Collectors.toList());
            }
            return rewardNames;
        }

        public Integer getRewardTotalLazy() {
            if (Optional.ofNullable(rewardTotal).isEmpty()) {
                rewardTotal = rewards.stream().mapToInt(Reward::getAmountPerKuji).sum();
            }
            return rewardTotal;
        }

        public ConfigInterface getPreviewGuiSettings() {
            return previewGuiSettings;
        }

        public ConfigInterface getDisplayGuiSettings() {
            return displayGuiSettings;
        }

        public List<Integer> getPreviewSlots() {
            return previewSlots;
        }

        public List<Integer> getDisplaySlots() {
            return displaySlots;
        }

        public ExtendedConfigItem getPreviewNextPage() {
            return previewNextPage;
        }

        public ExtendedConfigItem getPreviewPreviousPage() {
            return previewPreviousPage;
        }

        public ExtendedConfigItem getPlaceholderButton() {
            return placeholderButton;
        }

        public Integer getFinalRewardPosition() {
            return finalRewardPosition;
        }


        public EnumGuiPattern getGuiChangePattern() {
            return guiChangePattern;
        }

        public Double getInitialDelay() {
            return initialDelay;
        }

        public Double getRepeatDelay() {
            return repeatDelay;
        }

        public Double getSpinDuration() {
            return spinDuration;
        }

        public ConfigSound getRollSound() {
            return rollSound;
        }

        public ExtendedConfigItem getCoverItem() {
            return coverItem;
        }

        public ExtendedConfigItem getCasinoMarkLeft() {
            return casinoMarkLeft;
        }

        public ExtendedConfigItem getCasinoMarkRight() {
            return casinoMarkRight;
        }

        public Integer getOneDrawLimit() {
            return oneDrawLimit;
        }

        public Reward getLastShot() {
            return lastShot;
        }

        public Boolean getOneRound() {
            return oneRound;
        }

        public double getChance() {
            return chance;
        }

        public Reward getConsolationReward() {
            return consolationReward;
        }

        public List<String> getPreCrates() {
            return preCrates;
        }
    }

    @ConfigSerializable
    public static class Reward {
        private String id;
        private List<String> commands;
        private Integer amountPerKuji;
        private ExtendedConfigItem displayItem;

        private ConfigSound winSound;

        private Integer totalWeight = 1;

        private Boolean canPreview = true;

        public String getId() {
            return id;
        }

        public List<String> getCommands() {
            return commands;
        }

        public Integer getAmountPerKuji() {
            return amountPerKuji;
        }

        public ExtendedConfigItem getDisplayItem() {
            return displayItem;
        }

        public ConfigSound getWinSound() {
            return winSound;
        }

        public Integer getTotalWeight() {
            return totalWeight;
        }

        public Boolean getCanPreview() {
            return canPreview;
        }

        public void give(ForgeEnvyPlayer player) {
            for (String command : this.commands) {
                UtilForgeServer.executeCommand(command.replace(Placeholders.PLAYER_NAME, player.getName()));
            }
        }

        @Override
        public int hashCode() {
            return this.getId().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Reward) {
                return ((Reward) obj).getId().equals(this.getId());
            }
            return false;
        }
    }

    @ConfigSerializable
    public static class PlayerData {
        private Map<String, List<String>> kujiData;

        public PlayerData() {
            super();
        }

        public PlayerData(Map<String, List<String>> kujiData) {
            super();
            this.kujiData = kujiData;
        }

        public Map<String, List<String>> getKujiData() {
            return kujiData;
        }
    }
}
