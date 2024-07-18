package com.github.wujichen158.ikakuji.kuji;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.config.ConfigSound;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.text.Placeholder;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.executor.PreviewExecutor;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.lib.Placeholders;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KujiExecutor {
    public static void playSound(ConfigSound winSound, PlayerEntity player) {
        Optional.ofNullable(winSound).ifPresent(sound -> sound.playSound((ServerPlayerEntity) player));
    }

    public static Placeholder[] genAmountPlaceholder(int rewardDrawn, int rewardTotal) {
        return new Placeholder[]{
                Placeholder.simple(Placeholders.REWARD_DRAWN, String.valueOf(rewardDrawn)),
                Placeholder.simple(Placeholders.REWARD_REMAIN, String.valueOf(rewardTotal - rewardDrawn)),
                Placeholder.simple(Placeholders.REWARD_TOTAL, String.valueOf(rewardTotal))
        };
    }

    /**
     * Do these things:
     * <p>
     * 1. Give the reward
     * </p><p>
     * 2. Give last shot
     * </p><p>
     * 3. Save cache and file
     * </p>
     *
     * @param player
     * @param finalReward
     * @param playerDrawn
     * @param crate
     */
    public static void rewardPostProcess(ForgeEnvyPlayer player, KujiObj.Reward finalReward, List<String> playerDrawn, KujiObj.Crate crate) {
        finalReward.give(player);

        boolean isLast = processLastThings(player, playerDrawn, crate);
        if (IkaKuji.getInstance().getLocale().getLogs().getEnable()) {
            logRes(player, crate, finalReward, isLast);
        }
    }

    /**
     * Process multiple reward cases, which is jumped animation and uses greedy strategy
     *
     * @param player
     * @param finalRewards
     * @param playerDrawn
     * @param crate
     */
    public static void rewardPostProcess(ForgeEnvyPlayer player, List<KujiObj.Reward> finalRewards, List<String> playerDrawn, KujiObj.Crate crate) {
        finalRewards.forEach(reward -> reward.give(player));

        boolean isLast = processLastThings(player, playerDrawn, crate);
        if (IkaKuji.getInstance().getLocale().getLogs().getEnable()) {
            logRes(player, crate, finalRewards, isLast);
        }
    }

    public static boolean globalPostProcess(ForgeEnvyPlayer player, KujiObj.Reward finalReward,
                                         KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, int rewardIndex) {
        finalReward.give(player);

        boolean isLast = processLastThingsGlobal(player, globalKujiData, crate);
        GlobalKujiFactory.updateDrawn(globalKujiData, rewardIndex,
                player.getUniqueId(), player.getName(),
                finalReward.getId());

        if (IkaKuji.getInstance().getLocale().getLogs().getEnable()) {
            logGlobalRes(player, globalKujiData, finalReward, isLast);
        }

        return isLast;
    }

    private static boolean processLastThings(ForgeEnvyPlayer player, List<String> playerDrawn, KujiObj.Crate crate) {
        boolean isLast = false;
        if (isFullDrawn(playerDrawn, crate)) {
            isLast = true;
            deliverLast(player, crate);
        }
        PlayerKujiFactory.updatePlayerDrawn(playerDrawn, player.getUniqueId(), crate.getDisplayName());
        return isLast;
    }

    private static boolean processLastThingsGlobal(ForgeEnvyPlayer player,
                                                   KujiObj.GlobalData globalKujiData, KujiObj.Crate crate) {
        boolean isLast = false;
        if (isGlobalFullDrawn(globalKujiData)) {
            isLast = true;
            deliverLast(player, crate);
        }
        return isLast;
    }

    /**
     * Give last shot if present
     *
     * @param player
     * @param crate
     */
    private static void deliverLast(ForgeEnvyPlayer player, KujiObj.Crate crate) {
        Optional.ofNullable(player.getParent()).ifPresent(playerEntity -> {
            if (calRemainInvSize(playerEntity) == 0) {
                playerEntity.sendMessage(MsgUtil.prefixedColorMsg(IkaKuji.getInstance().getLocale().getMessages().getLastRewardFailMsg(), crate.getDisplayName()), playerEntity.getUUID());
            } else {
                Optional.ofNullable(crate.getLastShot()).ifPresent(lastShot -> lastShot.give(player));
            }
        });
    }


    /**
     * Log one draw to file
     *
     * @param player
     * @param crate
     * @param finalReward
     * @param isLast
     */
    private static void logRes(ForgeEnvyPlayer player, KujiObj.Crate crate, KujiObj.Reward finalReward, boolean isLast) {
        UtilConcurrency.runAsync(() -> {
            IkaKujiLocaleCfg.Logs logs = IkaKuji.getInstance().getLocale().getLogs();
            LocalDateTime now = LocalDateTime.now();
            StringBuilder builder = new StringBuilder(String.format("%s-%s-%s %02d:%02d:%02d: %s",
                    now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                    now.getHour(), now.getMinute(), now.getSecond(),
                    logs.getWinRewardLog()
                            .replace(Placeholders.PLAYER_NAME, player.getName())
                            .replace(Placeholders.CRATE_NAME, crate.getDisplayName())
                            .replace(Placeholders.REWARD_ID, finalReward.getId())
                            .replace(Placeholders.REWARD_NAME, finalReward.getDisplayItem().getName())
            ));
            checkLastAndLog(logs, builder, isLast);
        });
    }

    /**
     * Log one global draw to file
     *
     * @param player
     * @param globalData
     * @param finalReward
     * @param isLast
     */
    private static void logGlobalRes(ForgeEnvyPlayer player, KujiObj.GlobalData globalData, KujiObj.Reward finalReward, boolean isLast) {
        UtilConcurrency.runAsync(() -> {
            IkaKujiLocaleCfg.Logs logs = IkaKuji.getInstance().getLocale().getLogs();
            LocalDateTime now = LocalDateTime.now();
            StringBuilder builder = new StringBuilder(String.format("%s-%s-%s %02d:%02d:%02d: %s",
                    now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                    now.getHour(), now.getMinute(), now.getSecond(),
                    logs.getWinGlobalRewardLog()
                            .replace(Placeholders.PLAYER_NAME, player.getName())
                            .replace(Placeholders.GLOBAL_KUJI_NAME, GlobalKujiFactory.getName(globalData))
                            .replace(Placeholders.REWARD_ID, finalReward.getId())
                            .replace(Placeholders.REWARD_NAME, finalReward.getDisplayItem().getName())
            ));
            checkLastAndLog(logs, builder, isLast);
        });
    }

    /**
     * Log multi draws to file
     *
     * @param player
     * @param crate
     * @param finalRewards
     * @param isLast
     */
    private static void logRes(ForgeEnvyPlayer player, KujiObj.Crate crate, List<KujiObj.Reward> finalRewards, boolean isLast) {
        UtilConcurrency.runAsync(() -> {
            IkaKujiLocaleCfg.Logs logs = IkaKuji.getInstance().getLocale().getLogs();
            LocalDateTime now = LocalDateTime.now();
            StringBuilder builder = new StringBuilder();
            int finalRewardsSize = finalRewards.size();
            for (int i = 0; i < finalRewardsSize; i++) {
                builder.append(String.format("%s-%s-%s %02d:%02d:%02d: %s",
                        now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                        now.getHour(), now.getMinute(), now.getSecond(),
                        logs.getWinRewardLog()
                                .replace(Placeholders.PLAYER_NAME, player.getName())
                                .replace(Placeholders.CRATE_NAME, crate.getDisplayName())
                                .replace(Placeholders.REWARD_ID, finalRewards.get(i).getId())
                                .replace(Placeholders.REWARD_NAME, finalRewards.get(i).getDisplayItem().getName())
                ));
                if (i < finalRewardsSize - 1) {
                    builder.append("\n");
                }
            }
            checkLastAndLog(logs, builder, isLast);
        });
    }

    private static void checkLastAndLog(IkaKujiLocaleCfg.Logs logs, StringBuilder builder, boolean isLast) {
        if (isLast) {
            builder.append(logs.getLastShotLog());
        }
        String logEntry = builder.append("\n").toString();
        try {
            Files.write(Paths.get(Reference.LOG_PATH), logEntry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            IkaKuji.LOGGER.error(e.toString());
        }
    }

    /**
     * Check whether the player has drawn all rewards in specified crate
     * <p>
     * Though we can just judge the size of the 2 list,
     * but this entire checking is more reliable
     * </p>
     *
     * @param playerDrawn
     * @param crate
     * @return
     */
    public static boolean isFullDrawn(List<String> playerDrawn, KujiObj.Crate crate) {
        Map<String, Integer> playerDrawnMap = playerDrawn.stream().collect(Collectors.toMap(key -> key, value -> 1, Integer::sum));
        Map<String, Integer> rewardMap = crate.getRewardAmountMapLazy();
        return playerDrawnMap.equals(rewardMap);
    }

    /**
     * Check whether the specified global kuji has been fully drawn
     *
     * @param globalKujiData
     * @return
     */
    public static boolean isGlobalFullDrawn(KujiObj.GlobalData globalKujiData) {
        return globalKujiData.getDrawnCount() == globalKujiData.getData().size();
    }

    /**
     * Calculate intersect of player drawn list and corresponding crate.
     * This is able to retain the min number of duplicate elements,
     * and will try its best to keep the order of the player drawn list
     *
     * @param playerDrawn
     * @param crate
     * @return
     */
    public static List<String> calIntersect(List<String> playerDrawn, KujiObj.Crate crate) {
        Map<String, Integer> rewardMap = Maps.newHashMap(crate.getRewardAmountMapLazy());
        Iterator<String> iterator = playerDrawn.iterator();
        while (iterator.hasNext()) {
            String rewardName = iterator.next();
            Integer amount = rewardMap.get(rewardName);
            if (Optional.ofNullable(amount).isPresent() && amount > 0) {
                rewardMap.put(rewardName, amount - 1);
            } else {
                iterator.remove();
            }
        }
        return playerDrawn;
    }

    public static ItemStack addRewardLore(KujiObj.Reward reward, int rewardCount, double weight) {
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

        List<ITextComponent> loreLines = Lists.newArrayList(
                MsgUtil.colorMsg(messages.getDash()),
                MsgUtil.colorMsg(messages.getRewardRemainCount(), rewardCount)
        );
        if (reward.isShowProbInPreview()) {
            loreLines.add(MsgUtil.colorMsg(messages.getProbPerReward(), weight));
        }

        return addLore(reward, loreLines);
    }

    public static ItemStack addWinnerLore(KujiObj.Reward reward, String winnerName, String formattedWinTime) {
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

        List<ITextComponent> loreLines = Lists.newArrayList(
                MsgUtil.colorMsg(messages.getDash()),
                MsgUtil.colorMsg(messages.getWinnerName(), winnerName),
                MsgUtil.colorMsg(messages.getWinTime(), formattedWinTime)
        );

        return addLore(reward, loreLines);
    }

    private static ItemStack addLore(KujiObj.Reward reward, List<ITextComponent> loreLines) {
        ItemStack itemStack = UtilConfigItem.fromConfigItem(reward.getDisplayItem());
        CompoundNBT display = itemStack.getOrCreateTagElement("display");
        ListNBT currentLore = display.getList("Lore", 8);
        loreLines.forEach(line -> {
            currentLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(line)));
        });
        display.put("Lore", currentLore);
        itemStack.addTagElement("display", display);
        return itemStack;
    }


    /**
     * Regular open outside
     *
     * @param event
     * @param crate
     * @param itemCrate
     */
    public static void executeKujiLogic(PlayerInteractEvent event, KujiObj.Crate crate, boolean itemCrate) {
        PlayerEntity player = event.getPlayer();
        ItemStack itemStack = event.getItemStack();
        AtomicInteger minCount = new AtomicInteger(-1);
        int crateCountRequired = 1;
        if (itemCrate) {
            crateCountRequired = CrateFactory.getItemCrateCountRequired(itemStack);
            if (crateCountRequired > itemStack.getCount()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(IkaKuji.getInstance().getLocale().getMessages().getNeedItemCrateMsg(), crateCountRequired, itemStack.getItem().getName(itemStack).getString()), player.getUUID());
                return;
            }
            minCount.set(itemStack.getCount() / crateCountRequired);
        }

        if (executeKujiLogic(player, crate, minCount)) {
            //Consume crate
            if (!player.isCreative() && crate.isConsumeCrate() && itemCrate) {
                event.getItemStack().shrink((crate.isJumpAnimation() ? minCount.get() : 1) * crateCountRequired);
            }
        }
    }

    /**
     * Logic of opening. Open cmd use this method
     *
     * @param player
     * @param crate
     * @param minCount
     * @return
     */
    public static boolean executeKujiLogic(PlayerEntity player, KujiObj.Crate crate, AtomicInteger minCount) {
        ForgeEnvyPlayer envyPlayer = IkaKuji.getInstance().getPlayerManager().getPlayer((ServerPlayerEntity) player);
        if (!PlayerKujiFactory.hasPlayer(envyPlayer.getUniqueId())) {
            return false;
        }

        Map<String, List<String>> playerKujiData = PlayerKujiFactory.get(envyPlayer.getUniqueId()).getKujiData();
        String crateName = crate.getDisplayName();
        List<String> playerDrawn = playerKujiData.getOrDefault(crateName, Lists.newArrayList());
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

        // Preview
        if (player.isShiftKeyDown()) {
            PreviewExecutor.preview(player, crate, crateName, messages, playerDrawn, envyPlayer, 1);
            return false;
        }

        // Open
        // Perm check
        if (!IkaKuji.getInstance().getCommandFactory().hasPermission(player, PermissionNodes.getOpenPermNode(crateName))) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoOpenPermMsg(), crateName), player.getUUID());
            return false;
        }

        // Pre-crates check
        List<String> preCrates = crate.getPreCrates();
        if (Optional.ofNullable(preCrates).isPresent()) {
            List<String> incompleteCrates = Lists.newArrayList();
            for (String preCrateName : preCrates) {
                Optional.ofNullable(playerKujiData.get(preCrateName)).ifPresent(preCratePlayerDrawn -> {
                    Optional.ofNullable(CrateFactory.get(preCrateName)).ifPresent(preCrate -> {
                        if (!isFullDrawn(preCratePlayerDrawn, preCrate)) {
                            incompleteCrates.add(preCrateName);
                        }
                    });
                });
            }
            if (!incompleteCrates.isEmpty()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getIncompletePreKujiMsg(), incompleteCrates), player.getUUID());
                return false;
            }
        }

        // Check full
        if (KujiExecutor.isFullDrawn(playerDrawn, crate)) {
            if (crate.isOneRound()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getOneRoundMsg()), player.getUUID());
                return false;
            } else {
                // Clear here can avoid pre-checking issue
                playerDrawn.clear();
            }
        }


        // Generate available rewards list and total weight
        List<Pair<KujiObj.Reward, Double>> availableRewards = Lists.newArrayList();
        double totalWeight = getWeightWithAvailableRewards(availableRewards, playerDrawn, crate);

        // Check and cal available inventory size
        int invSize = 0;
        if (crate.isCheckInvBefore()) {
            invSize = calRemainInvSize(player);
            if (invSize == 0) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getInsufficientInvSizeMsg()), player.getUUID());
                return false;
            }
        }

        List<KujiObj.Reward> rewards;
        if (crate.isJumpAnimation()) {
            // Min availableReward size, limitPerDraw, inventory size, crate count and key count
            int crateCount = minCount.get();
            int limitPerDraw = crate.getLimitPerDraw();
            int times = Stream.of(availableRewards.size(),
                            crateCount != -1 ? crateCount : Integer.MAX_VALUE,
                            invSize > 0 ? invSize : Integer.MAX_VALUE,
                            limitPerDraw > 0 ? limitPerDraw : Integer.MAX_VALUE)
                    .min(Integer::compare)
                    .orElse(0);

            //Take item options
            //Check and take keys
            int keyCount = checkAndTakeKeys(crate.getKey(), crate.isConsumeKey(), player, times);
            if (keyCount == -1) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNeedKeyMsg(), crate.getKey().getAmount(), crate.getKey().getName()), player.getUUID());
                return false;
            } else if (keyCount > 0) {
                times = keyCount;
            }
            minCount.set(times);

            // Generate rewards must execute last
            rewards = genRandomRewards(availableRewards, totalWeight, playerDrawn, times);
            if (rewards.isEmpty()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoAvailableRwdMsg()), player.getUUID());
                return false;
            }

            rewardPostProcess(envyPlayer, rewards, playerDrawn, crate);
        } else {
            //TODO: Async?

            //Take item options
            //Check and take key
            if (!checkAndTakeKey(crate.getKey(), crate.isConsumeKey(), player)) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNeedKeyMsg(), crate.getKey().getAmount(), crate.getKey().getName()), player.getUUID());
                return false;
            }

            // Generate rewards must execute last
            rewards = genRandomRewards(availableRewards, totalWeight, playerDrawn);
            if (rewards.isEmpty()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoAvailableRwdMsg()), player.getUUID());
                return false;
            }

            KujiGuiManager.open(crate, envyPlayer, playerDrawn, rewards);
        }
        return true;
    }

    /**
     * Logic of opening globally
     *
     * @param page
     * @param globalKujiData
     * @param crate
     * @param player
     */
    public static void executeGlobalKujiLogic(int page, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, PlayerEntity player) {
        ForgeEnvyPlayer envyPlayer = IkaKuji.getInstance().getPlayerManager().getPlayer((ServerPlayerEntity) player);
        KujiGuiManager.openGlobal(page, globalKujiData, crate, envyPlayer);
    }

    private static int calRemainInvSize(PlayerEntity player) {
        int invSize = 0;
        for (ItemStack itemstack : player.inventory.items) {
            if (itemstack.isEmpty()) {
                invSize++;
            }
        }
        return invSize;
    }

    public static boolean checkAndTakeKey(ExtendedConfigItem crateKey, boolean isConsumeKey, PlayerEntity player) {
        if (Optional.ofNullable(crateKey).isPresent()) {
            for (ItemStack invItem : player.inventory.items) {
                if (ItemUtil.equalsWithPureTag(invItem, UtilConfigItem.fromConfigItem(crateKey))) {
                    //Consume key
                    if (!player.isCreative()) {
                        if (crateKey.getAmount() <= invItem.getCount()) {
                            if (isConsumeKey) {
                                invItem.shrink(crateKey.getAmount());
                            }
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private static int checkAndTakeKeys(ExtendedConfigItem crateKey, boolean isConsumeKey, PlayerEntity player, int minCount) {
        if (Optional.ofNullable(crateKey).isPresent()) {
            for (ItemStack invItem : player.inventory.items) {
                if (ItemUtil.equalsWithPureTag(invItem, UtilConfigItem.fromConfigItem(crateKey))) {
                    //Consume keys
                    if (!player.isCreative()) {
                        if (crateKey.getAmount() <= invItem.getCount()) {
                            int maxKeyCount = invItem.getCount() / crateKey.getAmount();
                            minCount = Math.min(minCount, maxKeyCount);
                            if (isConsumeKey) {
                                invItem.shrink(crateKey.getAmount() * minCount);
                            }
                            return minCount;
                        } else {
                            return -1;
                        }
                    }
                    return 0;
                }
            }
            return -1;
        }
        return 0;
    }

    public static double getWeightWithAvailableRewards(List<Pair<KujiObj.Reward, Double>> availableRewards, List<String> playerDrawn, KujiObj.Crate crate) {
        Map<String, Long> drawnMap = playerDrawn.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        double weightRes = 0;
        for (KujiObj.Reward reward : crate.getRewards()) {
            int availableAmount = getAvailableAmount(reward.getAmountPerKuji(), drawnMap.get(reward.getId()));
            double weightPerReward = reward.calWeightPerReward(getCurrentWeightOverride(crate, playerDrawn.size() + 1));
            weightRes += weightPerReward * availableAmount;
            for (int i = 0; i < availableAmount; i++) {
                availableRewards.add(new Pair<>(reward, weightPerReward));
            }
        }
        return weightRes;
    }

    public static double getWeightWithAvailableRewards(List<Pair<KujiObj.Reward, Double>> availableRewards,
                                                       KujiObj.GlobalData globalKujiData, KujiObj.Crate crate) {
        Map<String, Long> drawnMap = globalKujiData.getData().stream()
                .filter(globalDataEntry -> globalDataEntry.getRewardId() != null)
                .collect(Collectors.groupingBy(KujiObj.GlobalDataEntry::getRewardId, Collectors.counting()));
        double weightRes = 0;
        for (KujiObj.Reward reward : crate.getRewards()) {
            int availableAmount = getAvailableAmount(reward.getAmountPerKuji(), drawnMap.get(reward.getId()));
            double weightPerReward = reward.calWeightPerReward(getCurrentWeightOverride(crate,
                    globalKujiData.getDrawnCount() + 1));
            weightRes += weightPerReward * availableAmount;
            for (int i = 0; i < availableAmount; i++) {
                availableRewards.add(new Pair<>(reward, weightPerReward));
            }
        }
        return weightRes;
    }

    /**
     * Drawing is done draws.size() times, so currently is (draws.size() + 1)th drawing
     *
     * @param crate
     * @param th
     * @return
     */
    public static Map<String, Integer> getCurrentWeightOverride(KujiObj.Crate crate, int th) {
        return Optional.ofNullable(crate.getWeightOverrides())
                .map(weightOverrides -> weightOverrides.get(th))
                .orElse(null);
    }

    /**
     * Generate a reward list with the final reward in the first elem
     *
     * @param availableRewards
     * @param totalWeight
     * @param playerDrawn
     * @return
     */
    private static List<KujiObj.Reward> genRandomRewards(List<Pair<KujiObj.Reward, Double>> availableRewards, double totalWeight, List<String> playerDrawn) {
        List<KujiObj.Reward> rewards = Lists.newArrayList();
        double randomWeight = Reference.RANDOM.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;
        for (Pair<KujiObj.Reward, Double> weightedReward : availableRewards) {
            double weight = weightedReward.getSecond();
            if (cumulativeWeight < randomWeight && cumulativeWeight + weight >= randomWeight) {
                KujiObj.Reward reward = weightedReward.getFirst();
                playerDrawn.add(reward.getId());
                rewards.add(0, reward);
            } else {
                rewards.add(weightedReward.getFirst());
            }
            cumulativeWeight += weight;
        }
        return rewards;
    }

    /**
     * Generate a reward list which has times size
     *
     * @param availableRewards
     * @param totalWeight
     * @param playerDrawn
     * @param times
     * @return
     */
    private static List<KujiObj.Reward> genRandomRewards(List<Pair<KujiObj.Reward, Double>> availableRewards, double totalWeight, List<String> playerDrawn, int times) {
        List<KujiObj.Reward> rewards = Lists.newArrayList();
        for (int i = 0; i < times; i++) {
            double randomWeight = Reference.RANDOM.nextDouble() * totalWeight;
            double cumulativeWeight = 0.0;
            for (Pair<KujiObj.Reward, Double> weightedReward : availableRewards) {
                double weight = weightedReward.getSecond();
                // This condition guarantee the last reward would be delivered
                if (cumulativeWeight < randomWeight && cumulativeWeight + weight >= randomWeight) {
                    KujiObj.Reward reward = weightedReward.getFirst();
                    playerDrawn.add(reward.getId());
                    rewards.add(reward);
                    break;
                }
                cumulativeWeight += weight;
            }
        }
        return rewards;
    }

    /**
     * Generate one reward for global kuji
     *
     * @return
     */
    public static KujiObj.Reward genRandomReward(KujiObj.GlobalData globalKujiData, KujiObj.Crate crate) {
        List<Pair<KujiObj.Reward, Double>> availableRewards = Lists.newArrayList();
        double totalWeight = KujiExecutor.getWeightWithAvailableRewards(availableRewards, globalKujiData, crate);

        double randomWeight = Reference.RANDOM.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;
        for (Pair<KujiObj.Reward, Double> weightedReward : availableRewards) {
            double weight = weightedReward.getSecond();
            if (cumulativeWeight < randomWeight && cumulativeWeight + weight >= randomWeight) {
                return weightedReward.getFirst();
            }
            cumulativeWeight += weight;
        }
        return null;
    }

    public static int getAvailableAmount(int amountPerKuji, Long drawnAmount) {
        if (Optional.ofNullable(drawnAmount).isPresent() && drawnAmount <= amountPerKuji) {
            amountPerKuji -= drawnAmount;
        }
        return amountPerKuji;
    }
}
