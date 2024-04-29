package com.github.wujichen158.ikakuji.lib;

public class PermissionNodes {
    public static final String ADMIN_NODE = "admin";
    public static final String USER_NODE = "user";

    public static final String RELOAD_NODE = Reference.MOD_ID + "." + ADMIN_NODE + ".reload";

    public static final String GIVE_NODE = Reference.MOD_ID + "." + ADMIN_NODE + ".give";

    public static final String HOLD_NODE = Reference.MOD_ID + "." + ADMIN_NODE + ".hold";

    public static final String STOP_NODE = Reference.MOD_ID + "." + ADMIN_NODE + ".stop";


    //TODO: add an extra param that specify global or self. global by default, can click to swith like GD
    public static final String LIST_NODE = Reference.MOD_ID + "." + USER_NODE + ".list";

    //TODO: Add subnode like .join.<kuji_name>
    public static final String JOIN_NODE = Reference.MOD_ID + "." + USER_NODE + ".join";

}
