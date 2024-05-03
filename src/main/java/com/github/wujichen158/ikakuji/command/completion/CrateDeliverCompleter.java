package com.github.wujichen158.ikakuji.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;

import java.lang.annotation.Annotation;
import java.util.List;

public class CrateDeliverCompleter implements TabCompleter<String, ICommandSender> {

    public static final String KEY = "key";
    public static final String CRATE = "crate";
    private static final List<String> COMPLETIONS = Lists.newArrayList(KEY, CRATE);

    public CrateDeliverCompleter() {
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
        return COMPLETIONS;
    }
}
