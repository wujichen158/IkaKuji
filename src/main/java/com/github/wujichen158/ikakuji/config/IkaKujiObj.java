package com.github.wujichen158.ikakuji.config;

import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.config.ConfigSound;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.server.UtilForgeServer;
import com.github.wujichen158.ikakuji.kuji.EnumCrateType;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.*;
import java.util.stream.Collectors;

public class IkaKujiObj {
    @ConfigSerializable
    public static class Crate {
        private String displayName;
        private EnumCrateType crateType;
        private List<Map<String, String>> typeData;
        private ExtendedConfigItem key;
        private List<Reward> rewards;
        private transient Map<String, Integer> rewardAmountMap;
        private transient List<String> rewardNames;

        private ConfigInterface previewGuiSettings;
        private ConfigInterface displayGuiSettings;
        private List<Integer> previewSlots = Lists.newArrayList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);
        private List<Integer> displaySlots = Lists.newArrayList(10, 11, 12, 13, 14, 15, 16);

        private ExtendedConfigItem previewNextPage;
        private ExtendedConfigItem previewPreviousPage;

        private Integer initialDelay = 20;
        private Integer repeatDelay = 20;
        private Integer spinDuration = 5;
        private Integer finalRewardPosition = 22;
        private ConfigSound rollSound;

        private Integer oneDrawLimit = 0;
        private Reward lastShot;
        private Boolean oneRound = true;
        private double chance = 100d;
        private Reward consolationReward;
        private List<String> preCrates;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public EnumCrateType getCrateType() {
            return crateType;
        }

        public void setCrateType(EnumCrateType crateType) {
            this.crateType = crateType;
        }

        public List<Map<String, String>> getTypeData() {
            return typeData;
        }

        public ExtendedConfigItem getKey() {
            return key;
        }

        public void setTypeData(List<Map<String, String>> typeData) {
            this.typeData = typeData;
        }

        public void setKey(ExtendedConfigItem key) {
            this.key = key;
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
                        .collect(Collectors.toMap(IkaKujiObj.Reward::getId, IkaKujiObj.Reward::getAmountPerKuji));
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

        public void setRewards(List<Reward> rewards) {
            this.rewards = rewards;
        }

        public ConfigInterface getPreviewGuiSettings() {
            return previewGuiSettings;
        }

        public void setPreviewGuiSettings(ConfigInterface previewGuiSettings) {
            this.previewGuiSettings = previewGuiSettings;
        }

        public ConfigInterface getDisplayGuiSettings() {
            return displayGuiSettings;
        }

        public void setDisplayGuiSettings(ConfigInterface displayGuiSettings) {
            this.displayGuiSettings = displayGuiSettings;
        }

        public List<Integer> getPreviewSlots() {
            return previewSlots;
        }

        public void setPreviewSlots(List<Integer> previewSlots) {
            this.previewSlots = previewSlots;
        }

        public List<Integer> getDisplaySlots() {
            return displaySlots;
        }

        public void setDisplaySlots(List<Integer> displaySlots) {
            this.displaySlots = displaySlots;
        }

        public ExtendedConfigItem getPreviewNextPage() {
            return previewNextPage;
        }

        public void setPreviewNextPage(ExtendedConfigItem previewNextPage) {
            this.previewNextPage = previewNextPage;
        }

        public ExtendedConfigItem getPreviewPreviousPage() {
            return previewPreviousPage;
        }

        public void setPreviewPreviousPage(ExtendedConfigItem previewPreviousPage) {
            this.previewPreviousPage = previewPreviousPage;
        }

        public Integer getFinalRewardPosition() {
            return finalRewardPosition;
        }

        public void setFinalRewardPosition(Integer finalRewardPosition) {
            this.finalRewardPosition = finalRewardPosition;
        }

        public Integer getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(Integer initialDelay) {
            this.initialDelay = initialDelay;
        }

        public Integer getRepeatDelay() {
            return repeatDelay;
        }

        public void setRepeatDelay(Integer repeatDelay) {
            this.repeatDelay = repeatDelay;
        }

        public Integer getSpinDuration() {
            return spinDuration;
        }

        public void setSpinDuration(Integer spinDuration) {
            this.spinDuration = spinDuration;
        }

        public ConfigSound getRollSound() {
            return rollSound;
        }

        public void setRollSound(ConfigSound rollSound) {
            this.rollSound = rollSound;
        }

        public Integer getOneDrawLimit() {
            return oneDrawLimit;
        }

        public void setOneDrawLimit(Integer oneDrawLimit) {
            this.oneDrawLimit = oneDrawLimit;
        }

        public Reward getLastShot() {
            return lastShot;
        }

        public void setLastShot(Reward lastShot) {
            this.lastShot = lastShot;
        }

        public Boolean getOneRound() {
            return oneRound;
        }

        public void setOneRound(Boolean oneRound) {
            this.oneRound = oneRound;
        }

        public double getChance() {
            return chance;
        }

        public void setChance(double chance) {
            this.chance = chance;
        }

        public Reward getConsolationReward() {
            return consolationReward;
        }

        public void setConsolationReward(Reward consolationReward) {
            this.consolationReward = consolationReward;
        }

        public List<String> getPreCrates() {
            return preCrates;
        }

        public void setPreCrates(List<String> preCrates) {
            this.preCrates = preCrates;
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

        public void setId(String id) {
            this.id = id;
        }

        public List<String> getCommands() {
            return commands;
        }

        public void setCommands(List<String> commands) {
            this.commands = commands;
        }

        public Integer getAmountPerKuji() {
            return amountPerKuji;
        }

        public void setAmountPerKuji(Integer amountPerKuji) {
            this.amountPerKuji = amountPerKuji;
        }

        public ExtendedConfigItem getDisplayItem() {
            return displayItem;
        }

        public void setDisplayItem(ExtendedConfigItem displayItem) {
            this.displayItem = displayItem;
        }

        public ConfigSound getWinSound() {
            return winSound;
        }

        public void setWinSound(ConfigSound winSound) {
            this.winSound = winSound;
        }

        public Integer getTotalWeight() {
            return totalWeight;
        }

        public void setTotalWeight(Integer totalWeight) {
            this.totalWeight = totalWeight;
        }

        public Boolean getCanPreview() {
            return canPreview;
        }

        public void setCanPreview(Boolean canPreview) {
            this.canPreview = canPreview;
        }

        public void give(ForgeEnvyPlayer player) {
            for (String command : this.commands) {
                UtilForgeServer.executeCommand(command.replace("%player%", player.getName()));
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

        public void setKujiData(Map<String, List<String>> kujiData) {
            this.kujiData = kujiData;
        }
    }
}
