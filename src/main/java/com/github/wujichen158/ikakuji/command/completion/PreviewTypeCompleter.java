package com.github.wujichen158.ikakuji.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import com.google.common.collect.Lists;

import java.lang.annotation.Annotation;
import java.util.List;

public class PreviewTypeCompleter implements TabCompleter<String> {
    public static final String ALONE = "alone";
    public static final String GLOBAL = "global";
    private static final List<String> COMPLETIONS = Lists.newArrayList(ALONE, GLOBAL);

    public PreviewTypeCompleter() {
    }

    @Override
    public List<String> getCompletions(String s, String[] currentData, Annotation... annotations) {
        return COMPLETIONS;
    }
}
