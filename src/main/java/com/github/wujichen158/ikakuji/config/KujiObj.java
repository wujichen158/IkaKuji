package com.github.wujichen158.ikakuji.config;

import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.config.ConfigSound;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.server.UtilForgeServer;
import com.github.wujichen158.ikakuji.kuji.EnumCrateType;
import com.github.wujichen158.ikakuji.kuji.EnumGlobalRewardRule;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.kuji.gui.EnumGuiPattern;
import com.github.wujichen158.ikakuji.lib.Placeholders;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.TimeUtil;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class KujiObj {
    @ConfigSerializable
    public static class Crate {
        private String displayName;
        private EnumCrateType crateType;
        private boolean consumeCrate = true;
        private ExtendedConfigItem key;
        private boolean consumeKey = true;
        private List<Reward> rewards;
        private transient Map<String, Integer> rewardAmountMap;
        private transient Map<String, Reward> rewardMap;
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
        private ExtendedConfigItem coverItem;
        /**
         * Times are all in second
         */
        private double initialDelay = 2d;
        private double repeatDelay = 1d;
        private double spinDuration = 10d;
        private int finalRewardPosition = 22;
        private ConfigSound rollSound;
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

        public boolean isConsumeCrate() {
            return consumeCrate;
        }

        public ExtendedConfigItem getKey() {
            return key;
        }

        public boolean isConsumeKey() {
            return consumeKey;
        }

        public List<Reward> getRewards() {
            return rewards;
        }

        public Long calTotalWeight() {
            return rewards.stream().mapToLong(reward -> reward.totalWeight).sum();
        }

        // This reward map will be refreshed (i.e., set to null) after the Crate obj is reloaded from yml
        public Map<String, Integer> getRewardAmountMapLazy() {
            if (Optional.ofNullable(rewardAmountMap).isEmpty()) {
                rewardAmountMap = rewards.stream()
                        .collect(Collectors.toMap(KujiObj.Reward::getId, KujiObj.Reward::getAmountPerKuji));
            }
            return rewardAmountMap;
        }

        public Map<String, Reward> getRewardMapLazy() {
            if (Optional.ofNullable(rewardMap).isEmpty()) {
                rewardMap = rewards.stream()
                        .collect(Collectors.toMap(KujiObj.Reward::getId, reward -> reward));
            }
            return rewardMap;
        }

        public Integer getRewardTotalLazy() {
            if (Optional.ofNullable(rewardTotal).isEmpty()) {
                rewardTotal = rewards.stream().mapToInt(Reward::getAmountPerKuji).sum();
            }
            return rewardTotal;
        }

        /**
         * Check whether the player has drawn all rewards in specified crate
         * <p>
         * Though we can just judge the size of the 2 list,
         * but this entire checking is more reliable
         * </p>
         *
         * @param playerDrawn
         * @return
         */
        public boolean isFullDrawn(List<String> playerDrawn) {
            return playerDrawn.stream().collect(Collectors.toMap(key -> key, value -> 1, Integer::sum))
                    .equals(getRewardAmountMapLazy());
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

        public ExtendedConfigItem getCoverItem() {
            return coverItem;
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

        private int totalWeight = 1;
        private transient Double weightPerReward;

        private boolean canPreview = true;

        private boolean showProbInPreview = false;

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

        public int getTotalWeight() {
            return totalWeight;
        }

        public double calWeightPerReward(Map<String, Integer> weightOverride) {
            if (Optional.ofNullable(weightPerReward).isEmpty()) {
                this.weightPerReward = (double) Optional.ofNullable(weightOverride)
                        .map(weightOverrideMap -> weightOverrideMap.get(this.id))
                        .orElse(this.totalWeight) / this.amountPerKuji;
            }
            return this.weightPerReward;
        }

        public boolean isCanPreview() {
            return canPreview;
        }

        public boolean isShowProbInPreview() {
            return showProbInPreview;
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
                    if (!crate.isFullDrawn(drawnList)) {
                        availableCrates.add(crateName);
                    }
                });
            });
            return availableCrates;
        }
    }

    @ConfigSerializable
    public static class GlobalData {
        private String crateName;
        private EnumGlobalRewardRule rewardRule;
        private String startTime = "";
        private String endTime = "";
        private int lastShotIndex = -1;
        private transient LocalDateTime startDateTime;
        private transient LocalDateTime endDateTime;
        private transient int drawnCount;
        private List<KujiObj.GlobalDataEntry> data;

        public GlobalData() {
            super();
        }

        public GlobalData(String crateName, EnumGlobalRewardRule rewardRule, String startTime, String endTime) {
            this.crateName = crateName;
            this.rewardRule = rewardRule;
            this.startTime = startTime;
            this.endTime = endTime;
            this.data = Lists.newArrayList();
        }

        public String getCrateName() {
            return crateName;
        }

        public EnumGlobalRewardRule getRewardRule() {
            return rewardRule;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public int getLastShotIndex() {
            return lastShotIndex;
        }

        public LocalDateTime getStartDateTime() {
            return startDateTime;
        }

        public LocalDateTime getEndDateTime() {
            return endDateTime;
        }

        public int getDrawnCount() {
            return drawnCount;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public void setLastShotIndex(int lastShotIndex) {
            this.lastShotIndex = lastShotIndex;
        }

        public void setData(List<KujiObj.GlobalDataEntry> data) {
            this.data = data;
        }

        public void setEndDateTime(LocalDateTime endDateTime) {
            this.endDateTime = endDateTime;
        }

        public void setStartDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
        }

        public void setDrawnCount(int drawnCount) {
            this.drawnCount = drawnCount;
        }

        /**
         * Check whether this global kuji has been fully drawn.
         * Calculate in a more simple way than individual kuji.
         *
         * @return
         */
        public boolean isFullDrawn() {
            return this.drawnCount == this.data.size();
        }

        public void calDates() {
            this.startDateTime = Optional.ofNullable(TimeUtil.parseTimeString(this.startTime))
                    .orElse(LocalDateTime.now());
            this.endDateTime = TimeUtil.parseTimeString(this.endTime);
        }

        public List<KujiObj.GlobalDataEntry> getData() {
            return data;
        }

        public void updateData(int rewardIndex, UUID playerUuid, String playerName, String rewardId, String winTime) {
            this.data.get(rewardIndex).update(playerUuid, playerName, rewardId, winTime);
        }

        public void addDrawnCount() {
            this.drawnCount ++;
        }
    }

    @ConfigSerializable
    public static class GlobalDataEntry {
        private UUID playerUuid;
        private String playerName;
        private String rewardId;
        private String winTime;

        public GlobalDataEntry(String rewardId) {
            this.rewardId = rewardId;
        }

        public GlobalDataEntry() {}

        public UUID getPlayerUuid() {
            return playerUuid;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getRewardId() {
            return rewardId;
        }

        public String getWinTime() {
            return winTime;
        }

        /**
         * It'll update rewardId no matter what the original one is
         *
         * @param playerUuid
         * @param playerName
         * @param rewardId
         * @param winTime
         */
        public void update(UUID playerUuid, String playerName, String rewardId, String winTime) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.rewardId = rewardId;
            this.winTime = winTime;
        }

        /**
         * Regard `playerUuid != null` as player is not null
         */
        public boolean isSettled() {
            return this.playerUuid != null && this.winTime != null;
        }
    }
}
