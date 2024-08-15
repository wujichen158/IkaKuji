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
        private String noOpenPermMsg = "&fYou don't have permission to open crate &6%s";
        private String noPreviewPermMsg = "&fYou don't have permission to preview crate &6%s";
        private String noJoinPermMsg = "&fYou don't have permission to join global kuji &6%s";
        private String noSuchCrateMsg = "&fThere's no base crate named &6%s&f, please contact admin to have a check";
        private String needItemCrateMsg = "&fYou need at least &6%d %s &fcrate(s) to join this kuji!";
        private String needKeyMsg = "&fYou need at least &6%d %s &fkey(s) to join this kuji!";
        private String incompletePreKujiMsg = "&fYou will also need to complete these pre-kuji: &6%s";
        private String oneRoundMsg = "&fThis crate can only be opened for one round!";
        private String insufficientInvSizeMsg = "&fYou don't have enough inventory space to receive the reward";
        private String noAvailableRwdMsg = "&fThis crate has no available rewards!";
        private String openCrateForPlayerMsg = "&fSuccessfully open crate &6%s &ffor player &6%s";
        private String lastRewardFailMsg = "&fFailed to deliver the last-shot reward of crate &6%s&f, please contact admin to deliver again";
        private String globalKujiStartMsg = "&eGlobal Kuji &6%s &estarted! Use &6/kuji join &eto join!";
        private String dash = "&6————————————————";
        private String rewardRemainCount = "&6%d remaining";
        private String probPerReward = "&6probability per reward is about: %.2f%%";
        private String winnerName = "&6Winner: &e%s";
        private String winTime = "&6Won at &e%s";
        private String winWithLastShot = "&5With last shot";

        public String getPrefix() {
            return prefix;
        }

        public String getNoOpenPermMsg() {
            return noOpenPermMsg;
        }

        public String getNoPreviewPermMsg() {
            return noPreviewPermMsg;
        }

        public String getNoJoinPermMsg() {
            return noJoinPermMsg;
        }

        public String getNoSuchCrateMsg() {
            return noSuchCrateMsg;
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

        public String getDash() {
            return dash;
        }

        public String getLastRewardFailMsg() {
            return lastRewardFailMsg;
        }

        public String getGlobalKujiStartMsg() {
            return globalKujiStartMsg;
        }

        public String getRewardRemainCount() {
            return this.rewardRemainCount;
        }

        public String getProbPerReward() {
            return probPerReward;
        }

        public String getWinnerName() {
            return winnerName;
        }

        public String getWinTime() {
            return winTime;
        }

        public String getWinWithLastShot() {
            return winWithLastShot;
        }
    }

    @ConfigSerializable
    public static class Logs {
        private Boolean enable = true;
        private String winRewardLog = "%player% win %reward_id% (i.e. %reward_name%) in crate %crate%";
        private String winGlobalRewardLog = "%player% win %reward_id% (i.e. %reward_name%) in global kuji %global_kuji%";
        private String lastShotLog = " (with last shot)";

        public Boolean getEnable() {
            return enable;
        }

        public String getWinRewardLog() {
            return winRewardLog;
        }

        public String getWinGlobalRewardLog() {
            return winGlobalRewardLog;
        }

        public String getLastShotLog() {
            return lastShotLog;
        }
    }

    @ConfigSerializable
    public static class Commands {
        private String configReloaded = "&fSuccessfully reloaded!";
        private String giveKey = "&fYou give the key of crate &6%s &fto player %s";
        private String noKey = "&fCrate &6%s &fhas no key";
        private String giveCrate = "&fYou give a crate &6%s &fto player &6%s";
        private String invalidItemCrate = "&fInvalid item crate name &6%s&f!";
        private String notItemCrate = "&fCrate &6%s &fis not an item chest and cannot be given";
        private String invalidCrateName = "&fThere's no such crate &6%s";
        private String globalKujiCreated = "&fSuccessfully create global kuji &6%s";
        private String globalKujiStopped = "&fForce stop global kuji &6%s";
        private String globalKujiInvalid = "&fGlobal kuji &6%s &f doesn't exist or has expired";
        private String globalKujiExisted = "&fGlobal kuji &6%s &fhas already been created";
        private String timeFormatError = "&fThe time format isn't correct. Please use yyyyMMddHHmm format like 202407121647";
        private String invalidRewardRule = "&fGlobal kuji reward rule you provided doesn't exist, please have a check";
        private String globalKujiNotExisted = "&fGlobal kuji &6%s &fdoesn't exist";
        private String globalKujiNotInTime = "&fGlobal kuji &6%s &fis not currently in the holding period";
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

        public String getInvalidCrateName() {
            return invalidCrateName;
        }

        public String getGlobalKujiCreated() {
            return globalKujiCreated;
        }

        public String getGlobalKujiStopped() {
            return globalKujiStopped;
        }

        public String getGlobalKujiInvalid() {
            return globalKujiInvalid;
        }

        public String getGlobalKujiExisted() {
            return globalKujiExisted;
        }

        public String getTimeFormatError() {
            return timeFormatError;
        }

        public String getInvalidRewardRule() {
            return invalidRewardRule;
        }

        public String getGlobalKujiNotExisted() {
            return globalKujiNotExisted;
        }

        public String getGlobalKujiNotInTime() {
            return globalKujiNotInTime;
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
