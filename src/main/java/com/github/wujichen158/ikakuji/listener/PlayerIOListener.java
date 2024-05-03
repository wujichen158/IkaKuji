package com.github.wujichen158.ikakuji.listener;

import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerIOListener {
    @SubscribeEvent
    public void onPlayerIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerKujiFactory.register(event.player.getUniqueID());
    }

    @SubscribeEvent
    public void onPlayerOut(PlayerEvent.PlayerLoggedOutEvent event) {
        //Don't need to write files last, since we write file every time
        PlayerKujiFactory.unregister(event.player.getUniqueID());
    }
}
