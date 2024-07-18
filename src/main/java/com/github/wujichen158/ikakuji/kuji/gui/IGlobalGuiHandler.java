package com.github.wujichen158.ikakuji.kuji.gui;

import com.envyful.api.forge.config.UtilConfigInterface;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.pane.Pane;
import com.github.wujichen158.ikakuji.config.KujiObj;

import java.util.Optional;
import java.util.function.Consumer;

public interface IGlobalGuiHandler {

    /**
     * Handle GUI changing mode
     *
     * @param globalKujiData
     * @param crate
     * @param player
     * @return
     */
    Consumer<Pane> handle(int page, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, ForgeEnvyPlayer player);

    /**
     * Init all GUI
     *
     * @param pane
     * @param globalKujiData
     * @param crate
     * @param player
     */
    default void initGui(Pane pane, int page, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, ForgeEnvyPlayer player) {
        // Draw bg
        UtilConfigInterface.fillBackground(pane, crate.getDisplayGuiSettings());

        // Draw indicators
        Optional.ofNullable(crate.getIndicators()).ifPresent(indicators ->
                indicators.forEach(indicator ->
                        UtilConfigItem.builder().extendedConfigItem(pane, UtilConfigItem.fromConfigItem(indicator), indicator))
        );

        // Draw init display slots
        initDisplaySlots(pane, page, globalKujiData, crate, player);
    }

    /**
     * Init the display slots
     *
     * @param pane
     * @param page
     * @param crate
     * @param player
     */
    void initDisplaySlots(Pane pane, int page, KujiObj.GlobalData globalKujiData, KujiObj.Crate crate, ForgeEnvyPlayer player);

}
