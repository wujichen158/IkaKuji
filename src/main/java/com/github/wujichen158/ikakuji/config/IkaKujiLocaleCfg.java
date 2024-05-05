package com.github.wujichen158.ikakuji.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigPath(Reference.LOCALE_PATH)
@ConfigSerializable
public class IkaKujiLocaleCfg extends AbstractYamlConfig {

    private Messages messages = new Messages();
    private Logs logs = new Logs();
    private Commands commands = new Commands();

    public Messages getMessages() {
        return messages;
    }

    public Logs getLogs() {
        return logs;
    }

    public Commands getCommands() {
        return commands;
    }

    @ConfigSerializable
    public static class Messages {
        private String prefix = "&e&l[&6&lIka Kuji&e&l]";
        private String noOpenPermMsg = "&fYou don't have permission to open crate %s";
        private String noPreviewPermMsg = "&fYou don't have permission to preview crate %s";
        private String needItemCrateMsg = "&fYou need at least %d %s &fcrate(s) to join this kuji!";
        private String needKeyMsg = "&fYou need at least %d %s &fkey(s) to join this kuji!";
        private String incompletePreKujiMsg = "&fYou will also need to complete these pre-kuji: %s";
        private String oneRoundMsg = "&fThis crate can only be opened for one round!";
        private String insufficientInvSizeMsg = "&fYou don't have enough inventory space to receive the reward";
        private String noAvailableRwdMsg = "&fThis crate has no available rewards!";
        private String openCrateForPlayerMsg = "&fSuccessfully open crate %s &ffor player %s";
        private String lastRewardFailMsg = "&fFailed to deliver the last-shot reward of crate %s&f, please contact admin to deliver again";
        private String rewardRemainCount = "&6%d remaining";
        private String probPerReward = "&6probability per reward is about: %.2f%%";

        public String getPrefix() {
            return prefix;
        }

        public String getNoOpenPermMsg() {
            return noOpenPermMsg;
        }

        public String getNoPreviewPermMsg() {
            return noPreviewPermMsg;
        }

        public String getNeedItemCrateMsg() {
            return needItemCrateMsg;
        }

        public String getNeedKeyMsg() {
            return needKeyMsg;
        }

        public String getIncompletePreKujiMsg() {
            return incompletePreKujiMsg;
        }

        public String getOneRoundMsg() {
            return oneRoundMsg;
        }

        public String getInsufficientInvSizeMsg() {
            return insufficientInvSizeMsg;
        }

        public String getNoAvailableRwdMsg() {
            return noAvailableRwdMsg;
        }

        public String getOpenCrateForPlayerMsg() {
            return openCrateForPlayerMsg;
        }

        public String getLastRewardFailMsg() {
            return lastRewardFailMsg;
        }

        public String getRewardRemainCount() {
            return this.rewardRemainCount;
        }

        public String getProbPerReward() {
            return probPerReward;
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

    @ConfigSerializable
    public static class Commands {
        private String configReloaded = "&fSuccessfully reloaded!";
        private String giveKey = "&fYou give the key of crate %s &fto player %s";
        private String noKey = "&fCrate %s %fhas no key";
        private String giveCrate = "&fYou give a crate %s to player %s";
        private String invalidItemCrate = "&fInvalid item crate name %s%f!";
        private String notItemCrate = "&fCrate %s %fis not an item chest and cannot be given";
        private String listTitle = "&6————————Kujis On-going————————";
        private String listElemPrefix = "&e· %s";
        private String listFooter = "&6——————————————————————————————";
        private String cmdTitle = "&6————————Ika Kuji commands————————";
        private List<String> cmds = Lists.newArrayList(
                "&f /kuji give &e<key/crate> <player> <crate_name>&f: &6Give player a specified crate/key. Crate can only be item",
                "&f /kuji list &e[page_number]&f: &6List all kujis you're currently running. The page number will be 1 if you don't specify it",
                "&f /kuji open &e<player> <crate_name>&f: &6Open a specified kuji for the target player if the player satisfies the condition of that kuji",
                "&f /kuji reload&f: &6Reload Ika Kuji to apply all new changes"
        );

        public String getConfigReloaded() {
            return configReloaded;
        }

        public String getGiveKey() {
            return giveKey;
        }

        public String getNoKey() {
            return noKey;
        }

        public String getGiveCrate() {
            return giveCrate;
        }

        public String getInvalidItemCrate() {
            return invalidItemCrate;
        }

        public String getNotItemCrate() {
            return notItemCrate;
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

        public String getCmdTitle() {
            return cmdTitle;
        }

        public List<String> getCmds() {
            return cmds;
        }
    }
}
