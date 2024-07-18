package com.github.wujichen158.ikakuji.kuji;

import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.UtilConfigInterface;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.items.ItemBuilder;
import com.envyful.api.forge.items.ItemFlag;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.pane.Pane;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.executor.PreviewExecutor;
import com.github.wujichen158.ikakuji.kuji.gui.IGlobalGuiHandler;
import com.github.wujichen158.ikakuji.kuji.gui.IGuiTickHandler;
import com.github.wujichen158.ikakuji.kuji.gui.impl.GlobalGuiHandlerFactory;
import com.github.wujichen158.ikakuji.kuji.gui.impl.GuiTickHandlerFactory;
import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class KujiGuiManager {
    public static void open(KujiObj.Crate crate, ForgeEnvyPlayer player, List<String> playerDrawn, List<KujiObj.Reward> rewards) {
        AtomicInteger timer = new AtomicInteger(0);
        AtomicBoolean cleared = new AtomicBoolean(false);
        KujiObj.Reward finalReward = rewards.get(0);
        ItemStack rewardItem = new ItemBuilder(UtilConfigItem.fromConfigItem(finalReward.getDisplayItem()))
                .enchant(Enchantments.UNBREAKING, 1)
                .itemFlag(ItemFlag.HIDE_ENCHANTS)
                .build();

        IGuiTickHandler tickHandler = GuiTickHandlerFactory.getFromElem(crate.getGuiChangePattern());

        Pane pane = GuiFactory.paneBuilder()
                .height(crate.getDisplayGuiSettings().getHeight())
                .width(9)
                .topLeftX(0)
                .topLeftY(0)
                .tickHandler(GuiFactory.tickBuilder()
                        .async()
                        .initialDelay((int) (crate.getInitialDelay() * 20))
                        .repeatDelay((int) (crate.getRepeatDelay() * 20))
                        .handler(tickHandler.handle(crate, player, rewards, timer, cleared, rewardItem))
                        .build())
                .build();

        // init kuji GUI
        tickHandler.initGui(pane, crate, rewards);

        GuiFactory.guiBuilder()
                .addPane(pane)
                .title(UtilChatColour.colour(crate.getDisplayGuiSettings().getTitle()))
                .height(crate.getDisplayGuiSettings().getHeight())
                .setPlayerManager(IkaKuji.getInstance().getPlayerManager())
                .closeConsumer(GuiFactory.closeConsumerBuilder()
                        .async()
                        .handler(envyPlayer -> KujiExecutor.rewardPostProcess(player, finalReward, playerDrawn, crate))
                        .build())
                .build()
                .open(IkaKuji.getInstance().getPlayerManager().getPlayer(player.getParent()));
    }

    public static void openGlobal(int page, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, ForgeEnvyPlayer player) {
        IGlobalGuiHandler handler = GlobalGuiHandlerFactory.getHandler();

        Pane pane = GuiFactory.paneBuilder()
                .height(crate.getDisplayGuiSettings().getHeight())
                .width(9)
                .topLeftX(0)
                .topLeftY(0)
                .tickHandler(GuiFactory.tickBuilder()
                        .async()
                        .initialDelay(20)
                        .repeatDelay(20)
                        .handler(handler.handle(page, globalKujiData, crate, player))
                        .build())
                .build();

        // init kuji GUI
        handler.initGui(pane, page, globalKujiData, crate, player);

        GuiFactory.guiBuilder()
                .addPane(pane)
                .title(UtilChatColour.colour(crate.getDisplayGuiSettings().getTitle()))
                .height(crate.getDisplayGuiSettings().getHeight())
                .setPlayerManager(IkaKuji.getInstance().getPlayerManager())
                .build()
                .open(IkaKuji.getInstance().getPlayerManager().getPlayer(player.getParent()));
    }

    public static void preview(KujiObj.Crate crate, ForgeEnvyPlayer player, List<Pair<KujiObj.Reward, Integer>> availableRewards, AtomicDouble currentTotalWeight, Map<String, Integer> weightOverrideMap, int page) {
        Pane pane = GuiFactory.paneBuilder()
                .height(crate.getPreviewGuiSettings().getHeight())
                .width(9)
                .topLeftX(0)
                .topLeftY(0)
                .build();

        UtilConfigInterface.fillBackground(pane, crate.getPreviewGuiSettings());

        int onePageCount = crate.getPreviewSlots().size();
        int beginIndex = (page - 1) * onePageCount;
        for (int previewSlot : crate.getPreviewSlots()) {
            ItemStack itemStack;
            // Remain slots will keep empty
            if (beginIndex < availableRewards.size()) {
                Pair<KujiObj.Reward, Integer> pair = availableRewards.get(beginIndex);
                // Add lores for amount and probability preview
                KujiObj.Reward reward = pair.getFirst();
                itemStack = KujiExecutor.addRewardLore(reward, pair.getSecond(),
                        (reward.calWeightPerReward(weightOverrideMap) / currentTotalWeight.get()) * 100d);
            } else {
                itemStack = ItemStack.EMPTY;
            }
            pane.set(previewSlot % 9, previewSlot / 9, GuiFactory.displayable(itemStack));
            beginIndex++;
        }

        // Next page button
        if (onePageCount * page < availableRewards.size()) {
            UtilConfigItem.builder()
                    .clickHandler((envyPlayer, clickType) -> preview(crate, player, availableRewards, currentTotalWeight, weightOverrideMap, page + 1))
                    .extendedConfigItem(player, pane, crate.getPreviewNextPage());
        }

        // Previous page button
        if (page > 1) {
            UtilConfigItem.builder()
                    .clickHandler((envyPlayer, clickType) -> preview(crate, player, availableRewards, currentTotalWeight, weightOverrideMap, page - 1))
                    .extendedConfigItem(player, pane, crate.getPreviewPreviousPage());
        }

        // Placeholder
        Optional.ofNullable(crate.getPlaceholderButton()).ifPresent(placeholderItem -> {
            int rewardDrawn = Optional.ofNullable(PlayerKujiFactory.get(player.getUniqueId()))
                    .map(KujiObj.PlayerData::getKujiData)
                    .map(kujiData -> kujiData.get(crate.getDisplayName()))
                    .map(List::size)
                    .orElse(0);
            int rewardTotal = crate.getRewardTotalLazy();

            UtilConfigItem.builder()
                    .extendedConfigItem(player, pane, placeholderItem, KujiExecutor.genAmountPlaceholder(rewardDrawn, rewardTotal));
        });

        GuiFactory.guiBuilder()
                .addPane(pane)
                .title(UtilChatColour.colour(crate.getPreviewGuiSettings().getTitle()))
                .height(crate.getPreviewGuiSettings().getHeight())
                .setPlayerManager(IkaKuji.getInstance().getPlayerManager())
                .build()
                .open(IkaKuji.getInstance().getPlayerManager().getPlayer(player.getParent()));
    }

    public static void previewGlobal(KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, ForgeEnvyPlayer player,
                                     Map<String, Integer> weightOverrideMap, int page) {
        AtomicDouble currentTotalWeight = new AtomicDouble(0d);
        List<Pair<KujiObj.Reward, Integer>> availableRewards = PreviewExecutor.calAvailableRewards(
                globalKujiData.getData().stream()
                        .filter(globalDataEntry -> globalDataEntry.getRewardId() != null)
                        .collect(Collectors.groupingBy(KujiObj.GlobalDataEntry::getRewardId, Collectors.counting())),
                crate, currentTotalWeight, weightOverrideMap);

        Pane pane = GuiFactory.paneBuilder()
                .height(crate.getPreviewGuiSettings().getHeight())
                .width(9)
                .topLeftX(0)
                .topLeftY(0)
                .build();

        UtilConfigInterface.fillBackground(pane, crate.getPreviewGuiSettings());

        int onePageCount = crate.getPreviewSlots().size();
        int beginIndex = (page - 1) * onePageCount;
        for (int previewSlot : crate.getPreviewSlots()) {
            ItemStack itemStack;
            // Remain slots will keep empty
            if (beginIndex < availableRewards.size()) {
                Pair<KujiObj.Reward, Integer> pair = availableRewards.get(beginIndex);
                // Add lores for amount and probability preview
                KujiObj.Reward reward = pair.getFirst();
                itemStack = KujiExecutor.addRewardLore(pair.getFirst(), pair.getSecond(),
                        (reward.calWeightPerReward(weightOverrideMap) / currentTotalWeight.get()) * 100d);
            } else {
                itemStack = ItemStack.EMPTY;
            }
            pane.set(previewSlot % 9, previewSlot / 9, GuiFactory.displayable(itemStack));
            beginIndex++;
        }

        // Next page button
        if (onePageCount * page < availableRewards.size()) {
            UtilConfigItem.builder()
                    .clickHandler((envyPlayer, clickType) -> previewGlobal(globalKujiData, crate, player, weightOverrideMap, page + 1))
                    .extendedConfigItem(player, pane, crate.getPreviewNextPage());
        }

        // Previous page button
        if (page > 1) {
            UtilConfigItem.builder()
                    .clickHandler((envyPlayer, clickType) -> previewGlobal(globalKujiData, crate, player, weightOverrideMap, page - 1))
                    .extendedConfigItem(player, pane, crate.getPreviewPreviousPage());
        }

        // Placeholder
        Optional.ofNullable(crate.getPlaceholderButton()).ifPresent(placeholderItem -> {
            int rewardDrawn = globalKujiData.getDrawnCount();
            int rewardTotal = crate.getRewardTotalLazy();

            UtilConfigItem.builder()
                    .extendedConfigItem(player, pane, placeholderItem, KujiExecutor.genAmountPlaceholder(rewardDrawn, rewardTotal));
        });

        GuiFactory.guiBuilder()
                .addPane(pane)
                .title(UtilChatColour.colour(crate.getPreviewGuiSettings().getTitle()))
                .height(crate.getPreviewGuiSettings().getHeight())
                .setPlayerManager(IkaKuji.getInstance().getPlayerManager())
                .build()
                .open(IkaKuji.getInstance().getPlayerManager().getPlayer(player.getParent()));
    }
}
