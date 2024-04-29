package com.github.wujichen158.ikakuji.listener;

import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.config.IkaKujiObj;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Execute priority: Item > entity = block
 * @author wujichen158
 */
public class KujiTriggerListener {

    //TODO: Support permissions

    @SubscribeEvent
    public void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        this.handleItemInteract(event);
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        this.handleItemInteract(event);
        if (!event.isCanceled()) {
            BlockPos blockPos = event.getHitVec().getBlockPos();
            IkaKujiObj.Crate crate = CrateFactory.tryGetWorldPosCrate(event.getPlayer().level,
                    blockPos.getX(),
                    blockPos.getY(),
                    blockPos.getZ());
            if (Optional.ofNullable(crate).isEmpty()) {
                return;
            }

            event.setCanceled(true);
            event.setUseBlock(Event.Result.DENY);

            executeKujiLogic(event, crate);
        }
    }

    @SubscribeEvent
    public void onPlayerRightClickEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        this.handleItemInteract(event);
        if (!event.isCanceled()) {
            IkaKujiObj.Crate crate = CrateFactory.tryGetEntityCrate(event.getTarget().getName().getString());
            if (Optional.ofNullable(crate).isEmpty()) {
                return;
            }

            event.setCanceled(true);
            //TODO: Has doubt on this:
            event.setCancellationResult(ActionResultType.FAIL);

            executeKujiLogic(event, crate);
        }
    }

    private void handleItemInteract(PlayerInteractEvent event) {
        ResourceLocation resourceName = event.getItemStack().getItem().getRegistryName();
        if (Optional.ofNullable(resourceName).isEmpty()) {
            return;
        }

        IkaKujiObj.Crate crate = CrateFactory.tryGetItemCrate(resourceName.toString());
        if (Optional.ofNullable(crate).isEmpty()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.FAIL);

        executeKujiLogic(event, crate);
    }

    private void executeKujiLogic(PlayerInteractEvent event, IkaKujiObj.Crate crate) {
        if (event.getHand() != Hand.MAIN_HAND) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        ForgeEnvyPlayer envyPlayer = IkaKuji.getInstance().getPlayerManager().getPlayer((ServerPlayerEntity) player);
        if (!PlayerKujiFactory.hasPlayer(envyPlayer.getUniqueId())) {
            return;
        }

        List<String> playerDrawn = PlayerKujiFactory.get(envyPlayer.getUniqueId())
                .getKujiData().getOrDefault(crate.getDisplayName(), Lists.newArrayList());

        //Preview
        if (player.isShiftKeyDown()) {
            KujiExecutor.preview(crate, envyPlayer, calAvailableRewards(playerDrawn, crate), 1);
            return;
        }

        //Open
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

        //Check key first
        if (!checkKeyIfHas(crate.getKey(), player)) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNeedKeyMsg(), crate.getKey().getName()), player.getUUID());
            return;
        }

        // Check full
        if (KujiExecutor.isFullDrawn(playerDrawn, crate) && crate.getOneRound()) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getOneRoundMsg()), player.getUUID());
            return;
        }

        // Generate rewards
        List<IkaKujiObj.Reward> rewards = generateRandomRewards(playerDrawn, crate);
        if (rewards.isEmpty()) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoAvailableRwdMsg()), player.getUUID());
            return;
        }

        //Consume crate
        if (!player.isCreative()) {
            event.getItemStack().shrink(1);
        }

        KujiExecutor.open(crate, envyPlayer, playerDrawn, rewards);
    }

    private boolean checkKeyIfHas(ExtendedConfigItem crateKey, PlayerEntity player) {
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
