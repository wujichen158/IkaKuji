package com.github.wujichen158.ikakuji.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;

@Command(
        value = "reload"
)
@Permissible(PermissionNodes.RELOAD_NODE)
public class ReloadCmd {

    @CommandProcessor
    public void run(@Sender CommandSource sender) {
        IkaKuji.getInstance().loadConfig();
        sender.sendSystemMessage(MsgUtil.prefixedColorMsg(IkaKuji.getInstance().getLocale().getCommands().getConfigReloaded()));
    }
}
