package com.github.wujichen158.ikakuji.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

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
        CompoundNBT thisPureTag = getPureTag(here.getTag());
        CompoundNBT otherPureTag = getPureTag(other.getTag());
        boolean thisTagEmpty = thisPureTag == null || thisPureTag.isEmpty();
        boolean otherTagEmpty = otherPureTag == null || otherPureTag.isEmpty();
        if (thisTagEmpty != otherTagEmpty) {
            return false;
        }
        if (!thisTagEmpty) {
            return thisPureTag.equals(otherPureTag) && here.areCapsCompatible(other);
        }
        return true;
    }

    public static CompoundNBT getPureTag(CompoundNBT tag) {
        if (tag != null) {
            CompoundNBT newTags = tag.copy();
            newTags.remove("display");
            newTags.remove("RepairCost");
            return newTags;
        }
        return null;
    }

}
