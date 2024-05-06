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
        return here.isItemEqual(other) && tagMatches(here, other);
    }

    private static boolean tagMatches(ItemStack here, ItemStack other) {
        NBTTagCompound thisPureTag = getPureTag(here.getTagCompound());
        NBTTagCompound otherPureTag = getPureTag(other.getTagCompound());
        boolean thisTagEmpty = thisPureTag == null || thisPureTag.hasNoTags();
        boolean otherTagEmpty = otherPureTag == null || otherPureTag.hasNoTags();
        if (thisTagEmpty != otherTagEmpty) {
            return false;
        }
        if (!thisTagEmpty) {
            return thisPureTag.equals(otherPureTag) && here.areCapsCompatible(other);
        }
        return true;
    }

    public static NBTTagCompound getPureTag(NBTTagCompound tag) {
        if (tag != null) {
            NBTTagCompound newTags = tag.copy();
            newTags.removeTag("display");
            newTags.removeTag("RepairCost");
            newTags.removeTag("HideFlags");
            return newTags;
        }
        return null;
    }

}
