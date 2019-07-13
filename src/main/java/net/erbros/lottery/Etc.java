package net.erbros.lottery;

import net.erbros.lottery.lottery.LotteryConfig;
import org.bukkit.Material;

import java.util.*;

public class Etc {
    public static String formatCost(double cost, LotteryConfig lConfig) {
        if (lConfig.isUseEconomy()) {
            return lConfig.formatCurrency((formatAmount(cost, lConfig.isUseEconomy())));
        } else {
            return String.valueOf(
                    (int) formatAmount(cost, lConfig.isUseEconomy())).concat(
                    " " + formatMaterialName(lConfig.getMaterial()));
        }
    }

    public static double formatAmount(double amount, final boolean usingiConomy) {

        if (usingiConomy) {
            return Math.floor(amount * 100) / 100;
        } else {
            return Math.floor(amount);
        }
    }

    public static String formatMaterialName(final Material material) {
        String rawMaterialName = material.name().toLowerCase();
        // Large first letter.
        final String firstLetterCapital = rawMaterialName.substring(0, 1).toUpperCase();
        rawMaterialName = firstLetterCapital + rawMaterialName.substring(1);
        return rawMaterialName.replace("_", " ");
    }


    public static String timeUntil(final long time, final boolean mini, LotteryConfig lConfig) {
        long timeLeft = time;
        // How many days left?
        String stringTimeLeft = "";

        if (timeLeft >= 60 * 60 * 24) {
            final int days = (int) timeLeft / (60 * 60 * 24);
            timeLeft -= 60 * 60 * 24 * days;
            if (mini) {
                stringTimeLeft += days + "d ";
            } else {
                stringTimeLeft += days + " " + lConfig.getPlural("day", days) + ", ";
            }
        }
        if (timeLeft >= 60 * 60) {
            final int hours = (int) timeLeft / (60 * 60);
            timeLeft -= 60 * 60 * hours;
            if (mini) {
                stringTimeLeft += hours + "h ";
            } else {
                stringTimeLeft += hours + " " + lConfig.getPlural("hour", hours) + ", ";
            }
        }
        if (timeLeft >= 60) {
            final int minutes = (int) timeLeft / (60);
            timeLeft -= 60 * minutes;
            if (mini) {
                stringTimeLeft += minutes + "m ";
            } else {
                stringTimeLeft += minutes + " " + lConfig.getPlural("minute", minutes) + ", ";
            }
        } else {
            // Lets remove the last comma, since it will look bad with 2 days, 3
            // hours, and 14 seconds.
            if (!stringTimeLeft.equalsIgnoreCase("") && !mini) {
                stringTimeLeft = stringTimeLeft.substring(
                        0, stringTimeLeft.length() - 1);
            }
        }
        final int secs = (int) timeLeft;
        if (mini) {
            stringTimeLeft += secs + "s";
        } else {
            if (!stringTimeLeft.equalsIgnoreCase("")) {
                stringTimeLeft += "and ";
            }
            stringTimeLeft += secs + " " + lConfig.getPlural("second", secs);
        }

        return stringTimeLeft;
    }

    public static int parseInt(final String arg) {
        int newInt = 0;
        try {
            newInt = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
        }
        return newInt > 0 ? newInt : 0;
    }

    public static double parseDouble(final String arg) {
        double newDouble = 0;
        try {
            newDouble = Double.parseDouble(arg);
        } catch (NumberFormatException e) {
        }
        return newDouble > 0 ? newDouble : 0;
    }
}
