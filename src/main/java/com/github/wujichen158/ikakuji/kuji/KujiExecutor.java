package com.github.wujichen158.ikakuji.kuji;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.gui.Transformer;
import com.envyful.api.type.Pair;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.config.envynew.AmountPlaceholder;
import com.github.wujichen158.ikakuji.config.envynew.ConfigSound;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.lib.Placeholders;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
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
    public static void playSound(ConfigSound winSound, EntityPlayer player) {
        Optional.ofNullable(winSound).ifPresent(sound -> sound.playSound((EntityPlayerMP) player));
    }

    public static List<Transformer> genAmountPlaceholder(int rewardDrawn, int rewardTotal) {
        return Collections.singletonList(AmountPlaceholder.of(rewardDrawn, rewardTotal));
    }

    /**
     * Do these things:
     * <p>
     * 1. Give the reward
     * </p>
     * <p>
     * 2. Give last shot
     * </p>
     * <p>
     * 3. Clear player drawn list if the crate isn't once
     * </p>
     * <p>
     * 4. Save cache and file
     * </p>
     *
     * @param player
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

    public static void rewardPostProcess(ForgeEnvyPlayer player, List<KujiObj.Reward> finalRewards, List<String> playerDrawn, KujiObj.Crate crate) {
        finalRewards.forEach(reward -> reward.give(player));

        boolean isLast = processLastThings(player, playerDrawn, crate);
        if (IkaKuji.getInstance().getLocale().getLogs().getEnable()) {
            logRes(player, crate, finalRewards, isLast);
        }
    }

    private static boolean processLastThings(ForgeEnvyPlayer player, List<String> playerDrawn, KujiObj.Crate crate) {
        boolean isLast = false;
        if (isFullDrawn(playerDrawn, crate)) {
            isLast = true;

            // Give last shot if present
            Optional.ofNullable(player.getParent()).ifPresent(playerEntity -> {
                if (calRemainInvSize(playerEntity) == 0) {
                    playerEntity.sendMessage(MsgUtil.prefixedColorMsg(IkaKuji.getInstance().getLocale().getMessages().getLastRewardFailMsg(), crate.getDisplayName()));
                } else {
                    Optional.ofNullable(crate.getLastShot()).ifPresent(lastShot -> lastShot.give(player));
                }
            });
        }
        PlayerKujiFactory.updatePlayerDrawn(playerDrawn, player.getUuid(), crate.getDisplayName());
        return isLast;
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
     * Calculate intersect of player drawn list and corresponding crate.
     * This is able to retain the min number of duplicate elements,
     * and will try its best to keep the order of the player drawn list
     *
     * @param playerDrawn
     * @param crate
     * @return
     */
    public static List<String> calIntersect(List<String> playerDrawn, KujiObj.Crate crate) {
        Map<String, Integer> rewardMap = new HashMap<>(crate.getRewardAmountMapLazy());
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

    //TODO: May not compatible in 1.12
    public static ItemStack addRewardLore(KujiObj.Reward reward, int rewardCount, double currentTotalWeight, Map<String, Integer> weightOverrides) {
        ItemStack itemStack = UtilConfigItem.fromConfigItem(reward.getDisplayItem());
        Optional.ofNullable(itemStack.getTagCompound()).ifPresent(nbtTagCompound -> {
            NBTTagCompound display = itemStack.getOrCreateSubCompound("display");
            NBTTagList currentLore = display.getTagList("Lore", 8);
            IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();
            currentLore.appendTag(new NBTTagString(
                    ITextComponent.Serializer.componentToJson(
                            MsgUtil.colorMsg(messages.getDash()))));
            currentLore.appendTag(new NBTTagString(
                    ITextComponent.Serializer.componentToJson(
                            MsgUtil.colorMsg(messages.getRewardRemainCount(), rewardCount))));
            if (reward.isShowProbInPreview()) {
                currentLore.appendTag(new NBTTagString(
                        ITextComponent.Serializer.componentToJson(
                                MsgUtil.colorMsg(messages.getProbPerReward(), (reward.calWeightPerReward(weightOverrides) / currentTotalWeight) * 100d))));
            }
            display.setTag("Lore", currentLore);
            nbtTagCompound.setTag("display", display);
        });
        return itemStack;
    }


    public static void executeKujiLogic(PlayerInteractEvent event, KujiObj.Crate crate, boolean itemCrate) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack itemStack = event.getItemStack();
        AtomicInteger minCount = new AtomicInteger(-1);
        int crateCountRequired = 1;
        if (itemCrate) {
            crateCountRequired = CrateFactory.getItemCrateCountRequired(itemStack);
            if (crateCountRequired > itemStack.getCount()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(IkaKuji.getInstance().getLocale().getMessages().getNeedItemCrateMsg(), crateCountRequired, itemStack.getDisplayName()));
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

    public static boolean executeKujiLogic(EntityPlayer player, KujiObj.Crate crate, AtomicInteger minCount) {
        ForgeEnvyPlayer envyPlayer = IkaKuji.getInstance().getPlayerManager().getPlayer((EntityPlayerMP) player);
        if (!PlayerKujiFactory.hasPlayer(envyPlayer.getUuid())) {
            return false;
        }

        Map<String, List<String>> playerKujiData = PlayerKujiFactory.get(envyPlayer.getUuid()).getKujiData();
        String crateName = crate.getDisplayName();
        List<String> playerDrawn = playerKujiData.getOrDefault(crateName, Lists.newArrayList());
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

        // Preview
        if (player.isSneaking()) {
            // Perm check
            if (!UtilPlayer.hasPermission((EntityPlayerMP) player, PermissionNodes.getPreviewPermNode(crateName))) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoPreviewPermMsg(), crateName));
                return false;
            }

            //Pre cal available rewards here to speed up page changing
            AtomicDouble currentTotalWeight = new AtomicDouble(0d);
            Map<String, Integer> weightOverrideMap = getCurrentWeightOverride(crate, playerDrawn);
            KujiGuiManager.preview(crate, envyPlayer, calAvailableRewards(playerDrawn, crate, currentTotalWeight, weightOverrideMap), currentTotalWeight, weightOverrideMap, 1);
            return false;
        }

        // Open
        // Perm check
        if (!UtilPlayer.hasPermission((EntityPlayerMP) player, PermissionNodes.getOpenPermNode(crateName))) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoOpenPermMsg(), crateName));
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
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getIncompletePreKujiMsg(), incompleteCrates));
                return false;
            }
        }

        // Check full
        if (KujiExecutor.isFullDrawn(playerDrawn, crate)) {
            if (crate.isOneRound()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getOneRoundMsg()));
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
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getInsufficientInvSizeMsg()));
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
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNeedKeyMsg(), crate.getKey().getAmount(), crate.getKey().getName()));
                return false;
            } else if (keyCount > 0) {
                times = keyCount;
            }
            minCount.set(times);

            // Generate rewards must execute last
            rewards = genRandomRewards(availableRewards, totalWeight, playerDrawn, times);
            if (rewards.isEmpty()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoAvailableRwdMsg()));
                return false;
            }

            rewardPostProcess(envyPlayer, rewards, playerDrawn, crate);
        } else {
            //TODO: Async?

            //Take item options
            //Check and take key
            if (!checkAndTakeKey(crate.getKey(), crate.isConsumeKey(), player)) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNeedKeyMsg(), crate.getKey().getAmount(), crate.getKey().getName()));
                return false;
            }

            // Generate rewards must execute last
            rewards = genRandomRewards(availableRewards, totalWeight, playerDrawn);
            if (rewards.isEmpty()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoAvailableRwdMsg()));
                return false;
            }

            KujiGuiManager.open(crate, envyPlayer, playerDrawn, rewards);
        }
        return true;
    }

    private static int calRemainInvSize(EntityPlayer player) {
        int invSize = 0;
        for (ItemStack itemstack : player.inventory.mainInventory) {
            if (itemstack.isEmpty()) {
                invSize++;
            }
        }
        return invSize;
    }

    private static boolean checkAndTakeKey(ExtendedConfigItem crateKey, boolean isConsumeKey, EntityPlayer player) {
        if (Optional.ofNullable(crateKey).isPresent()) {
            for (ItemStack invItem : player.inventory.mainInventory) {
                if (invItem.isItemEqual(UtilConfigItem.fromConfigItem(crateKey))) {
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

    private static int checkAndTakeKeys(ExtendedConfigItem crateKey, boolean isConsumeKey, EntityPlayer player, int minCount) {
        if (Optional.ofNullable(crateKey).isPresent()) {
            for (ItemStack invItem : player.inventory.mainInventory) {
                if (invItem.isItemEqual(UtilConfigItem.fromConfigItem(crateKey))) {
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

    private static double getWeightWithAvailableRewards(List<Pair<KujiObj.Reward, Double>> availableRewards, List<String> playerDrawn, KujiObj.Crate crate) {
        Map<String, Long> drawnMap = playerDrawn.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        double weightRes = 0;
        for (KujiObj.Reward reward : crate.getRewards()) {
            int availableAmount = getAvailableAmount(reward.getAmountPerKuji(), drawnMap.get(reward.getId()));
            double weightPerReward = reward.calWeightPerReward(getCurrentWeightOverride(crate, playerDrawn));
            weightRes += weightPerReward * availableAmount;
            for (int i = 0; i < availableAmount; i++) {
                availableRewards.add(Pair.of(reward, weightPerReward));
            }
        }
        return weightRes;
    }

    /**
     * Drawing is done playerDrawn.size() times, so currently is (playerDrawn.size() + 1)th drawing
     *
     * @param crate
     * @param playerDrawn
     * @return
     */
    private static Map<String, Integer> getCurrentWeightOverride(KujiObj.Crate crate, List<String> playerDrawn) {
        int th = playerDrawn.size() + 1;
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
            double weight = weightedReward.getY();
            if (cumulativeWeight < randomWeight && cumulativeWeight + weight >= randomWeight) {
                KujiObj.Reward reward = weightedReward.getX();
                playerDrawn.add(reward.getId());
                rewards.add(0, reward);
            } else {
                rewards.add(weightedReward.getX());
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
                double weight = weightedReward.getY();
                // This condition guarantee the last reward would be delivered
                if (cumulativeWeight < randomWeight && cumulativeWeight + weight >= randomWeight) {
                    KujiObj.Reward reward = weightedReward.getX();
                    playerDrawn.add(reward.getId());
                    rewards.add(reward);
                    break;
                }
                cumulativeWeight += weight;
            }
        }
        return rewards;
    }

    private static int getAvailableAmount(int amountPerKuji, Long drawnAmount) {
        if (Optional.ofNullable(drawnAmount).isPresent() && drawnAmount <= amountPerKuji) {
            amountPerKuji -= drawnAmount;
        }
        return amountPerKuji;
    }

    private static List<Pair<KujiObj.Reward, Integer>> calAvailableRewards(List<String> playerDrawn, KujiObj.Crate crate, AtomicDouble currentTotalWeight, Map<String, Integer> weightOverrideMap) {
        Map<String, Long> playerDrawnCount = playerDrawn.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return crate.getRewards().stream()
                .filter(KujiObj.Reward::isCanPreview)
                .map(reward -> Pair.of(reward, getAvailableAmount(reward.getAmountPerKuji(), playerDrawnCount.get(reward.getId()))))
                .filter(pair -> pair.getY() > 0)
                .peek(pair -> currentTotalWeight.addAndGet(pair.getX().calWeightPerReward(weightOverrideMap) * pair.getY()))
                .collect(Collectors.toList());
    }
}
