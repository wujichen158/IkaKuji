package com.github.wujichen158.ikakuji.kuji;

import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.ConfigSound;
import com.envyful.api.forge.config.UtilConfigInterface;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.items.ItemBuilder;
import com.envyful.api.forge.items.ItemFlag;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.pane.Pane;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.config.IkaKujiObj;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.Enchantments;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KujiExecutor {
    public static void playSound(ConfigSound winSound, PlayerEntity player) {
        Optional.ofNullable(winSound).ifPresent(sound -> sound.playSound((ServerPlayerEntity) player));
    }

    /**
     * Do these things:
     * 1. Give last shot
     * 2. Clear player drawn list if the crate isn't once
     * 3. Save cache and file
     *
     * @param player
     * @param playerDrawn
     * @param crate
     */
    public static void rewardPostProcess(ForgeEnvyPlayer player, List<String> playerDrawn, IkaKujiObj.Crate crate) {
        if (isFullDrawn(playerDrawn, crate)) {
            // Give last shot if present
            Optional.ofNullable(crate.getLastShot()).ifPresent(lastShot -> lastShot.give(player));

            if (!crate.getOneRound()) {
                playerDrawn.clear();
            }
        }
        PlayerKujiFactory.updatePlayerDrawn(playerDrawn, player.getUniqueId(), crate.getDisplayName());
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
    public static boolean isFullDrawn(List<String> playerDrawn, IkaKujiObj.Crate crate) {
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
    public static List<String> calIntersect(List<String> playerDrawn, IkaKujiObj.Crate crate) {
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


    
    public static void executeKujiLogic(PlayerInteractEvent event, IkaKujiObj.Crate crate, boolean takeItem) {
        // Must check here, or it'll execute twice
        if (event.getHand() != Hand.MAIN_HAND) {
            return;
        }

        PlayerEntity player = event.getPlayer();
        if (executeKujiLogic(player, crate)) {
            //Consume crate
            if (!player.isCreative() && takeItem) {
                event.getItemStack().shrink(1);
            }
        }
    }

    public static boolean executeKujiLogic(PlayerEntity player, IkaKujiObj.Crate crate) {
        ForgeEnvyPlayer envyPlayer = IkaKuji.getInstance().getPlayerManager().getPlayer((ServerPlayerEntity) player);
        if (!PlayerKujiFactory.hasPlayer(envyPlayer.getUniqueId())) {
            return false;
        }

        List<String> playerDrawn = PlayerKujiFactory.get(envyPlayer.getUniqueId())
                .getKujiData().getOrDefault(crate.getDisplayName(), Lists.newArrayList());

        //Preview
        if (player.isShiftKeyDown()) {
            KujiGuiManager.preview(crate, envyPlayer, calAvailableRewards(playerDrawn, crate), 1);
            return false;
        }

        //Open
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

        // Check full
        if (KujiExecutor.isFullDrawn(playerDrawn, crate) && crate.getOneRound()) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getOneRoundMsg()), player.getUUID());
            return false;
        }

        // Generate rewards
        List<IkaKujiObj.Reward> rewards = generateRandomRewards(playerDrawn, crate);
        if (rewards.isEmpty()) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoAvailableRwdMsg()), player.getUUID());
            return false;
        }

        //Take item options must execute last
        //Check and take key
        if (!checkKeyIfHas(crate.getKey(), player)) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNeedKeyMsg(), crate.getKey().getName()), player.getUUID());
            return false;
        }

        //TODO: Async?
        KujiGuiManager.open(crate, envyPlayer, playerDrawn, rewards);
        return true;
    }

    private static boolean checkKeyIfHas(ExtendedConfigItem crateKey, PlayerEntity player) {
        if (Optional.ofNullable(crateKey).isPresent()) {
            String crateKeyName = crateKey.getType();

            for (ItemStack invItem : player.inventory.items) {
                if (Optional.ofNullable(invItem.getItem().getRegistryName())
                        .map(ResourceLocation::toString)
                        .filter(regName -> regName.equals(crateKeyName)).isPresent()) {
                    //Consume key
                    if (!player.isCreative()) {
                        invItem.shrink(crateKey.getAmount());
                    }
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private static List<IkaKujiObj.Reward> generateRandomRewards(List<String> playerDrawn, IkaKujiObj.Crate crate) {
        Map<String, Long> drawnMap = playerDrawn.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        List<Pair<IkaKujiObj.Reward, Double>> availableRewards = Lists.newArrayList();
        double totalWeight = 0;
        for (IkaKujiObj.Reward reward : crate.getRewards()) {
            int availableAmount = getAvailableAmount(reward.getAmountPerKuji(), drawnMap.get(reward.getId()));
            double rewardWeight = (double) reward.getTotalWeight() / reward.getAmountPerKuji();
            totalWeight += rewardWeight * availableAmount;
            for (int i = 0; i < availableAmount; i++) {
                availableRewards.add(new Pair<>(reward, rewardWeight));
            }
        }

        double randomWeight = Reference.RANDOM.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;
        List<IkaKujiObj.Reward> rewards = Lists.newArrayList();
        for (Pair<IkaKujiObj.Reward, Double> weightedReward : availableRewards) {
            if (cumulativeWeight < randomWeight && cumulativeWeight + weightedReward.getSecond() >= randomWeight) {
                IkaKujiObj.Reward reward = weightedReward.getFirst();
                playerDrawn.add(reward.getId());
                rewards.add(0, reward);
            } else {
                rewards.add(weightedReward.getFirst());
            }
            cumulativeWeight += weightedReward.getSecond();
        }
        return rewards;
    }

    private static int getAvailableAmount(int amountPerKuji, Long drawnAmount) {
        if (Optional.ofNullable(drawnAmount).isPresent() && drawnAmount <= amountPerKuji) {
            amountPerKuji -= drawnAmount.intValue();
        }
        return amountPerKuji;
    }

    private static List<Pair<ExtendedConfigItem, Integer>> calAvailableRewards(List<String> playerDrawn, IkaKujiObj.Crate crate) {
        Map<String, Long> playerDrawnCount = playerDrawn.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return crate.getRewards().stream()
                .filter(IkaKujiObj.Reward::getCanPreview)
                .map(reward -> new Pair<>(reward.getDisplayItem(), reward.getAmountPerKuji() - playerDrawnCount.getOrDefault(reward.getId(), 0L).intValue()))
                .filter(pair -> pair.getSecond() > 0)
                .collect(Collectors.toList());
    }
}
