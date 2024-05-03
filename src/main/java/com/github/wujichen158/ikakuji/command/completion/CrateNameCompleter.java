package com.github.wujichen158.ikakuji.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import net.minecraft.command.ICommandSender;

import java.lang.annotation.Annotation;
import java.util.List;

public class CrateNameCompleter implements TabCompleter<String, ICommandSender> {

    public CrateNameCompleter() {
    }

    @Override
    public Class<ICommandSender> getSenderClass() {
        return ICommandSender.class;
    }

    @Override
    public Class<String> getCompletedClass() {
        return String.class;
    }

    @Override
    public List<String> getCompletions(ICommandSender sender, String[] currentData, Annotation... completionData) {
        return CrateFactory.getAllRegisteredNames();
    }
}

