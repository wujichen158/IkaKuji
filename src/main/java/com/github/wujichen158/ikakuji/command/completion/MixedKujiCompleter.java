package com.github.wujichen158.ikakuji.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import com.github.wujichen158.ikakuji.util.CrateFactory;
import com.github.wujichen158.ikakuji.util.GlobalKujiFactory;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.lang.annotation.Annotation;
import java.util.List;

public class MixedKujiCompleter implements TabCompleter<String> {

    public MixedKujiCompleter() {
    }

    @Override
    public List<String> getCompletions(String s, String[] currentData, Annotation... annotations) {
        return Lists.newArrayList(Iterables.concat(
                GlobalKujiFactory.getOngoingGloballyKuji(), CrateFactory.getAllRegisteredNames()));
    }
}