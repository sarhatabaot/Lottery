package net.erbros.lottery.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LotteryDrawEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private UUID winner;
    private String winnerName;
    private int ticketsBought;
    private double winnings;
    private Material material;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
