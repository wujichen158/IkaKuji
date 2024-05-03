package com.github.wujichen158.ikakuji.listener;

import com.github.wujichen158.ikakuji.util.PlayerKujiFactory;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerIOListener {
    @SubscribeEvent
    public void onPlayerIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerKujiFactory.register(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onPlayerOut(PlayerEvent.PlayerLoggedOutEvent event) {
        //Don't need to write files last, since we write file every time
        PlayerKujiFactory.unregister(event.getEntity().getUUID());
    }
}
