package com.github.wujichen158.ikakuji.lib;

public class PermissionNodes {
    public static final String ADMIN_NODE = "admin";
    public static final String USER_NODE = "user";

    /**
     * Common part
     */

    public static final String RELOAD_NODE = Reference.MOD_ID + "." + ADMIN_NODE + ".reload";

    //TODO: add an extra param that specify global or self. global by default, can click to swith like GD
    public static final String LIST_NODE = Reference.MOD_ID + "." + USER_NODE + ".list";


    /**
     * Individual kuji part
     */

    public static final String OPEN_NODE = Reference.MOD_ID + "." + ADMIN_NODE + ".open";

    public static final String GIVE_NODE = Reference.MOD_ID + "." + ADMIN_NODE + ".give";

    private static final String OPEN_CRATE_NODE = Reference.MOD_ID + "." + USER_NODE + ".open.%s";
    public static String getOpenPermNode(String crateName) {
        return String.format(OPEN_CRATE_NODE, crateName);
    }

    private static final String PREVIEW_CRATE_NODE = Reference.MOD_ID + "." + USER_NODE + ".preview.%s";
    public static String getPreviewPermNode(String crateName) {
        return String.format(PREVIEW_CRATE_NODE, crateName);
    }

    private static final String PREVIEW_GLOBAL_NODE = Reference.MOD_ID + "." + USER_NODE + ".global_preview.%s";
    public static String getGlobalPreviewPermNode(String globalKujiName) {
        return String.format(PREVIEW_GLOBAL_NODE, globalKujiName);
    }

    /**
     * Global kuji part
     */

    public static final String HOLD_NODE = Reference.MOD_ID + "." + ADMIN_NODE + ".hold";

    public static final String STOP_NODE = Reference.MOD_ID + "." + ADMIN_NODE + ".stop";

    private static final String JOIN_NODE = Reference.MOD_ID + "." + USER_NODE + ".join.%s";
    public static String getJoinPermNode(String crateName) {
        return String.format(JOIN_NODE, crateName);
    }

}
