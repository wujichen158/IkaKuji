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
        private String prefix = "&b&l[虚拟对战NPC] &a➠ ";
        private String configReloaded = "&f插件重载成功";
        private String noSuchNpc = "&f不存在该npc文件";
        private String noPlayerPokemons = "&f你没有可对战的宝可梦";
        private String pokemonFileError = "&f该npc对应的宝可梦文件存取有误，暂时跳过";
        private String battleRuleFileError = "&f该npc对应的对战规则文件存取有误，暂时跳过";
        private String noNpcPokemons = "&f该npc文件中没有宝可梦或是宝可梦格式有误";
        private String notNpc = "&f你选择的不是一个训练家npc";
        private String alreadyExisted = "&f已含有该名称的npc";
        private String npcSaved = "&fnpc数据存入成功";
        private String rightClickBlankName = "&f请输入正确的npc名称";
        private String rightClick = "&f请右键npc训练师存入数据";
        private String alreadyBeaten = "&f该npc已经被击败过一次了";
        private String reachDailyWinLimit = "&f战胜该npc的奖励已达今日获取上限";
        private String reachWeeklyWinLimit = "&f战胜该npc的奖励已达本周获取上限";
        private String reachDailyLoseLimit = "&f败给该npc的惩罚次数已达今日上限";
        private String reachWeeklyLoseLimit = "&f败给该npc的惩罚次数已达本周上限";
        private String cleanFail = "&f玩家 %s 的日常/周常数据重置异常，请手动查看";
        private String cleanSuccess = "&f成功重置了所有玩家的日常/周常数据";
        private String cleanPlayerSuccess = "&f成功重置了 %s 的日常/周常数据";

        public String getPrefix() {
            return prefix;
        }

        public String getConfigReloaded() {
            return configReloaded;
        }

        public String getNoSuchNpc() {
            return noSuchNpc;
        }

        public String getNoPlayerPokemons() {
            return noPlayerPokemons;
        }

        public String getPokemonFileError() {
            return pokemonFileError;
        }

        public String getBattleRuleFileError() {
            return battleRuleFileError;
        }

        public String getNoNpcPokemons() {
            return noNpcPokemons;
        }

        public String getNotNpc() {
            return notNpc;
        }

        public String getAlreadyExisted() {
            return alreadyExisted;
        }

        public String getNpcSaved() {
            return npcSaved;
        }

        public String getRightClickBlankName() {
            return rightClickBlankName;
        }

        public String getRightClick() {
            return rightClick;
        }

        public String getAlreadyBeaten() {
            return alreadyBeaten;
        }

        public String getReachDailyWinLimit() {
            return reachDailyWinLimit;
        }

        public String getReachWeeklyWinLimit() {
            return reachWeeklyWinLimit;
        }

        public String getReachDailyLoseLimit() {
            return reachDailyLoseLimit;
        }

        public String getReachWeeklyLoseLimit() {
            return reachWeeklyLoseLimit;
        }

        public String getCleanFail() {
            return cleanFail;
        }

        public String getCleanSuccess() {
            return cleanSuccess;
        }

        public String getCleanPlayerSuccess() {
            return cleanPlayerSuccess;
        }
    }
}
