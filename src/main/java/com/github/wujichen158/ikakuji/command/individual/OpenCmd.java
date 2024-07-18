package com.github.wujichen158.ikakuji.command.individual;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.command.completion.player.PlayerTabCompleter;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.command.completion.CrateNameCompleter;
import com.github.wujichen158.ikakuji.kuji.KujiExecutor;
import com.github.wujichen158.ikakuji.lib.PermissionNodes;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Command(
        value = "open"
)
@Permissible(PermissionNodes.OPEN_NODE)
public class OpenCmd {

    @CommandProcessor
    public void run(@Sender ICommandSource sender,
                    @Completable(PlayerTabCompleter.class) @Argument ServerPlayerEntity targetPlayer,
                    @Completable(CrateNameCompleter.class) @Argument String crateName) {
        Optional.ofNullable(CrateFactory.get(crateName)).ifPresent(crate -> KujiExecutor.executeKujiLogic(targetPlayer, crate, new AtomicInteger(1)));
        sender.sendMessage(
                MsgUtil.prefixedColorMsg(IkaKuji.getInstance().getLocale().getMessages().getOpenCrateForPlayerMsg(), crateName, targetPlayer.getName().getString()),
                targetPlayer.getUUID());
    }
}
