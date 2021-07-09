package net.erbros.lottery.events;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class LotteryDrawEvent extends Event {

    private final UUID winner;
    private final int ticketsBought;
    private final double winnings;
    private final Material material;
    private static final HandlerList handlers = new HandlerList();
    private final String winnerName;

    public LotteryDrawEvent(UUID winner, String winnerName, int ticketsBought, double winnings, Material material) {
        this.winner = winner;
        this.winnerName = winnerName;
        this.ticketsBought = ticketsBought;
        this.winnings = winnings;
        this.material = material;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UUID getWinner() {
        return winner;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public int getTicketsBought() {
        return ticketsBought;
    }

    public double getWinnings() {
        return winnings;
    }

    public Material getMaterial() {
        return material;
    }
}
