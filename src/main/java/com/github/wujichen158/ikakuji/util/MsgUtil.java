package com.github.wujichen158.ikakuji.util;

import com.envyful.api.forge.chat.UtilChatColour;
import com.github.wujichen158.ikakuji.IkaKuji;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class MsgUtil {
    /**
     * Add prefix to the given msg,
     * translate color char to real color,
     * process the args
     *
     * @param msg  Message to process
     * @param args arguments
     * @return processed message
     */
    public static ITextComponent prefixedColorMsg(String msg, Object... args) {
        return new TextComponentString(UtilChatColour.translateColourCodes('&', IkaKuji.getInstance().getLocale().getMessages().getPrefix() + String.format(msg, args)));
    }

    /**
     * Translate color char to real color,
     * process the args
     *
     * @param msg  Message to process
     * @param args arguments
     * @return processed message
     */
    public static ITextComponent colorMsg(String msg, Object... args) {
        return new TextComponentString(UtilChatColour.translateColourCodes('&', String.format(msg, args)));
    }
}
