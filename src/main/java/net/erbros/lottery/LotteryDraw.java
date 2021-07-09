package net.erbros.lottery;

import java.util.TimerTask;


public class LotteryDraw extends TimerTask {

    private final Lottery plugin;
    private final boolean draw;

    public LotteryDraw(final Lottery plugin, final boolean draw) {
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