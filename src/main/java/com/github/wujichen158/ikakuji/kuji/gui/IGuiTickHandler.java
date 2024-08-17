package com.github.wujichen158.ikakuji.kuji.gui;

import com.envyful.api.forge.config.UtilConfigInterface;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.pane.Pane;
import com.github.wujichen158.ikakuji.config.KujiObj;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public interface IGuiTickHandler {

    /**
     * Handle GUI changing mode
     *
     * @param crate
     * @param player
     * @param rewards
     * @param timer
     * @param cleared
     * @param rewardItem
     * @return
     */
    Consumer<Pane> handle(KujiObj.Crate crate, ForgeEnvyPlayer player, List<KujiObj.Reward> rewards, AtomicInteger timer, AtomicBoolean cleared, ItemStack rewardItem);

    /**
     * Init all GUI
     *
     * @param pane
     * @param crate
     * @param rewards
     * @param rewardItem
     */
    default void initGui(Pane pane, KujiObj.Crate crate, List<KujiObj.Reward> rewards, ItemStack rewardItem) {
        // Draw bg
        UtilConfigInterface.fillBackground(pane, crate.getDisplayGuiSettings());

        // Draw indicators
        Optional.ofNullable(crate.getIndicators()).ifPresent(indicators ->
                indicators.forEach(indicator ->
                        UtilConfigItem.builder().extendedConfigItem(pane, UtilConfigItem.fromConfigItem(indicator), indicator))
        );

        // Draw init display slots. rewardItem for kuji pattern
        initDisplaySlots(pane, crate, rewards, rewardItem);
    }

    /**
     * Init the display slots
     *
     * @param pane
     * @param crate
     * @param rewards
     * @param rewardItem
     */
    void initDisplaySlots(Pane pane, KujiObj.Crate crate, List<KujiObj.Reward> rewards, ItemStack rewardItem);


}
