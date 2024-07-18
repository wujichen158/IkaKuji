package com.github.wujichen158.ikakuji.util;

public class CmdUtil {
    public static int getPageFromArgs(String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Math.max(Integer.parseInt(args[0]), 1);
            } catch (NumberFormatException ignored) {
            }
        }
        return page;
    }
}
