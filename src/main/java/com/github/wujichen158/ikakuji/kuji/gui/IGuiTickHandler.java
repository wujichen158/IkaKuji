package com.github.wujichen158.ikakuji.kuji.gui;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.pane.Pane;
import com.github.wujichen158.ikakuji.config.KujiObj;
import net.minecraft.item.ItemStack;

import java.util.List;
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
     * Init the display slots
     *
     * @param pane
     * @param crate
     * @param rewards
     */
    void initDisplaySlots(Pane pane, KujiObj.Crate crate, List<KujiObj.Reward> rewards);
}
