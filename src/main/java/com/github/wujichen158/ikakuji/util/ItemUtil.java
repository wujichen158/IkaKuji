package com.github.wujichen158.ikakuji.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemUtil {

    /**
     * No count comparison
     *
     * @param here
     * @param other
     * @return
     */
    public static boolean equalsWithPureTag(ItemStack here, ItemStack other) {
        if (here.isEmpty()) {
            return other.isEmpty();
        }
        return !other.isEmpty() && here.getItem() == other.getItem() && tagMatches(here, other);
    }

    private static boolean tagMatches(ItemStack here, ItemStack other) {
        boolean thisTagEmpty = here.getTagCompound() == null || here.getTagCompound().hasNoTags();
        boolean otherTagEmpty = other.getTagCompound() == null || other.getTagCompound().hasNoTags();
        if (thisTagEmpty != otherTagEmpty) {
            return false;
        }
        if (!thisTagEmpty) {
            return getPureTag(here.getTagCompound()).equals(getPureTag(other.getTagCompound())) && here.areCapsCompatible(other);
        }
        return true;
    }

    public static NBTTagCompound getPureTag(NBTTagCompound tag) {
        if (tag != null) {
            tag.removeTag("display");
        }
        return tag;
    }

}
