package com.github.wujichen158.ikakuji.kuji;

import com.envyful.api.config.type.ExtendedConfigItem;
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
import com.github.wujichen158.ikakuji.kuji.gui.IGuiTickHandler;
import com.github.wujichen158.ikakuji.kuji.gui.impl.GuiTickHandlerFactory;
import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
                        .initialDelay((int)(crate.getInitialDelay() * 20))
                        .repeatDelay((int)(crate.getRepeatDelay() * 20))
                        .handler(tickHandler.handle(crate, player, rewards, timer, cleared, rewardItem))
                        .build())
                .build();

        UtilConfigInterface.fillBackground(pane, crate.getDisplayGuiSettings());

        // Display init kuji gui
        tickHandler.initDisplaySlots(pane, crate, rewards);

        GuiFactory.guiBuilder()
                .addPane(pane)
                .title(UtilChatColour.colour(crate.getDisplayGuiSettings().getTitle()))
                .height(crate.getDisplayGuiSettings().getHeight())
                .setPlayerManager(IkaKuji.getInstance().getPlayerManager())
                .closeConsumer(GuiFactory.closeConsumerBuilder()
                        .async()
                        .handler(envyPlayer -> {
                            finalReward.give(player);
                            KujiExecutor.rewardPostProcess(player, finalReward, playerDrawn, crate);
                        })
                        .build())
                .build()
                .open(IkaKuji.getInstance().getPlayerManager().getPlayer(player.getParent()));
    }

    public static void preview(KujiObj.Crate crate, ForgeEnvyPlayer player, List<Pair<ExtendedConfigItem, Integer>> availableRewardItems, int page) {
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
            if (beginIndex < availableRewardItems.size()) {
                Pair<ExtendedConfigItem, Integer> pair = availableRewardItems.get(beginIndex);
                itemStack = UtilConfigItem.fromConfigItem(pair.getFirst());
                // Add a lore for amount preview
                KujiExecutor.addRewardLore(itemStack, pair.getSecond());
            } else {
                itemStack = ItemStack.EMPTY;
            }
            pane.set(previewSlot % 9, previewSlot / 9, GuiFactory.displayable(itemStack));
            beginIndex++;
        }

        // Next page button
        if (onePageCount * page < availableRewardItems.size()) {
            UtilConfigItem.builder()
                    .clickHandler((envyPlayer, clickType) -> preview(crate, player, availableRewardItems, page + 1))
                    .extendedConfigItem(player, pane, crate.getPreviewNextPage());
        }

        // Previous page button
        if (page > 1) {
            UtilConfigItem.builder()
                    .clickHandler((envyPlayer, clickType) -> preview(crate, player, availableRewardItems, page - 1))
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
}
