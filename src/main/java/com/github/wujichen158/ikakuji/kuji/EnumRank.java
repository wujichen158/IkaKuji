package com.github.wujichen158.ikakuji.kuji;

@Deprecated
public enum EnumRank {
    // Rank enums

    A(1),
    B(2),
    C(4),
    D(8),
    E(16),
    F(32),
    G(64),
    H(128),
    I(256),
    J(512);

    private final int defaultWeight;

    EnumRank(int defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

    public int getDefaultWeight() {
        return this.defaultWeight;
    }
}
