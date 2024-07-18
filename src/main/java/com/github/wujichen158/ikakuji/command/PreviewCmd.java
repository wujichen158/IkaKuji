package com.github.wujichen158.ikakuji.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.github.wujichen158.ikakuji.IkaKuji;
import com.github.wujichen158.ikakuji.command.completion.MixedKujiCompleter;
import com.github.wujichen158.ikakuji.command.completion.PreviewTypeCompleter;
import com.github.wujichen158.ikakuji.config.IkaKujiLocaleCfg;
import com.github.wujichen158.ikakuji.kuji.executor.PreviewExecutor;
import com.github.wujichen158.ikakuji.util.CmdUtil;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.GlobalKujiFactory;
import com.github.wujichen158.ikakuji.util.MsgUtil;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Optional;

@Command(
        value = "preview"
)
public class PreviewCmd {
    @CommandProcessor
    public void run(@Sender ServerPlayerEntity player,
                    @Completable(PreviewTypeCompleter.class) @Argument String type,
                    @Completable(MixedKujiCompleter.class) @Argument String kujiName,
                    String[] args) {
        IkaKujiLocaleCfg.Commands commands = IkaKuji.getInstance().getLocale().getCommands();
        IkaKujiLocaleCfg.Messages messages = IkaKuji.getInstance().getLocale().getMessages();

        int page = CmdUtil.getPageFromArgs(args);

        if (PreviewTypeCompleter.ALONE.equalsIgnoreCase(type)) {
            Optional.ofNullable(CrateFactory.get(kujiName))
                    .ifPresentOrElse(crate -> {
                        PreviewExecutor.preview(player, crate, page);
                    }, () -> {
                        player.sendMessage(MsgUtil.prefixedColorMsg(commands.getInvalidCrateName(), kujiName),
                                player.getUUID());
                    });
        } else if (PreviewTypeCompleter.GLOBAL.equalsIgnoreCase(type)) {
            Optional.ofNullable(GlobalKujiFactory.get(kujiName))
                    .ifPresentOrElse(globalKujiData -> {
                        Optional.ofNullable(CrateFactory.get(globalKujiData.getCrateName()))
                                .ifPresentOrElse(crate -> {
                                    PreviewExecutor.previewGlobal(player, globalKujiData, crate, kujiName, page);
                                }, () -> {
                                    player.sendMessage(MsgUtil.prefixedColorMsg(messages.getNoSuchCrateMsg(), kujiName),
                                            player.getUUID());
                                });
                    }, () -> {
                        player.sendMessage(MsgUtil.prefixedColorMsg(commands.getGlobalKujiInvalid(), kujiName),
                                player.getUUID());
                    });
        }
    }
}