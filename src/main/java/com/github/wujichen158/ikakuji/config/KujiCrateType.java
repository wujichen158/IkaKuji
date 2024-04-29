package com.github.wujichen158.ikakuji.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

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

    @ConfigSerializable
    public static class EntityCrate {
        String entityName;

        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }
    }
}
