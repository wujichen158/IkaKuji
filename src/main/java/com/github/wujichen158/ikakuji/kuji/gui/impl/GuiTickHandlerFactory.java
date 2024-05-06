package com.github.wujichen158.ikakuji.kuji.gui.impl;

import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.pane.Pane;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.kuji.gui.EnumGuiPattern;
import com.github.wujichen158.ikakuji.kuji.gui.IGuiTickHandler;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GuiTickHandlerFactory {

    public static class StandardHandler implements IGuiTickHandler {
        @Override
        public Consumer<Pane> handle(KujiObj.Crate crate, ForgeEnvyPlayer player, List<KujiObj.Reward> rewards, AtomicInteger timer, AtomicBoolean cleared, ItemStack rewardItem) {
            return pane -> {
                timer.incrementAndGet();
                // Reward settlement
                double currentSecond = crate.getRepeatDelay() * timer.get();
                if (currentSecond >= crate.getSpinDuration()) {

                    if (!cleared.get()) {
                        cleared.set(true);
                        KujiObj.Reward finalReward = rewards.get(0);
                        KujiExecutor.playSound(finalReward.getWinSound(), player.getParent());
                        int i = 0;
                        for (ConfigItem fillerItem : crate.getDisplayGuiSettings().getFillerItems()) {
                            if (!fillerItem.isEnabled() || i == crate.getFinalRewardPosition()) {
                                i++;
                                continue;
                            }

                            pane.set(i % 9, i / 9, GuiFactory.displayable(UtilConfigItem.fromConfigItem(fillerItem)));
                            i++;
                        }
                        pane.set(crate.getFinalRewardPosition() % 9, crate.getFinalRewardPosition() / 9,
                                GuiFactory.displayable(rewardItem));
                    }

                    return;
                }

                // Rolling
                KujiExecutor.playSound(crate.getRollSound(), player.getParent());
                List<Integer> spinSlots = crate.getDisplaySlots();

                for (int i = spinSlots.size() - 1; i > 0; i--) {
                    int slot = spinSlots.get(i);
                    int lastSlot = spinSlots.get(i - 1);
//                    pane.set(slot % 9, slot / 9, pane.get(lastSlot % 9, lastSlot / 9));
                }

                int slot = spinSlots.get(0);

                int offset = spinSlots.indexOf(crate.getFinalRewardPosition());
                offset = offset == -1 ? 4 : spinSlots.size() - offset;

                pane.set(slot % 9, slot / 9, GuiFactory.displayable(
                        currentSecond == (crate.getSpinDuration() - offset) ?
                                rewardItem : UtilConfigItem.fromConfigItem(rewards.get(Reference.RANDOM.nextInt(rewards.size())).getDisplayItem())));
            };
        }

        @Override
        public void initDisplaySlots(Pane pane, KujiObj.Crate crate, List<KujiObj.Reward> rewards) {
            for (Integer spinSlot : crate.getDisplaySlots()) {
                pane.set(spinSlot % 9, spinSlot / 9, GuiFactory.displayable(
                        UtilConfigItem.fromConfigItem(rewards.get(Reference.RANDOM.nextInt(rewards.size())).getDisplayItem())));
            }
        }
    }

    public static class KujiHandler implements IGuiTickHandler {
        @Override
        public Consumer<Pane> handle(KujiObj.Crate crate, ForgeEnvyPlayer player, List<KujiObj.Reward> rewards, AtomicInteger timer, AtomicBoolean cleared, ItemStack rewardItem) {
            return pane -> {
                // Only render one time is ok
                if (!cleared.get()) {
                    draw(pane, crate, rewards, rewardItem);
                    cleared.set(true);
                }
            };
        }

        public void draw(Pane pane, KujiObj.Crate crate, List<KujiObj.Reward> rewards, ItemStack rewardItem) {
            List<Integer> displaySlots = crate.getDisplaySlots();

            int rewardsSize = rewards.size();
            ItemStack coverItem = UtilConfigItem.fromConfigItem(crate.getCoverItem());


            int i = 0;
            for (int slot : displaySlots) {
                ItemStack itemStack = i < rewardsSize ? coverItem : ItemStack.EMPTY;
                pane.set(slot % 9, slot / 9, GuiFactory.displayableBuilder(itemStack)
                        .clickHandler((envyPlayer, clickType) -> {
                            setResultPane(pane, rewardItem, coverItem, displaySlots, rewardsSize, slot);
                        }).build());
                i++;
            }
        }

        private void setResultPane(Pane pane, ItemStack rewardItem, ItemStack coverItem, List<Integer> displaySlots, int rewardsSize, int rewardSlot) {
            int i = 0;
            for (int slot : displaySlots) {
                ItemStack itemStack = rewardSlot == slot ? rewardItem : i < rewardsSize ? coverItem : ItemStack.EMPTY;
                pane.set(slot % 9, slot / 9, GuiFactory.displayable(itemStack));
                i++;
            }
        }

        @Override
        public void initDisplaySlots(Pane pane, KujiObj.Crate crate, List<KujiObj.Reward> rewards) {
            ItemStack coverItem = Optional.ofNullable(crate.getCoverItem()).map(UtilConfigItem::fromConfigItem).orElse(ItemStack.EMPTY);
            for (Integer slot : crate.getDisplaySlots()) {
                pane.set(slot % 9, slot / 9, GuiFactory.displayable(coverItem));
            }
        }

    }

    public static class CasinoHandler implements IGuiTickHandler {

        private static final List<Integer> DEFAULT_COLS = Lists.newArrayList(2, 4, 6);

        /**
         * In this case:
         * <p>
         * displaySlots present columns
         * </p>
         * <p>
         * finalRewardPosition presents the stop row
         * </p>
         *
         * @param crate
         * @param player
         * @param rewards
         * @param timer
         * @param cleared
         * @param rewardItem
         * @return
         */
        @Override
        public Consumer<Pane> handle(KujiObj.Crate crate, ForgeEnvyPlayer player, List<KujiObj.Reward> rewards, AtomicInteger timer, AtomicBoolean cleared, ItemStack rewardItem) {
            return pane -> {
                timer.incrementAndGet();
                //frequency * times
                double currentSecond = crate.getRepeatDelay() * timer.get();
                // Reward settlement
                if (currentSecond >= crate.getSpinDuration()) {

                    if (!cleared.get()) {
                        cleared.set(true);
                        KujiObj.Reward finalReward = rewards.get(0);
                        KujiExecutor.playSound(finalReward.getWinSound(), player.getParent());
                    }
                    return;
                }

                // Rolling
                KujiExecutor.playSound(crate.getRollSound(), player.getParent());
                List<Integer> spinCols = get3Cols(crate.getDisplaySlots());
                int guiHeight = crate.getDisplayGuiSettings().getHeight() - 1;

                int stopRow = crate.getFinalRewardPosition();
                if (stopRow > 5 || stopRow < 0) {
                    stopRow = 3;
                }
                stopRow = Math.min(guiHeight, stopRow);

                //Gap between 2 reward cols
                int rewardGap = 3;

                //Offset of each reward. The second part can be deferred from rewardGap
                int offset = stopRow + (rewardGap * 2 + 1);

                for (int i = 0; i < spinCols.size(); i++) {
                    int col = spinCols.get(i);
                    if ((timer.get() + offset - (i + 1) * rewardGap) * crate.getRepeatDelay() <= crate.getSpinDuration()) {
                        for (int j = guiHeight; j > 0; j--) {
//                            pane.set(col, j, pane.get(col, j - 1));
                        }
                        pane.set(col, 0, GuiFactory.displayable(
                                (timer.get() + offset - i * rewardGap) * crate.getRepeatDelay() == crate.getSpinDuration() ?
                                        rewardItem : UtilConfigItem.fromConfigItem(rewards.get(Reference.RANDOM.nextInt(rewards.size())).getDisplayItem())));

                    }
                }
            };
        }

        @Override
        public void initDisplaySlots(Pane pane, KujiObj.Crate crate, List<KujiObj.Reward> rewards) {
            List<Integer> cols = get3Cols(crate.getDisplaySlots());
            for (int col : cols) {
                for (int i = 0; i < crate.getDisplayGuiSettings().getHeight(); i++) {
                    pane.set(col, i, GuiFactory.displayable(
                            UtilConfigItem.fromConfigItem(rewards.get(Reference.RANDOM.nextInt(rewards.size())).getDisplayItem())));
                }
            }
        }

        private List<Integer> get3Cols(List<Integer> displaySlots) {
            if (checkCasinoDisplayCol(displaySlots)) {
                return displaySlots;
            } else {
                return DEFAULT_COLS;
            }
        }

        private boolean checkCasinoDisplayCol(List<Integer> displaySlots) {
            int validColSize = 3;
            if (displaySlots.size() != validColSize) {
                return false;
            }
            Set<Integer> set = new HashSet<>(displaySlots);
            if (set.size() != validColSize) {
                return false;
            }
            for (int num : set) {
                if (num < 0 || num > 8) {
                    return false;
                }
            }
            return true;
        }
    }

    public static IGuiTickHandler getFromElem(EnumGuiPattern guiPattern) {
        switch (guiPattern) {
            case kuji:
                return getInstance(KujiHandler.class);
            case casino:
                return getInstance(CasinoHandler.class);
            case standard:
            default:
                return getInstance(StandardHandler.class);
        }
    }

    private static final Map<Class<?>, Object> INSTANCES = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> clazz) {
        return (T) INSTANCES.computeIfAbsent(clazz, GuiTickHandlerFactory::createInstance);
    }

    private static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }

}
