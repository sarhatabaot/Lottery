package net.erbros.lottery.lottery;

import lombok.AllArgsConstructor;
import net.erbros.lottery.Lottery;

import java.util.TimerTask;

@AllArgsConstructor
//FIXME use a bukkit runnable instead. + runTaskTimer
public class LotteryDraw extends TimerTask {
    private final Lottery plugin;
    private final boolean draw;

    public void run() {
        if (draw && plugin.isLotteryDue()) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                    plugin, plugin::lotteryDraw);
        } else {
            plugin.extendLotteryDraw();
        }
    }
}