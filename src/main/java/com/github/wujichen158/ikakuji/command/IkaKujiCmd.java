package com.github.wujichen158.ikakuji.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.lib.Reference;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;

@Command(
        value = {
                Reference.MOD_ID,
                "kuji"
        }
)
@SubCommands({
        GiveCmd.class,
        ListCmd.class,
        OpenCmd.class,
        ReloadCmd.class,
})
public class IkaKujiCmd {
    @CommandProcessor
    public void run(@Sender CommandSource sender) {
        IkaKujiLocaleCfg.Commands commands = IkaKuji.getInstance().getLocale().getCommands();
        sender.sendSystemMessage(MsgUtil.colorMsg(commands.getCmdTitle()));
        commands.getCmds().forEach(cmd -> {
            sender.sendSystemMessage(MsgUtil.colorMsg(cmd));
        });
    }
}
