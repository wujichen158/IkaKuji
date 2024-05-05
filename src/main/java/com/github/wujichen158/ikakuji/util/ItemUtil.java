package com.github.wujichen158.ikakuji.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

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
        return !other.isEmpty() && ItemStack.isSameItem(here, other) && tagMatches(here, other);
    }

    private static boolean tagMatches(ItemStack here, ItemStack other) {
        CompoundTag thisPureTag = getPureTag(here.getTag());
        CompoundTag otherPureTag = getPureTag(other.getTag());
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

    public static CompoundTag getPureTag(CompoundTag tag) {
        if (tag != null) {
            CompoundTag newTags = tag.copy();
            newTags.remove("display");
            newTags.remove("RepairCost");
            return newTags;
        }
        return null;
    }

}
