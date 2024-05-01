package com.github.wujichen158.ikakuji.config;

import net.minecraft.item.ItemStack;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Objects;

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

        public void setWorld(String world) {
            this.world = world;
        }

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

        public Integer getZ() {
            return z;
        }

        public void setZ(Integer z) {
            this.z = z;
        }
    }

    public static class ItemWrapper {
        private ItemStack itemStack;

        public ItemWrapper(ItemStack itemStack) {
            this.itemStack = itemStack;
        }

        public ItemStack getItem() {
            return itemStack;
        }

        public void setItem(ItemStack itemStack) {
            this.itemStack = itemStack;
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
            return this.itemStack.equals(other.getItem(), true);
        }

        /**
         * Currently use isEmpty, item, count and shareTag to judge
         *
         * @return a hashCode
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.itemStack.isEmpty(), this.itemStack.getItem(), this.itemStack.getCount(), this.itemStack.getShareTag());
        }
    }
}
