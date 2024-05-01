package com.github.wujichen158.ikakuji.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.Util;

@Command(
        value = "reload"
)
@Permissible(PermissionNodes.RELOAD_NODE)
public class ReloadCmd {

    @CommandProcessor
    public void run(@Sender ICommandSource sender) {
        IkaKuji.getInstance().loadConfig();
        sender.sendMessage(MsgUtil.prefixedColorMsg(IkaKuji.getInstance().getLocale().getCommands().getConfigReloaded()), Util.NIL_UUID);
    }
}
