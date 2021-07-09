package net.erbros.lottery.model;

import net.erbros.lottery.FormatUtil;
import net.erbros.lottery.LotteryConfig;
import net.erbros.lottery.LotteryPlugin;
import net.erbros.lottery.RandomCollection;
import net.erbros.lottery.events.LotteryBuyTicketEvent;
import net.erbros.lottery.events.LotteryDrawEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;


public class LotteryGame {

    private final LotteryPlugin plugin;
    private final LotteryConfig lotteryConfig;
    private final File playersfile;

    public LotteryGame(final LotteryPlugin plugin) {
        playersfile = new File(plugin.getDataFolder(), "lotteryPlayers.yml");
        this.plugin = plugin;
        lotteryConfig = plugin.getLotteryConfig();
    }

    public boolean addPlayer(final Player player, final int maxAmountOfTickets, final int numberOfTickets) {
        LotteryBuyTicketEvent buyEvent = new LotteryBuyTicketEvent(player, numberOfTickets);
        plugin.getServer().getPluginManager().callEvent(buyEvent);
        if (buyEvent.isCancelled()) {
            return false;
        }
        // Do the ticket cost money or item?
        if (lotteryConfig.useEconomy() && plugin.hasEconomy()) {
            // Do the player have money?
            // First checking if the player got an account, if not let's create
            // it.
            OfflinePlayer p = Bukkit.getOfflinePlayer(player.getUniqueId());
            if (!plugin.getEconomy().hasAccount(p)) {
                plugin.getEconomy().createPlayerAccount(p);
            }
            final double balance = plugin.getEconomy().getBalance(p);

            // And lets withdraw some money
            if (balance > lotteryConfig.getCost() * numberOfTickets) {
                // Removing coins from players account.
                plugin.getEconomy().withdrawPlayer(p, lotteryConfig.getCost() * numberOfTickets);
            } else {
                return false;
            }
            lotteryConfig.debugMsg("taking " + (lotteryConfig.getCost() * numberOfTickets) + "from account");
        } else {
            // Do the user have the item
            if (player.getInventory().contains(Material.getMaterial(lotteryConfig.getMaterial()), (int) lotteryConfig.getCost() * numberOfTickets)) {
                // Remove items.
                player.getInventory().removeItem(
                        new ItemStack(Material.getMaterial(lotteryConfig.getMaterial()), (int) lotteryConfig.getCost() * numberOfTickets));
            } else {
                return false;
            }
        }
        // If the user paid, continue. Else we would already have sent return
        // false
        YamlConfiguration config = loadPlayersFile();
        int tickets = config.getInt("players." + player.getUniqueId() + ".tickets", 0);
        tickets += numberOfTickets;
        config.set("players." + player.getUniqueId() + ".tickets", tickets);
        config.set("players." + player.getUniqueId() + ".name", player.getName());
        savePlayersFile(config);
        return true;
    }

    public RandomCollection<LotteryEntry> getBoughtTickets() {
        final RandomCollection<LotteryEntry> players = new RandomCollection<>();

        YamlConfiguration config = loadPlayersFile();
        if (!config.isConfigurationSection("players")) {
            return new RandomCollection<>();
        }
        ConfigurationSection section = config.getConfigurationSection("players");
        for (String key : section.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            int tickets = section.getInt(key + ".tickets", 0);
            String name = section.getString(key + ".name", "");
            players.add(tickets, new LotteryEntry(uuid, name, tickets));
        }
        return players;
    }

