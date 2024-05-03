package com.github.wujichen158.ikakuji.config;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
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

        // No count comparison
        private boolean equalsWithPureTag(ItemStack other) {
            if (this.itemStack.isEmpty()) {
                return other.isEmpty();
            }
            return !other.isEmpty() && this.itemStack.getItem() == other.getItem() && tagMatches(other);
        }

        private boolean tagMatches(ItemStack other) {
            boolean thisTagEmpty = this.itemStack.getTag() == null || this.itemStack.getTag().isEmpty();
            boolean otherTagEmpty = other.getTag() == null || other.getTag().isEmpty();
            if (thisTagEmpty != otherTagEmpty) {
                return false;
            }
            if (!thisTagEmpty) {
                return getPureTag(this.itemStack.getTag()).equals(getPureTag(other.getTag())) && this.itemStack.areCapsCompatible(other);
            }
            return true;
        }

        public CompoundNBT getPureTag(CompoundNBT tag) {
            if (tag != null) {
                tag.remove("display");
            }
            return tag;
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
            return equalsWithPureTag(other.getItemStack());
        }

        /**
         * Currently use item and pure tags to judge
         *
         * @return a hashCode
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.itemStack.getItem(), Optional.ofNullable(getPureTag(this.itemStack.getTag())).filter(CompoundNBT::isEmpty).orElse(null));
        }

        @Override
        public String toString() {
            return String.format("%s, %s, %s", this.itemStack.getItem().getRegistryName(), Optional.ofNullable(this.itemStack.getShareTag()).map(CompoundNBT::toString).orElse("null"), Optional.ofNullable(this.itemStack.getTag()).map(CompoundNBT::toString).orElse("null"));
        }
    }
}
