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
        boolean thisTagEmpty = here.getTag() == null || here.getTag().isEmpty();
        boolean otherTagEmpty = other.getTag() == null || other.getTag().isEmpty();
        if (thisTagEmpty != otherTagEmpty) {
            return false;
        }
        if (!thisTagEmpty) {
            return getPureTag(here.getTag()).equals(getPureTag(other.getTag())) && here.areCapsCompatible(other);
        }
        return true;
    }

    public static CompoundTag getPureTag(CompoundTag tag) {
        if (tag != null) {
            tag.remove("display");
        }
        return tag;
    }

}
