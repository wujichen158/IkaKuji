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
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                if (timer.get() >= (2 * crate.getSpinDuration())) {

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
                        //TODO: See whether this works
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
                    pane.set(slot % 9, slot / 9, pane.get(lastSlot % 9, lastSlot / 9));
                }

                int slot = spinSlots.get(0);

                int subtraction = spinSlots.indexOf(crate.getFinalRewardPosition());
                subtraction = subtraction == -1 ? 4 : spinSlots.size() - subtraction;

                pane.set(slot % 9, slot / 9, GuiFactory.displayable(
                        timer.get() == ((2 * crate.getSpinDuration()) - subtraction) ?
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
                    cleared.set(true);
                }
            };
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
            ItemStack coverItem = UtilConfigItem.fromConfigItem(crate.getCoverItem());
            for (Integer slot : crate.getDisplaySlots()) {
                pane.set(slot % 9, slot / 9, GuiFactory.displayable(coverItem));
            }
        }

    }

    public static class CasinoHandler implements IGuiTickHandler {
        @Override
        public Consumer<Pane> handle(KujiObj.Crate crate, ForgeEnvyPlayer player, List<KujiObj.Reward> rewards, AtomicInteger timer, AtomicBoolean cleared, ItemStack rewardItem) {
            return pane -> {
                timer.incrementAndGet();
                // Reward settlement
                if (timer.get() >= (2 * crate.getSpinDuration())) {

                    if (!cleared.get()) {
                        cleared.set(true);
                        KujiObj.Reward finalReward = rewards.get(0);
                        KujiExecutor.playSound(finalReward.getWinSound(), player.getParent());
                        int counter = 0;
                        for (ConfigItem fillerItem : crate.getDisplayGuiSettings().getFillerItems()) {
                            if (!fillerItem.isEnabled() || counter == crate.getFinalRewardPosition()) {
                                ++counter;
                                continue;
                            }

                            pane.set(counter % 9, counter / 9, GuiFactory.displayable(UtilConfigItem.fromConfigItem(fillerItem)));
                            ++counter;
                        }
                    }

                    pane.set(crate.getFinalRewardPosition() % 9, crate.getFinalRewardPosition() / 9,
                            GuiFactory.displayable(rewardItem));
                    return;
                }

                // Rolling
                KujiExecutor.playSound(crate.getRollSound(), player.getParent());
                List<Integer> spinSlots = crate.getDisplaySlots();

                for (int i = spinSlots.size() - 1; i > 0; i--) {
                    int slot = spinSlots.get(i);
                    int lastSlot = spinSlots.get(i - 1);
                    pane.set(slot % 9, slot / 9, pane.get(lastSlot % 9, lastSlot / 9));
                }

                int slot = spinSlots.get(0);

                int subtraction = spinSlots.indexOf(crate.getFinalRewardPosition());
                subtraction = subtraction == -1 ? 4 : spinSlots.size() - subtraction;

                pane.set(slot % 9, slot / 9, GuiFactory.displayable(
                        timer.get() == ((2 * crate.getSpinDuration()) - subtraction) ?
                                rewardItem : UtilConfigItem.fromConfigItem(rewards.get(Reference.RANDOM.nextInt(rewards.size())).getDisplayItem())));
            };
        }

        @Override
        public void initDisplaySlots(Pane pane, KujiObj.Crate crate, List<KujiObj.Reward> rewards) {

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
