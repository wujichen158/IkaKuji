package com.github.wujichen158.ikakuji.command.global;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.command.completion.GlobalKujiCompleter;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.GlobalKujiFactory;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.Util;

@Command(
        value = "stop"
)
@Permissible(PermissionNodes.STOP_NODE)
public class StopCmd {
    @CommandProcessor
    public void run(@Sender ICommandSource sender,
                    @Completable(GlobalKujiCompleter.class) @Argument String globalKujiName) {
        IkaKujiLocaleCfg.Commands commands = IkaKuji.getInstance().getLocale().getCommands();
        if (GlobalKujiFactory.forceStop(globalKujiName)) {
            sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getGlobalKujiStopped(), globalKujiName),
                    Util.NIL_UUID);
        } else {
            sender.sendMessage(MsgUtil.prefixedColorMsg(commands.getGlobalKujiInvalid(), globalKujiName),
                    Util.NIL_UUID);
        }
    }
}
