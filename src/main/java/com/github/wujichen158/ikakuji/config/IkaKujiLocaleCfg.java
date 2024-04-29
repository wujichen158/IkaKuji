package com.github.wujichen158.ikakuji.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.github.wujichen158.ikakuji.lib.Reference;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigPath(Reference.LOCALE_PATH)
@ConfigSerializable
public class IkaKujiLocaleCfg extends AbstractYamlConfig {

    private Messages messages = new Messages();

    public Messages getMessages() {
        return messages;
    }

    @ConfigSerializable
    public static class Messages {
        private String prefix = "&e&l[&6&lIka Kuji&e&l]";
        private String configReloaded = "&fSuccessfully reloaded!";
        private String giveKeyMsg = "&fYou give the key of crate %s to %s";
        private String giveCrateMsg = "&fYou give a crate %s to %s";
        private String needKeyMsg = "&fYou need a %s to join this kuji!";
        private String oneRoundMsg = "&fThis crate can only be opened for one round!";
        private String noAvailableRwdMsg = "&fThis crate has no available rewards!";
        private String openCrateForPlayerMsg = "&fSuccessfully open crate %s for %s";
        private String rewardRemainCount = "&f%d remaining";

        public String getPrefix() {
            return prefix;
        }

        public String getConfigReloaded() {
            return configReloaded;
        }

        public String getGiveKeyMsg() {
            return giveKeyMsg;
        }

        public String getGiveCrateMsg() {
            return giveCrateMsg;
        }

        public String getNeedKeyMsg() {
            return needKeyMsg;
        }

        public String getOneRoundMsg() {
            return oneRoundMsg;
        }

        public String getNoAvailableRwdMsg() {
            return noAvailableRwdMsg;
        }

        public String getOpenCrateForPlayerMsg() {
            return openCrateForPlayerMsg;
        }

        public String getRewardRemainCount() {
            return this.rewardRemainCount;
        }
    }
}
