package com.github.wujichen158.ikakuji.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.github.wujichen158.ikakuji.lib.Reference;
import net.minecraft.command.ICommandSource;

@Command(
        value = {
                Reference.MOD_ID,
                "kuji"
        }
)
@SubCommands({
        ReloadCmd.class,
})
public class IkaKujiCmd {
    @CommandProcessor
    public void run(@Sender ICommandSource sender) {
    }
}
