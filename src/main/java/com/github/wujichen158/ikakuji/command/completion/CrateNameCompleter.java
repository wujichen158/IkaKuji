package com.github.wujichen158.ikakuji.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import com.github.wujichen158.ikakuji.util.CrateFactory;

import java.lang.annotation.Annotation;
import java.util.List;

public class CrateNameCompleter implements TabCompleter<String> {

    public CrateNameCompleter() {
    }

    @Override
    public List<String> getCompletions(String s, String[] currentData, Annotation... annotations) {
        return CrateFactory.getAllRegisteredNames();
    }
}

