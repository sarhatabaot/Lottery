package net.erbros.lottery.model;

import net.erbros.lottery.LotteryPlugin;

import java.util.TimerTask;


public class LotteryDraw extends TimerTask {

    private final LotteryPlugin plugin;
    private final boolean draw;

    public LotteryDraw(final LotteryPlugin plugin, final boolean draw) {
        this.plugin = plugin;
        this.draw = draw;
    }

    public void run() {
        if (draw && plugin.isLotteryDue()) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin::lotteryDraw);
        } else {
            plugin.extendLotteryDraw();
        }
    }
}