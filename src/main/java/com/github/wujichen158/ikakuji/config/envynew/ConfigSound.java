package com.github.wujichen158.ikakuji.config.envynew;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.util.SoundCategory;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ConfigSound {
    private String sound;
    private float volume;
    private float pitch;
    private SoundCategory source;

    public ConfigSound(String sound, float volume, float pitch, SoundCategory source) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.source = source;
    }

    public ConfigSound(String sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.source = SoundCategory.MASTER;
    }

    public ConfigSound() {
    }

    public void playSound(EntityPlayerMP... players) {
        for (EntityPlayerMP player : players) {
            player.connection.sendPacket(new SPacketCustomSound(this.sound, this.source, player.posX, player.posY, player.posZ, 1.0F, 1.0F));
        }
    }
}