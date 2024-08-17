package com.github.wujichen158.ikakuji.command.global;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.command.completion.CrateNameCompleter;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.EnumGlobalRewardRule;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.GlobalKujiFactory;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import com.github.wujichen158.ikakuji.util.TimeUtil;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.Util;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(
        value = "hold"
)
@Permissible(PermissionNodes.HOLD_NODE)
public class HoldCmd {

    @CommandProcessor
    public void run(@Sender ICommandSource sender,
                    @Argument String globalKujiName,
                    @Completable(CrateNameCompleter.class) @Argument String crateName,
                    @Argument String startTime,
                    @Argument String endTime, String[] args) {
        IkaKujiLocaleCfg.Commands commands = IkaKuji.getInstance().getLocale().getCommands();

        // Judge existence
        if (GlobalKujiFactory.isExisted(globalKujiName)) {
            sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getGlobalKujiExisted(), globalKujiName),
                    Util.NIL_UUID);
            return;
        }

        // Judge crate
        KujiObj.Crate crate = CrateFactory.get(crateName);
        if (crate == null) {
            sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getInvalidCrateName(), globalKujiName),
                    Util.NIL_UUID);
            return;
        }

        // Judge time
        LocalDateTime startDateTime;
        if (Reference.CURRENT_TIME_SYMBOLS.contains(startTime.toLowerCase(Locale.ROOT))) {
            startDateTime = LocalDateTime.now();
            startTime = TimeUtil.parseDateTime(startDateTime);
        } else {
            startDateTime = TimeUtil.parseTimeString(startTime);
            if (startDateTime == null) {
                sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getTimeFormatError()), Util.NIL_UUID);
                return;
            }
        }

        LocalDateTime endDateTime = null;
        if (Reference.INF_TIME_SYMBOLS.contains(endTime.toLowerCase(Locale.ROOT))) {
            endTime = "0";
        } else {
            endDateTime = TimeUtil.parseTimeString(endTime);
            if (endDateTime == null) {
                sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getTimeFormatError()), Util.NIL_UUID);
                return;
            }
        }

        // Create global data obj
        EnumGlobalRewardRule globalRewardRule;
        if (args.length > 0) {
            try {
                globalRewardRule = EnumGlobalRewardRule.valueOf(args[0]);
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getInvalidRewardRule()), Util.NIL_UUID);
                return;
            }
        } else {
            globalRewardRule = EnumGlobalRewardRule.common;
        }

        KujiObj.GlobalData globalData = prepareGlobalKujiData(crateName, globalRewardRule,
                startTime, endTime, startDateTime, endDateTime, crate);

        GlobalKujiFactory.create(globalKujiName, globalData);
        sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getGlobalKujiCreated(), globalKujiName), Util.NIL_UUID);

        // If time is up, start immediately
        if (globalData.getStartDateTime().isBefore(LocalDateTime.now())) {
            GlobalKujiFactory.start(globalKujiName);
        }

    }

    /**
     * New a globalData and set its transient fields
     *
     * @param crateName
     * @param globalRewardRule
     * @param startTime
     * @param endTime
     * @param startDateTime
     * @param endDateTime
     * @param crate
     * @return
     */
    private static KujiObj.GlobalData prepareGlobalKujiData(String crateName, EnumGlobalRewardRule globalRewardRule,
                                                            String startTime, String endTime,
                                                            LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                            KujiObj.Crate crate) {
        KujiObj.GlobalData globalData = new KujiObj.GlobalData(crateName, globalRewardRule, startTime, endTime);

        globalData.setStartDateTime(startDateTime);
        globalData.setEndDateTime(endDateTime);
        List<KujiObj.GlobalDataEntry> globalDataList;

        switch (globalData.getRewardRule()) {
            case flat:
                globalDataList = Lists.newArrayList();
                crate.getRewards().forEach(reward -> {
                    for (int i = 0; i < reward.getAmountPerKuji(); i++) {
                        globalDataList.add(new KujiObj.GlobalDataEntry(reward.getId()));
                    }
                });
                Collections.shuffle(globalDataList);
                break;
            case common:
            default:
                globalDataList = Stream.generate(KujiObj.GlobalDataEntry::new)
                        .limit(crate.getRewardTotalLazy())
                        .collect(Collectors.toList());
        }
        globalData.setData(globalDataList);

        return globalData;
    }
}
