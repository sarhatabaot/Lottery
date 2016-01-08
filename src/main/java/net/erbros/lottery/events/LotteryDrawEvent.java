package net.erbros.lottery.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class LotteryDrawEvent extends Event
{

    private UUID winner;
    private int ticketsBought;
    private double winnings;
    private int material;
    private static final HandlerList handlers = new HandlerList();

    public LotteryDrawEvent( UUID winner, int ticketsBought, double winnings, int material )
    {
        this.winner = winner;
        this.ticketsBought = ticketsBought;
        this.winnings = winnings;
        this.material = material;
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public UUID getWinner()
    {
        return winner;
    }

    public int getTicketsBought()
    {
        return ticketsBought;
    }

    public double getWinnings()
    {
        return winnings;
    }

    public int getMaterial()
    {
        return material;
    }
}
