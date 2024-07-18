package com.github.wujichen158.ikakuji.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.command.global.HoldCmd;
import com.github.wujichen158.ikakuji.command.global.JoinCmd;
import com.github.wujichen158.ikakuji.command.global.StopCmd;
import com.github.wujichen158.ikakuji.command.individual.GiveCmd;
import com.github.wujichen158.ikakuji.command.individual.OpenCmd;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.Util;

@Command(
        value = {
                Reference.MOD_ID,
                "kuji"
        }
)
@SubCommands({
        GiveCmd.class,
        OpenCmd.class,
        HoldCmd.class,
        JoinCmd.class,
        StopCmd.class,
        PreviewCmd.class,
        ListCmd.class,
        ReloadCmd.class,
})
public class IkaKujiCmd {
    @CommandProcessor
    public void run(@Sender ICommandSource sender) {
        IkaKujiLocaleCfg.Commands commands = IkaKuji.getInstance().getLocale().getCommands();
        sender.sendMessage(MsgUtil.colorMsg(commands.getCmdTitle()), Util.NIL_UUID);
        commands.getCmds().forEach(cmd -> {
            sender.sendMessage(MsgUtil.colorMsg(cmd), Util.NIL_UUID);
        });
    }
}
