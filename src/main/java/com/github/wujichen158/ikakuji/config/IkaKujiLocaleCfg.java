package com.github.wujichen158.ikakuji.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.github.wujichen158.ikakuji.lib.Reference;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigPath(Reference.LOCALE_PATH)
@ConfigSerializable
public class IkaKujiLocaleCfg extends AbstractYamlConfig {

    private Messages messages = new Messages();
    private Logs logs = new Logs();

    public Messages getMessages() {
        return messages;
    }

    public Logs getLogs() {
        return logs;
    }

    @ConfigSerializable
    public static class Messages {
        private String prefix = "&e&l[&6&lIka Kuji&e&l]";
        private String configReloaded = "&fSuccessfully reloaded!";
        private String giveKeyMsg = "&fYou give the key of crate %s to %s";
        private String giveCrateMsg = "&fYou give a crate %s to %s";
        private String listTitle = "&6————————Kujis On-going————————";
        private String listElemPrefix = "&e· %s";
        private String listFooter = "&6——————————————————————————————";
        private String noOpenPermMsg = "&fYou don't have permission to open crate %s";
        private String noPreviewPermMsg = "&fYou don't have permission to preview crate %s";
        private String needKeyMsg = "&fYou need a %s to join this kuji!";
        private String incompletePreKuji = "&fYou will also need to complete these pre-kuji: %s";
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

        public String getListTitle() {
            return listTitle;
        }

        public String getListElemPrefix() {
            return listElemPrefix;
        }

        public String getListFooter() {
            return listFooter;
        }

        public String getNoOpenPermMsg() {
            return noOpenPermMsg;
        }

        public String getNoPreviewPermMsg() {
            return noPreviewPermMsg;
        }

        public String getNeedKeyMsg() {
            return needKeyMsg;
        }

        public String getIncompletePreKuji() {
            return incompletePreKuji;
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

    @ConfigSerializable
    public static class Logs {
        private Boolean enable = true;
        private String winRewardLog = "%player% win %reward_id% (i.e. %reward_name%) in crate %crate%";
        private String lastShotLog = " (with last shot)";

        public Boolean getEnable() {
            return enable;
        }

        public String getWinRewardLog() {
            return winRewardLog;
        }

        public String getLastShotLog() {
            return lastShotLog;
        }
    }
}
