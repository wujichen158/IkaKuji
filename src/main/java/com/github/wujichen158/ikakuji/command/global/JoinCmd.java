package com.github.wujichen158.ikakuji.command.global;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.command.completion.GlobalKujiCompleter;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.config.KujiObj;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.CmdUtil;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.GlobalKujiFactory;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import net.minecraft.entity.player.ServerPlayerEntity;

@Command(
        value = "join"
)
public class JoinCmd {

    @CommandProcessor
    public void run(@Sender ServerPlayerEntity player,
                    @Completable(GlobalKujiCompleter.class) @Argument String globalKujiName,
                    String[] args) {
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();
        IkaKujiLocaleCfg.Commands commands = IkaKuji.getInstance().getLocale().getCommands();

        // Existence check
        if (!GlobalKujiFactory.isExisted(globalKujiName)) {
            player.sendMessage(MsgUtil.prefixedColorMsg(commands.getGlobalKujiNotExisted(), globalKujiName), player.getUUID());
            return;
        }

        // Holding check
        if (!GlobalKujiFactory.isHolding(globalKujiName)) {
            player.sendMessage(MsgUtil.prefixedColorMsg(
                    IkaKuji.getInstance().getLocale().getCommands().getGlobalKujiNotInTime()), player.getUUID());
            return;
        }

        // Perm check
        if (!IkaKuji.getInstance().getCommandFactory()
                .hasPermission(player, PermissionNodes.getJoinPermNode(globalKujiName))) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoJoinPermMsg(), globalKujiName),
                    player.getUUID());
            return;
        }

        // Base crate check
        KujiObj.GlobalData globalData = GlobalKujiFactory.get(globalKujiName);
        KujiObj.Crate crate = CrateFactory.get(globalData.getCrateName());
        if (crate == null) {
            player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoSuchCrateMsg(), globalKujiName),
                    player.getUUID());
            return;
        }

        // Don't do keys check here. Check when clicking

        // Page check
        int page = CmdUtil.getPageFromArgs(args);

        // Run
        KujiExecutor.executeGlobalKujiLogic(page, globalData, crate, player);

    }
}
