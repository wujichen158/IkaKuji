package com.github.wujichen158.ikakuji.config.envynew;

import com.envyful.api.gui.Transformer;
import com.github.wujichen158.ikakuji.lib.Placeholders;

public class AmountPlaceholder implements Transformer {

    private int rewardDrawn;
    private int rewardTotal;

    public static AmountPlaceholder of(int rewardDrawn, int rewardTotal) {
        return new AmountPlaceholder(rewardDrawn, rewardTotal);
    }

    private AmountPlaceholder(int rewardDrawn, int rewardTotal) {
        this.rewardDrawn = rewardDrawn;
        this.rewardTotal = rewardTotal;
    }

    public String transformName(String name) {
        return name.replace(Placeholders.REWARD_DRAWN, String.valueOf(this.rewardDrawn))
                .replace(Placeholders.REWARD_REMAIN, String.valueOf(this.rewardTotal - this.rewardDrawn))
                .replace(Placeholders.REWARD_TOTAL, String.valueOf(this.rewardTotal));
    }
}
