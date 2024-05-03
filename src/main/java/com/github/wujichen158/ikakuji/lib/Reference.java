package com.github.wujichen158.ikakuji.lib;

import java.util.Random;

public class Reference {
    public static final String MOD_ID = "ikakuji";

    public static final String CONFIG_PATH_PREFIX = "config/" + MOD_ID + "/";

    public static final String YAML_SUFFIX = ".yml";

    public static final Random RANDOM = new Random();

    public static final String CONFIG_PATH = CONFIG_PATH_PREFIX + "config" + YAML_SUFFIX;
    public static final String LOCALE_PATH = CONFIG_PATH_PREFIX + "locale" + YAML_SUFFIX;

    public static final String CRATE_PATH = CONFIG_PATH_PREFIX + "crates";
    public static final String DATA_PATH = CONFIG_PATH_PREFIX + "data";

    public static final String LOG_PATH = "logs/ikakuji.log";

}
