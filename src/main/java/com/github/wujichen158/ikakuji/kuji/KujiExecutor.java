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
import com.github.wujichen158.ikakuji.config.IkaKujiObj;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class KujiExecutor {
    public static void open(IkaKujiObj.Crate crate, ForgeEnvyPlayer player, List<String> playerDrawn, List<IkaKujiObj.Reward> rewards) {
        AtomicInteger timer = new AtomicInteger(0);
        AtomicBoolean cleared = new AtomicBoolean(false);
        IkaKujiObj.Reward finalReward = rewards.get(0);
        ItemStack rewardItem = new ItemBuilder(UtilConfigItem.fromConfigItem(finalReward.getDisplayItem()))
                .enchant(Enchantments.UNBREAKING, 1)
                .itemFlag(ItemFlag.HIDE_ENCHANTS)
                .build();
        //TODO: Test delay
        Pane pane = GuiFactory.paneBuilder()
                .height(crate.getDisplayGuiSettings().getHeight())
                .width(9)
                .topLeftX(0)
                .topLeftY(0)
                .tickHandler(GuiFactory.tickBuilder()
                        .async()
                        .initialDelay(60)
                        .repeatDelay(20)
                        .handler(pane1 -> {
                            timer.incrementAndGet();
                            // Reward settlement
                            if (timer.get() >= (2 * crate.getSpinDuration())) {

                                if (!cleared.get()) {
                                    cleared.set(true);
                                    playSound(finalReward.getWinSound(), player.getParent());
                                    int counter = 0;
                                    for (ConfigItem fillerItem : crate.getDisplayGuiSettings().getFillerItems()) {
                                        if (!fillerItem.isEnabled() || counter == crate.getFinalRewardPosition()) {
                                            ++counter;
                                            continue;
                                        }

                                        pane1.set(counter % 9, counter / 9, GuiFactory.displayable(UtilConfigItem.fromConfigItem(fillerItem)));
                                        ++counter;
                                    }
                                }

                                pane1.set(crate.getFinalRewardPosition() % 9, crate.getFinalRewardPosition() / 9,
                                        GuiFactory.displayable(rewardItem));
                                return;
                            }

                            // Rolling
                            playSound(crate.getRollSound(), player.getParent());
                            List<Integer> spinSlots = crate.getDisplaySlots();

                            for (int i = spinSlots.size() - 1; i > 0; i--) {
                                int slot = spinSlots.get(i);
                                int lastSlot = spinSlots.get(i - 1);
                                pane1.set(slot % 9, slot / 9, pane1.get(lastSlot % 9, lastSlot / 9));
                            }

                            int slot = spinSlots.get(0);

                            int subtraction = spinSlots.indexOf(crate.getFinalRewardPosition());
                            subtraction = subtraction == -1 ? 4 : spinSlots.size() - subtraction;

                            pane1.set(slot % 9, slot / 9, GuiFactory.displayable(
                                    timer.get() == ((2 * crate.getSpinDuration()) - subtraction) ?
                                            rewardItem : UtilConfigItem.fromConfigItem(rewards.get(Reference.RANDOM.nextInt(rewards.size())).getDisplayItem())));
                        })
                        .build())
                .build();

        UtilConfigInterface.fillBackground(pane, crate.getDisplayGuiSettings());

        // Display init kuji gui
        for (Integer spinSlot : crate.getDisplaySlots()) {
            pane.set(spinSlot % 9, spinSlot / 9, GuiFactory.displayable(
                    UtilConfigItem.fromConfigItem(rewards.get(Reference.RANDOM.nextInt(rewards.size())).getDisplayItem())));
        }

        GuiFactory.guiBuilder()
                .addPane(pane)
                .title(UtilChatColour.colour(crate.getDisplayGuiSettings().getTitle()))
                .height(crate.getDisplayGuiSettings().getHeight())
                .setPlayerManager(IkaKuji.getInstance().getPlayerManager())
                .closeConsumer(GuiFactory.closeConsumerBuilder()
                        .async()
                        .handler(envyPlayer -> {
                            finalReward.give(player);
                            rewardPostProcess(player, playerDrawn, crate);
                        })
                        .build())
                .build()
                .open(IkaKuji.getInstance().getPlayerManager().getPlayer(player.getParent()));
    }

    private static void playSound(ConfigSound winSound, PlayerEntity player) {
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
    private static void rewardPostProcess(ForgeEnvyPlayer player, List<String> playerDrawn, IkaKujiObj.Crate crate) {
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

    public static void preview(IkaKujiObj.Crate crate, ForgeEnvyPlayer player, List<Pair<ExtendedConfigItem, Integer>> availableRewardItems, int page) {
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
                addRewardLore(itemStack, pair.getSecond());
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

        GuiFactory.guiBuilder()
                .addPane(pane)
                .title(UtilChatColour.colour(crate.getPreviewGuiSettings().getTitle()))
                .height(crate.getPreviewGuiSettings().getHeight())
                .setPlayerManager(IkaKuji.getInstance().getPlayerManager())
                .build()
                .open(IkaKuji.getInstance().getPlayerManager().getPlayer(player.getParent()));
    }

    private static void addRewardLore(ItemStack itemStack, int rewardCount) {
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
}
