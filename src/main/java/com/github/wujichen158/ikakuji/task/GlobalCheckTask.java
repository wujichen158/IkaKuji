package com.github.wujichen158.ikakuji.task;

import com.github.wujichen158.ikakuji.util.GlobalKujiFactory;

import java.time.LocalTime;

public class GlobalCheckTask implements Runnable {
    private LocalTime lastTime;
    public static LocalTime cfgTime;

    public GlobalCheckTask() {
        this.lastTime = LocalTime.now();
    }

    @Override
    public void run() {
        GlobalKujiFactory.updateGlobalKuji();
    }
}
