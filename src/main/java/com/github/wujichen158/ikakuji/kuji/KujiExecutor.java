package com.github.wujichen158.ikakuji.kuji;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.ConfigSound;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.text.Placeholder;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.lib.Placeholders;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
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

        boolean isLast = false;
        if (isFullDrawn(playerDrawn, crate)) {
            isLast = true;

            // Give last shot if present
            Optional.ofNullable(crate.getLastShot()).ifPresent(lastShot -> lastShot.give(player));

            if (!crate.getOneRound()) {
                playerDrawn.clear();
            }
        }
        PlayerKujiFactory.updatePlayerDrawn(playerDrawn, player.getUniqueId(), crate.getDisplayName());

        if (IkaKuji.getInstance().getLocale().getLogs().getEnable()) {
            logRes(player, crate, finalReward, isLast);
        }
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
            String logEntry = String.format("%s-%s-%s %02d:%02d:%02d: %s\n",
                    now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                    now.getHour(), now.getMinute(), now.getSecond(),
                    logs.getWinRewardLog()
                            .replace(Placeholders.PLAYER_NAME, player.getName())
                            .replace(Placeholders.CRATE_NAME, crate.getDisplayName())
                            .replace(Placeholders.REWARD_ID, finalReward.getId())
                            .replace(Placeholders.REWARD_NAME, finalReward.getDisplayItem().getName())
            );
            if (isLast) {
                logEntry += logs.getLastShotLog();
            }
            try {
                Files.write(Paths.get(Reference.LOG_PATH), logEntry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                IkaKuji.LOGGER.error(e.toString());
            }
        });
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

    public static void addRewardLore(ItemStack itemStack, int rewardCount) {
        CompoundNBT display = itemStack.getOrCreateTagElement("display");
        ListNBT currentLore = display.getList("Lore", 8);
        currentLore.add(StringNBT.valueOf(""));
        currentLore.add(StringNBT.valueOf(
                ITextComponent.Serializer.toJson(
                        UtilChatColour.colour(String.format(
                                IkaKuji.getInstance().getLocale().getMessages().getRewardRemainCount(), rewardCount)))));
        display.put("Lore", currentLore);
        itemStack.addTagElement("display", display);
    }


    public static void executeKujiLogic(PlayerInteractEvent event, KujiObj.Crate crate, boolean takeItem) {
        // Must check here, or it'll execute twice
        if (event.getHand() != Hand.MAIN_HAND) {
            return;
        }

        PlayerEntity player = event.getPlayer();
        AtomicInteger minCount = new AtomicInteger(event.getItemStack().getCount());
        if (executeKujiLogic(player, crate, minCount)) {
            //Consume crate
            if (!player.isCreative() && takeItem) {
                event.getItemStack().shrink(minCount.get());
            }
        }
    }

    public static boolean executeKujiLogic(PlayerEntity player, KujiObj.Crate crate, AtomicInteger minCount) {
        ForgeEnvyPlayer envyPlayer = IkaKuji.getInstance().getPlayerManager().getPlayer((ServerPlayerEntity) player);
        if (!PlayerKujiFactory.hasPlayer(envyPlayer.getUniqueId())) {
            return false;
        }

        Map<String, List<String>> playerKujiData = PlayerKujiFactory.get(envyPlayer.getUniqueId()).getKujiData();
        List<String> playerDrawn = playerKujiData.getOrDefault(crate.getDisplayName(), Lists.newArrayList());

        //Preview
        if (player.isShiftKeyDown()) {
            KujiGuiManager.preview(crate, envyPlayer, calAvailableRewards(playerDrawn, crate), 1);
            return false;
        }

        //Open
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

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
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getIncompletePreKuji(), incompleteCrates), player.getUUID());
                return false;
            }
        }

        // Check full
        if (KujiExecutor.isFullDrawn(playerDrawn, crate) && crate.getOneRound()) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getOneRoundMsg()), player.getUUID());
            return false;
        }


        // Generate available rewards list and total weight
        List<Pair<KujiObj.Reward, Double>> availableRewards = Lists.newArrayList();
        double totalWeight = getWeightWithAvailableRewards(availableRewards, playerDrawn, crate);

        List<KujiObj.Reward> rewards;
        if (crate.getJumpAnimation()) {
            // Min availableReward size, limitPerDraw, crate count and key count
            int times = Math.min(availableRewards.size(), minCount.get());
            int limitPerDraw = crate.getLimitPerDraw();
            if (limitPerDraw > 0) {
                times = Math.min(limitPerDraw, times);
            }

            //Take item options must execute last
            //Check and take key
            int keyCount = checkAndTakeKeys(crate.getKey(), player, times);
            if (keyCount == -1) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNeedKeyMsg(), crate.getKey().getName()), player.getUUID());
                return false;
            } else if (keyCount > 0) {
                times = keyCount;
            }
            minCount.set(times);

            // Generate rewards
            rewards = genRandomRewards(availableRewards, totalWeight, playerDrawn, times);
            if (rewards.isEmpty()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoAvailableRwdMsg()), player.getUUID());
                return false;
            }

            rewardPostProcess(envyPlayer, rewards.get(0), playerDrawn, crate);
        } else {
            //TODO: Async?

            // Generate rewards
            rewards = genRandomRewards(availableRewards, totalWeight, playerDrawn);
            if (rewards.isEmpty()) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoAvailableRwdMsg()), player.getUUID());
                return false;
            }

            //Take item options must execute last
            //Check and take key
            if (!checkAndTakeKey(crate.getKey(), player)) {
                player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNeedKeyMsg(), crate.getKey().getName()), player.getUUID());
                return false;
            }

            KujiGuiManager.open(crate, envyPlayer, playerDrawn, rewards);
        }
        return true;
    }

    private static boolean checkAndTakeKey(ExtendedConfigItem crateKey, PlayerEntity player) {
        if (Optional.ofNullable(crateKey).isPresent()) {
            String crateKeyName = crateKey.getType();

            for (ItemStack invItem : player.inventory.items) {
                if (Optional.ofNullable(invItem.getItem().getRegistryName())
                        .map(ResourceLocation::toString)
                        .filter(regName -> regName.equals(crateKeyName)).isPresent()) {
                    //Consume key
                    if (!player.isCreative()) {
                        if (crateKey.getAmount() <= invItem.getCount()) {
                            invItem.shrink(crateKey.getAmount());
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

    private static int checkAndTakeKeys(ExtendedConfigItem crateKey, PlayerEntity player, int minCount) {
        if (Optional.ofNullable(crateKey).isPresent()) {
            String crateKeyName = crateKey.getType();

            for (ItemStack invItem : player.inventory.items) {
                if (Optional.ofNullable(invItem.getItem().getRegistryName())
                        .map(ResourceLocation::toString)
                        .filter(regName -> regName.equals(crateKeyName)).isPresent()) {
                    //Consume keys
                    if (!player.isCreative()) {
                        int maxKeyCount = invItem.getCount() / crateKey.getAmount();
                        minCount = Math.min(minCount, maxKeyCount);
                        invItem.shrink(crateKey.getAmount() * minCount);
                        return minCount;
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
        double totalWeight = 0;
        for (KujiObj.Reward reward : crate.getRewards()) {
            int availableAmount = getAvailableAmount(reward.getAmountPerKuji(), drawnMap.get(reward.getId()));
            double rewardWeight = (double) reward.getTotalWeight() / reward.getAmountPerKuji();
            totalWeight += rewardWeight * availableAmount;
            for (int i = 0; i < availableAmount; i++) {
                availableRewards.add(new Pair<>(reward, rewardWeight));
            }
        }
        return totalWeight;
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
            if (cumulativeWeight < randomWeight && cumulativeWeight + weightedReward.getSecond() >= randomWeight) {
                KujiObj.Reward reward = weightedReward.getFirst();
                playerDrawn.add(reward.getId());
                rewards.add(0, reward);
            } else {
                rewards.add(weightedReward.getFirst());
            }
            cumulativeWeight += weightedReward.getSecond();
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
                if (cumulativeWeight >= randomWeight) {
                    KujiObj.Reward reward = weightedReward.getFirst();
                    playerDrawn.add(reward.getId());
                    rewards.add(0, reward);
                    break;
                }
                cumulativeWeight += weightedReward.getSecond();
            }
        }
        return rewards;
    }

    private static int getAvailableAmount(int amountPerKuji, Long drawnAmount) {
        if (Optional.ofNullable(drawnAmount).isPresent() && drawnAmount <= amountPerKuji) {
            amountPerKuji -= drawnAmount.intValue();
        }
        return amountPerKuji;
    }

    private static List<Pair<ExtendedConfigItem, Integer>> calAvailableRewards(List<String> playerDrawn, KujiObj.Crate crate) {
        Map<String, Long> playerDrawnCount = playerDrawn.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return crate.getRewards().stream()
                .filter(KujiObj.Reward::getCanPreview)
                .map(reward -> new Pair<>(reward.getDisplayItem(), reward.getAmountPerKuji() - playerDrawnCount.getOrDefault(reward.getId(), 0L).intValue()))
                .filter(pair -> pair.getSecond() > 0)
                .collect(Collectors.toList());
    }
}
