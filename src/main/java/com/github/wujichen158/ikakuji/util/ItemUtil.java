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

    public static CompoundNBT getPureTag(CompoundNBT tag) {
        if (tag != null) {
            tag.remove("display");
        }
        return tag;
    }

}
