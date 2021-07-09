package net.erbros.lottery;

import net.erbros.lottery.model.LotteryGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final LotteryGame lGame;

    public PlayerJoinListener(final LotteryPlugin plugin) {
        lGame = plugin.getLotteryGame();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("lottery.notify")) {
            // Send the player some info about time until lottery draw?
            lGame.sendMessage(event.getPlayer(), "Welcome");
        }
    }
}
