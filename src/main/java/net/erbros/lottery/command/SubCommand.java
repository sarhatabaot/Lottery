package net.erbros.lottery.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author sarhatabaot
 */
public abstract class SubCommand extends Command {

    public SubCommand(final String name) {
        super(name);
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if(!sender.hasPermission(getPermission() == null ? "" : getPermission())){
            sender.sendMessage("You do not have permission"+getPermission());
        }
        run(sender,args);
        return false;
    }

    protected abstract void run(final CommandSender sender, final String[] args);
}
