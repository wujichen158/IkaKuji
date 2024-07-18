package com.github.wujichen158.ikakuji.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import com.github.wujichen158.ikakuji.util.GlobalKujiFactory;

import java.lang.annotation.Annotation;
import java.util.List;

public class GlobalKujiCompleter implements TabCompleter<String> {

    public GlobalKujiCompleter() {
    }

    @Override
    public List<String> getCompletions(String s, String[] currentData, Annotation... annotations) {
        return GlobalKujiFactory.getOngoingGloballyKuji();
    }
}
