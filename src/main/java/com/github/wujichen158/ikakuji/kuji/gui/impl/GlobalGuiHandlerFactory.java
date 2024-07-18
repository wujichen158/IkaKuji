package com.github.wujichen158.ikakuji.kuji.gui.impl;

import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.pane.Pane;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.EnumGlobalRewardRule;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.kuji.gui.IGlobalGuiHandler;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class GlobalGuiHandlerFactory {
    public static class GlobalKujiHandler implements IGlobalGuiHandler {
        @Override
        public Consumer<Pane> handle(int page, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, ForgeEnvyPlayer player) {
            return pane -> {
                List<Integer> displaySlots = crate.getDisplaySlots();
                int offset = (page - 1) * displaySlots.size();
                int pageSize = displaySlots.size();


                // Only traverse specified range to improve efficiency
                List<KujiObj.GlobalDataEntry> data = globalKujiData.getData();
                int dataSize = data.size();

                int slotIndex = 0;
                for (int i = offset; i < offset + pageSize && i < dataSize; i++) {
                    KujiObj.GlobalDataEntry globalDataEntry = data.get(i);
                    if (globalDataEntry.isSettled()) {
                        int slot = displaySlots.get(slotIndex);
                        setWonSlot(pane, crate, globalDataEntry, slot);
                    }
                    slotIndex++;
                }
            };
        }

        @Override
        public void initDisplaySlots(Pane pane, int page, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, ForgeEnvyPlayer player) {
            ItemStack coverItem = Optional.ofNullable(crate.getCoverItem()).map(UtilConfigItem::fromConfigItem).orElse(ItemStack.EMPTY);
            List<Integer> displaySlots = crate.getDisplaySlots();
            int offset = (page - 1) * displaySlots.size();
            int pageSize = displaySlots.size();


            // Only traverse specified range to improve efficiency
            List<KujiObj.GlobalDataEntry> data = globalKujiData.getData();
            int dataSize = data.size();

            int slotIndex = 0;
            for (int i = offset; i < offset + pageSize && i < dataSize; i++) {
                int slot = displaySlots.get(slotIndex);
                KujiObj.GlobalDataEntry globalDataEntry = data.get(i);
                if (globalDataEntry.isSettled()) {
                    setWonSlot(pane, crate, globalDataEntry, slot);
                } else {
                    int posX = slot % 9, posY = slot / 9;
                    int rewardIndex = i;
                    pane.set(posX, posY, GuiFactory.displayableBuilder(coverItem)
                            .clickHandler((envyPlayer, clickType) -> {
                                handleClick(pane, globalKujiData, crate, globalDataEntry, player,
                                        rewardIndex, posX, posY);
                            })
                            .build());
                }
                slotIndex++;
            }
            for (int i = dataSize; i < offset + pageSize; i++) {
                int slot = displaySlots.get(slotIndex);
                pane.set(slot % 9, slot / 9, GuiFactory.displayable(ItemStack.EMPTY));
                slotIndex++;
            }
        }

        private void handleClick(Pane pane, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate,
                                 KujiObj.GlobalDataEntry globalDataEntry, ForgeEnvyPlayer player,
                                 int rewardIndex, int posX, int posY) {
            //Check and take key
            PlayerEntity playerEntity = player.getParent();
            if (playerEntity != null &&
                    !KujiExecutor.checkAndTakeKey(crate.getKey(), crate.isConsumeKey(), playerEntity)) {
                playerEntity.sendMessage(MsgUtil.prefixedColorMsg(
                        IkaKuji.getInstance().getLocale().getMessages().getNeedKeyMsg(), crate.getKey().getAmount(),
                        crate.getKey().getName()), playerEntity.getUUID());
                return;
            }

            // Generate reward
            KujiObj.Reward reward = globalKujiData.getRewardRule() == EnumGlobalRewardRule.flat ?
                    genFlatReward(crate, globalDataEntry.getRewardId()) : genCommonReward(globalKujiData, crate);

            // Deliver reward and set pane
            setResult(pane, globalKujiData, crate, globalDataEntry, player, reward, rewardIndex, posX, posY);
        }

        private KujiObj.Reward genFlatReward(KujiObj.Crate crate, String rewardId) {
            return crate.getRewardMapLazy().get(rewardId);
        }

        private KujiObj.Reward genCommonReward(KujiObj.GlobalData globalKujiData, KujiObj.Crate crate) {
            // Generate available rewards list and total weight
            return KujiExecutor.genRandomReward(globalKujiData, crate);
        }

        private void setResult(Pane pane, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate,
                               KujiObj.GlobalDataEntry globalDataEntry, ForgeEnvyPlayer player,
                               KujiObj.Reward reward, int rewardIndex, int posX, int posY) {
            // Post process, including reward delivering and last shot judgement
            KujiExecutor.globalPostProcess(player, reward, globalKujiData, crate, rewardIndex);

            //TODO: Support showing last shot

            ItemStack itemStack = KujiExecutor.addWinnerLore(reward,
                    globalDataEntry.getPlayerName(), globalDataEntry.getWinTime());
            pane.set(posX, posY, GuiFactory.displayable(itemStack));
        }

        private void setWonSlot(Pane pane, KujiObj.Crate crate, KujiObj.GlobalDataEntry globalDataEntry, int slot) {
            KujiObj.Reward reward = crate.getRewardMapLazy().get(globalDataEntry.getRewardId());
            ItemStack rewardItem = KujiExecutor.addWinnerLore(reward,
                    globalDataEntry.getPlayerName(), globalDataEntry.getWinTime());
            pane.set(slot % 9, slot / 9, GuiFactory.displayable(rewardItem));
        }
    }

    public static IGlobalGuiHandler getHandler() {
        return getInstance(GlobalKujiHandler.class);
    }

    private static final Map<Class<?>, Object> INSTANCES = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> clazz) {
        return (T) INSTANCES.computeIfAbsent(clazz, GlobalGuiHandlerFactory::createInstance);
    }

    private static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }
}