    private YamlConfiguration loadPlayersFile() {
        if (!playersfile.exists()) {
            try {
                playersfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(playersfile);
    }

    private void savePlayersFile(YamlConfiguration config) {
        try {
            config.save(playersfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double winningAmount() {
        double amount;
        final RandomCollection<LotteryEntry> players = getBoughtTickets();
        int ticketsize = 0;
        for (LotteryEntry entry : players.values()) {
            ticketsize += entry.getTickets();
        }
        amount = ticketsize * FormatUtil.formatAmount(lotteryConfig.getCost(), lotteryConfig.useEconomy());
        lotteryConfig.debugMsg("playerno: " + players.size() + " amount: " + amount);
        // Set the net payout as configured in the config.
        if (lotteryConfig.getNetPayout() > 0) {
            amount = amount * lotteryConfig.getNetPayout() / 100;
        }
        // Add extra money added by admins and mods?
        amount += lotteryConfig.getExtraInPot();
        // Any money in jackpot?

        // format it once again.
        amount = FormatUtil.formatAmount(amount, lotteryConfig.useEconomy());

        return amount;
    }

    public double taxAmount() {
        double amount = 0;

        // we only have tax is the net payout is between 0 and 100.
        if (lotteryConfig.getNetPayout() >= 100 || lotteryConfig.getNetPayout() <= 0 || !lotteryConfig.useEconomy()) {
            return amount;
        }

        final RandomCollection<LotteryEntry> players = getBoughtTickets();
        amount = players.size() * FormatUtil.formatAmount(lotteryConfig.getCost(), lotteryConfig.useEconomy());

        // calculate the tax.
        amount = amount * (1 - (lotteryConfig.getNetPayout() / 100));

        // format it once again.
        amount = FormatUtil.formatAmount(amount, lotteryConfig.useEconomy());

        return amount;
    }

    public int ticketsSold() {
        int sold;
        final RandomCollection<LotteryEntry> players = getBoughtTickets();
        sold = players.size();
        return sold;
    }

    public void removeFromClaimList(final Player player) {
        // Do the player have something to claim?
        final ArrayList<String> otherPlayersClaims = new ArrayList<>();
        final ArrayList<String> claimArray = new ArrayList<>();
        try {
            final BufferedReader in = new BufferedReader(
                    new FileReader(plugin.getDataFolder() + File.separator + "lotteryClaim.txt"));
            String str;
            while ((str = in.readLine()) != null) {
                final String[] split = str.split(":");
                if (split[0].equals(player.getUniqueId().toString())) {
                    // Adding this to player claim.
                    claimArray.add(str);
                } else {
                    otherPlayersClaims.add(str);
                }
            }
            in.close();
        } catch (IOException e) {
        }

        // Did the user have any claims?
        if (claimArray.isEmpty()) {
            sendMessage(player, "ErrorClaim");
        }
        // Do a bit payout.
        for (String aClaimArray : claimArray) {
            final String[] split = aClaimArray.split(":");
            final int claimAmount = Integer.parseInt(split[1]);
            final Material claimMaterial = Material.matchMaterial(split[2]);
            player.getInventory().addItem(new ItemStack(claimMaterial, claimAmount));
            sendMessage(player, "PlayerClaim", FormatUtil.formatMaterialName(claimMaterial.getData().getName()));
        }

        // Add the other players claims to the file again.
        try {
            final BufferedWriter out = new BufferedWriter(
                    new FileWriter(plugin.getDataFolder() + File.separator + "lotteryClaim.txt"));
            for (String otherPlayersClaim : otherPlayersClaims) {
                out.write(otherPlayersClaim);
                out.newLine();
            }

            out.close();
        } catch (IOException e) {
        }
    }

    public void addToClaimList(final UUID player, final int winningAmount, final Material winningMaterial) {
        // Then first add new winner, and after that the old winners.
        try {
            final BufferedWriter out = new BufferedWriter(
                    new FileWriter(plugin.getDataFolder() + File.separator + "lotteryClaim.txt", true));
            out.write(player + ":" + winningAmount + ":" + winningMaterial.getData().getName());
            out.newLine();
            out.close();
        } catch (IOException e) {
        }
    }

    public void addToWinnerList(final String playerName, final Double winningAmount, final Material winningMaterial) {
        // This list should be 10 players long.
        final ArrayList<String> winnerArray = new ArrayList<>();
        try {
            final BufferedReader in = new BufferedReader(
                    new FileReader(plugin.getDataFolder() + File.separator + "lotteryWinners.txt"));
            String str;
            while ((str = in.readLine()) != null) {
                winnerArray.add(str);
            }
            in.close();
        } catch (IOException e) {
        }
        // Then first add new winner, and after that the old winners.
        try {
            final BufferedWriter out = new BufferedWriter(
                    new FileWriter(plugin.getDataFolder() + File.separator + "lotteryWinners.txt"));
            out.write(playerName + ":" + winningAmount + ":" + winningMaterial.getData().getName());
            out.newLine();
            // How long is the array? We just want the top 9. Removing index 9
            // since its starting at 0.
            if (!winnerArray.isEmpty()) {
                if (winnerArray.size() > 9) {
                    winnerArray.remove(9);
                }
                // Go trough list and output lines.
                for (String aWinnerArray : winnerArray) {
                    out.write(aWinnerArray);
                    out.newLine();
                }
            }
            out.close();
        } catch (IOException e) {
        }
    }

    public long timeUntil() {
        final long nextDraw = lotteryConfig.getNextexec();
        return ((nextDraw - System.currentTimeMillis()) / 1000);
    }

    public String timeUntil(final boolean mini) {
        final long timeLeft = timeUntil();
        // If negative number, just tell them its DRAW TIME!
        if (timeLeft < 0) {
            // Lets make it draw at once.. ;)
            plugin.startTimerSchedule(true);
            // And return some string to let the user know we are doing our best ;)
            if (mini) {
                return "Soon";
            }
            return "Draw will occur soon!";
        }

        return FormatUtil.timeUntil(timeLeft, mini, lotteryConfig);
    }

    public boolean getWinner() {
        final RandomCollection<LotteryEntry> players = getBoughtTickets();
        if (players.isEmpty()) {
            broadcastMessage("NoWinnerTickets");
            return false;
        } else {
            LotteryEntry winner = players.next();
            double amount = winningAmount();
            int ticketsBought = winner.getTickets();
            if (lotteryConfig.useEconomy()) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(winner.getUUID());
                if (!plugin.getEconomy().hasAccount(p)) {
                    plugin.getEconomy().createPlayerAccount(p);
                }

                // Just make sure the account exists, or make it with default
                // value.
                // Add money to account.
                plugin.getEconomy().depositPlayer(p, amount);
                // Announce the winner:
                broadcastMessage("WinnerCongrat", winner.getName(), FormatUtil.formatCost(amount, lotteryConfig), ticketsBought, lotteryConfig.getPlural("ticket", ticketsBought));
                addToWinnerList(winner.getName(), amount, Material.AIR);

                double taxAmount = taxAmount();
                if (taxAmount() > 0 && lotteryConfig.getTaxTarget().length() > 0) {
                    OfflinePlayer target = Bukkit.getOfflinePlayer(lotteryConfig.getTaxTarget());
                    if (target == null) {
                        plugin.getLogger().warning("Invalid economy account specified '" + lotteryConfig.getTaxTarget() + "', tax lost.  Fix your 'taxTarget' in config file.");
                    }
                    if (!plugin.getEconomy().hasAccount(target)) {
                        plugin.getEconomy().createPlayerAccount(target);
                    }
                    plugin.getEconomy().depositPlayer(target, taxAmount);
                }
            } else {
                // let's throw it to an int.
                final int matAmount = (int) FormatUtil.formatAmount(amount, lotteryConfig.useEconomy());
                amount = (double) matAmount;

                broadcastMessage("WinnerCongrat", winner.getName(), FormatUtil.formatCost(amount, lotteryConfig), ticketsBought, lotteryConfig.getPlural("ticket", ticketsBought));
                broadcastMessage("WinnerCongratClaim");
                addToWinnerList(winner.getName(), amount, Material.getMaterial(lotteryConfig.getMaterial()));

                addToClaimList(winner.getUUID(), matAmount, Material.getMaterial(lotteryConfig.getMaterial()));
            }
            int ticketsize = 0;
            for (LotteryEntry entry : players.values()) {
                ticketsize += entry.getTickets();
            }
            broadcastMessage(
                    "WinnerSummary", players.size(), lotteryConfig.getPlural(
                            "player", players.size()), ticketsize, lotteryConfig.getPlural("ticket", ticketsize));

            // Add last winner to config.
            lotteryConfig.setLastwinner(winner.getName());
            lotteryConfig.setLastwinneramount(amount);

            clearAfterGettingWinner();

            Material material = lotteryConfig.useEconomy() ? Material.AIR : Material.getMaterial(lotteryConfig.getMaterial());
            LotteryDrawEvent drawEvent = new LotteryDrawEvent(winner.getUUID(), winner.getName(), ticketsBought, amount, material);
            Bukkit.getServer().getPluginManager().callEvent(drawEvent);
        }
        return true;
    }

    public void clearAfterGettingWinner() {

        // extra money in pot added by admins and mods?
        // Should this be removed?
        if (lotteryConfig.clearExtraInPot()) {
            lotteryConfig.setExtraInPot(0);
        }
        // Clear file.
        YamlConfiguration config = loadPlayersFile();
        config.set("players", null);
        savePlayersFile(config);
    }


    public void broadcastMessage(final String topic, final Object... args) {
        try {
            for (String message : lotteryConfig.getMessage(topic)) {
                String outMessage = formatCustomMessageLive(message, args);
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.hasMetadata("LotteryOptOut") && player.getMetadata("LotteryOptOut").get(0).asBoolean()) {
                        continue;
                    }
                    outMessage = outMessage.replaceAll("%player%", player.getDisplayName());
                    player.sendMessage(outMessage);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Invalid Translation Key: " + topic, e);
        }
    }

    public void sendMessage(final CommandSender player, final String topic, final Object... args) {
        try {
            for (String message : lotteryConfig.getMessage(topic)) {
                String outMessage = formatCustomMessageLive(message, args);
                if (player instanceof Player) {
                    outMessage = outMessage.replaceAll("%player%", Matcher.quoteReplacement(((Player) player).getDisplayName()));
                }
                player.sendMessage(outMessage);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Invalid Translation Key: " + topic, e);
        }
    }

    public String formatCustomMessageLive(final String message, final Object... args) throws Exception {
        //Lets give timeLeft back if user provie %draw%
        String outMessage = message.replaceAll("%draw%", Matcher.quoteReplacement(timeUntil(true)));

        //Lets give timeLeft with full words back if user provie %drawLong%
        outMessage = outMessage.replaceAll("%drawLong%", Matcher.quoteReplacement(timeUntil(false)));

        // %cost% = cost
        outMessage = outMessage.replaceAll("%cost%", Matcher.quoteReplacement(FormatUtil.formatCost(lotteryConfig.getCost(), lotteryConfig)));

        // %pot%
        outMessage = outMessage.replaceAll("%pot%", Matcher.quoteReplacement(FormatUtil.formatCost(winningAmount(), lotteryConfig)));

        // %prefix%
        outMessage = outMessage.replaceAll("%prefix%", Matcher.quoteReplacement(lotteryConfig.getMessage("prefix").get(0)));

        for (int i = 0; i < args.length; i++) {
            outMessage = outMessage.replaceAll("%" + i + "%", Matcher.quoteReplacement(args[i].toString()));
        }

        // Lets get some colors on this, shall we?
        outMessage = outMessage.replaceAll("(&([a-fk-or0-9]))", "\u00A7$2");
        return outMessage;
    }

    public int getTickets(Player player) {
        for (LotteryEntry entry : getBoughtTickets().values()) {
            if (entry.getUUID().equals(player.getUniqueId())) {
                return entry.getTickets();
            }
        }
        return 0;
    }
}
