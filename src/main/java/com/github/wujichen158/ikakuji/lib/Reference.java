package com.github.wujichen158.ikakuji.lib;

import com.google.common.collect.Sets;

import java.util.Random;
import java.util.Set;

public class Reference {
    public static final String MOD_ID = "ikakuji";

    public static final String CONFIG_PATH_PREFIX = "config/" + MOD_ID + "/";

    public static final String YAML_SUFFIX = ".yml";

    public static final Random RANDOM = new Random();

    public static final String CONFIG_PATH = CONFIG_PATH_PREFIX + "config" + YAML_SUFFIX;
    public static final String LOCALE_PATH = CONFIG_PATH_PREFIX + "locale" + YAML_SUFFIX;

    public static final String CRATE_PATH = CONFIG_PATH_PREFIX + "crates";
    public static final String DATA_PATH = CONFIG_PATH_PREFIX + "data";
    public static final String GLOBAL_DATA_PATH = CONFIG_PATH_PREFIX + "globalkujis";

    public static final String LOG_PATH = "logs/ikakuji.log";

    public static final Set<String> CURRENT_TIME_SYMBOLS = Sets.newHashSet("0", "now");
    public static final Set<String> INF_TIME_SYMBOLS = Sets.newHashSet("0", "no", "never");

}
