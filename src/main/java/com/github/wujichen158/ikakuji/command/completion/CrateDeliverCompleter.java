package com.github.wujichen158.ikakuji.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import com.google.common.collect.Lists;

import java.lang.annotation.Annotation;
import java.util.List;

public class CrateDeliverCompleter implements TabCompleter<String> {

    public static final String KEY = "key";
    public static final String CRATE = "crate";
    private static final List<String> COMPLETIONS = Lists.newArrayList(KEY, CRATE);

    public CrateDeliverCompleter() {
    }

    @Override
    public List<String> getCompletions(String s, String[] currentData, Annotation... annotations) {
        return COMPLETIONS;
    }
}
