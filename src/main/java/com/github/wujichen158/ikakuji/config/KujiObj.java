package com.github.wujichen158.ikakuji.config;

import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.server.UtilForgeServer;
import com.github.wujichen158.ikakuji.config.envynew.ConfigSound;
import com.github.wujichen158.ikakuji.kuji.EnumCrateType;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.kuji.gui.EnumGuiPattern;
import com.github.wujichen158.ikakuji.lib.Placeholders;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class KujiObj {
    @ConfigSerializable
    public static class Crate {
        private String displayName;
        private EnumCrateType crateType;
        private ExtendedConfigItem key;
        private List<Reward> rewards;
        private transient Map<String, Integer> rewardAmountMap;
        private transient List<String> rewardNames;
        private transient Integer rewardTotal;

        private boolean jumpAnimation = false;
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
        private double initialDelay = 2d;
        private double repeatDelay = 1d;
        private double spinDuration = 10d;
        private int finalRewardPosition = 22;
        private ConfigSound rollSound;

        private ExtendedConfigItem coverItem;
        private List<ExtendedConfigItem> indicators;

        private int limitPerDraw = 0;
        private Reward lastShot;
        private boolean oneRound = true;
        private boolean checkInvBefore = true;
        private double chance = 100d;
        private Reward consolationReward;
        private Map<Integer, Map<String, Integer>> weightOverrides;
        private List<String> preCrates;

        public String getDisplayName() {
            return displayName;
        }

        public EnumCrateType getCrateType() {
            return crateType;
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
            if (!Optional.ofNullable(rewardAmountMap).isPresent()) {
                rewardAmountMap = rewards.stream()
                        .collect(Collectors.toMap(KujiObj.Reward::getId, KujiObj.Reward::getAmountPerKuji));
            }
            return rewardAmountMap;
        }

        public List<String> getRewardNamesLazy() {
            if (!Optional.ofNullable(rewardNames).isPresent()) {
                rewardNames = rewards.stream()
                        .flatMap(reward -> Collections.nCopies(reward.getAmountPerKuji(), reward.getId()).stream())
                        .collect(Collectors.toList());
            }
            return rewardNames;
        }

        public Integer getRewardTotalLazy() {
            if (!Optional.ofNullable(rewardTotal).isPresent()) {
                rewardTotal = rewards.stream().mapToInt(Reward::getAmountPerKuji).sum();
            }
            return rewardTotal;
        }

        public boolean isJumpAnimation() {
            return jumpAnimation;
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

        public int getFinalRewardPosition() {
            return finalRewardPosition;
        }


        public EnumGuiPattern getGuiChangePattern() {
            return guiChangePattern;
        }

        public double getInitialDelay() {
            return initialDelay;
        }

        public double getRepeatDelay() {
            return repeatDelay;
        }

        public double getSpinDuration() {
            return spinDuration;
        }

        public ConfigSound getRollSound() {
            return rollSound;
        }

        public ExtendedConfigItem getCoverItem() {
            return coverItem;
        }

        public List<ExtendedConfigItem> getIndicators() {
            return indicators;
        }

        public int getLimitPerDraw() {
            return limitPerDraw;
        }

        public Reward getLastShot() {
            return lastShot;
        }

        public boolean isOneRound() {
            return oneRound;
        }

        public boolean isCheckInvBefore() {
            return checkInvBefore;
        }

        public double getChance() {
            return chance;
        }

        public Reward getConsolationReward() {
            return consolationReward;
        }

        public Map<Integer, Map<String, Integer>> getWeightOverrides() {
            return weightOverrides;
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

        public List<String> getAvailableCrates() {
            List<String> availableCrates = Lists.newArrayList();
            kujiData.forEach((crateName, drawnList) -> {
                Optional.ofNullable(CrateFactory.get(crateName)).ifPresent(crate -> {
                    if (!KujiExecutor.isFullDrawn(drawnList, crate)) {
                        availableCrates.add(crateName);
                    }
                });
            });
            return availableCrates;
        }
    }
}
