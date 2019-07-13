package net.erbros.lottery.command;

import net.erbros.lottery.Etc;
import net.erbros.lottery.Lottery;
import net.erbros.lottery.lottery.LotteryConfig;
import net.erbros.lottery.lottery.LotteryGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;

/**
 * @author sarhatabaot
 */
public class LotteryCommand extends Command {
    private Map<String,SubCommand> subCommandMap;

    public LotteryCommand() {
        super("lottery");
        setAliases(Arrays.asList("lot","lotto"));
        setUsage("/lottery [winners|claim|help|messages|buy [amount]]");
        setDescription("Provides access to Permissions commands and information.");
        setPermission("lottery.buy");
    }

    @Override
    public boolean execute(final CommandSender sender, final String s, final String[] args) {
        if(args.length == 0)
            commandNull(sender);
        if(!subCommandMap.containsKey(args[0])){
            sender.sendMessage("No such command.");
            return false;
        }
        //actually run the command
        runSubCommand(sender,args);
        return true;
    }

    private void runSubCommand(final CommandSender sender, final String[] args) {
        subCommandMap.get(args[0]).execute(sender,args[0],args);
    }

    public void registerSubCommand(SubCommand command){
        subCommandMap.put(command.getName(),command);
    }

    private void commandNull(final CommandSender sender) {
        // Is this a console? If so, just tell that lottery is running and time until next draw.
        if (!(sender instanceof Player)) {
            sender.sendMessage("Hi Console - The Lottery plugin is running");
            Lottery.getInstance().getLotteryGame().sendMessage(sender, "DrawIn", Lottery.getInstance().getLotteryGame().timeUntil(false));
            return;
        }
        LotteryGame lGame = Lottery.getInstance().getLotteryGame();
        LotteryConfig lConfig = Lottery.getInstance().getLotteryConfig();
        final Player player = (Player) sender;

        // Check if we got any money/items in the pot.
        final double amount = lGame.winningAmount();
        lConfig.debugMsg("pot current total: " + amount);
        // Send some messages:
        lGame.sendMessage(sender, "DrawIn", lGame.timeUntil(false));
        lGame.sendMessage(sender, "TicketCommand");
        lGame.sendMessage(sender, "PotAmount");
        if (lConfig.getMaxTicketsEachUser() > 1) {
            lGame.sendMessage(
                    player, "YourTickets", lGame.getTickets(player), lConfig.getPlural("ticket", lGame.getTickets(player)));
        }
        // Number of tickets available?
        if (lConfig.getTicketsAvailable() > 0) {
            lGame.sendMessage(
                    sender, "TicketRemaining", (lConfig.getTicketsAvailable() - lGame.ticketsSold()), lConfig.getPlural(
                            "ticket", lConfig.getTicketsAvailable() - lGame.ticketsSold()));
        }
        lGame.sendMessage(sender, "CommandHelp");

        // Does lastwinner exist and != null? Show.
        // Show different things if we are using iConomy over
        // material.
        if (lConfig.getLastWinner() != null) {
            lGame.sendMessage(sender, "LastWinner", lConfig.getLastWinner(), Etc.formatCost(lConfig.getLastWinnerAmount(), lConfig));
        }

        // if not iConomy, make players check for claims.
        if (!lConfig.isUseEconomy()) {
            lGame.sendMessage(sender, "CheckClaim");
        }
    }
}
