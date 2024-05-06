package com.github.wujichen158.ikakuji.config;

import com.github.wujichen158.ikakuji.util.ItemUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Objects;
import java.util.Optional;

public class KujiCrateType {
    @ConfigSerializable
    public static class PositionCrate {
        String world;
        Integer x;
        Integer y;
        Integer z;

        public String getWorld() {
            return world;
        }

        public Integer getX() {
            return x;
        }

        public Integer getY() {
            return y;
        }

        public Integer getZ() {
            return z;
        }
    }

    @ConfigSerializable
    public static class EntityCrate {
        String name;

        public String getName() {
            return name;
        }
    }

    public static class ItemWrapper {
        private ItemStack itemStack;

        public ItemWrapper(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ItemWrapper)) {
                return false;
            }
            ItemWrapper other = (ItemWrapper) obj;
            return ItemUtil.equalsWithPureTag(this.itemStack, other.itemStack);
        }

        /**
         * Currently use item, damage and pure tag to judge
         *
         * @return a hashCode
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.itemStack.getItem(), this.itemStack.getItemDamage(), ItemUtil.getPureTag(this.itemStack.getTagCompound()));
        }
    }
}
