package net.erbros.lottery;

import org.bukkit.Material;

import java.util.*;


public class FormatUtil {
    public static String formatCost(double cost, LotteryConfig lConfig) {
        if (lConfig.useEconomy()) {
            return lConfig.formatCurrency((formatAmount(cost, lConfig.useEconomy())));
        } else {
            return String.valueOf(
                    (int) formatAmount(cost, lConfig.useEconomy())).concat(
                    " " + formatMaterialName(lConfig.getMaterial()));
        }
    }

    //Nobody uses iConomy anymore.
    public static double formatAmount(double amount,@Deprecated final boolean usingiConomy) {

        if (usingiConomy) {
            return Math.floor(amount * 100) / 100;
        } else {
            return Math.floor(amount);
        }
    }

    public static String formatMaterialName(final String materialId) {
        String rawMaterialName = Material.getMaterial(materialId).getData().getName();
        rawMaterialName = rawMaterialName.toLowerCase(Locale.ENGLISH);
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
            timeLeft -= 60L * 60 * 24 * days;
            if (mini) {
                stringTimeLeft += days + "d ";
            } else {
                stringTimeLeft += days + " " + lConfig.getPlural("day", days) + ", ";
            }
        }
        if (timeLeft >= 60 * 60) {
            final int hours = (int) timeLeft / (60 * 60);
            timeLeft -= 60L * 60 * hours;
            if (mini) {
                stringTimeLeft += hours + "h ";
            } else {
                stringTimeLeft += hours + " " + lConfig.getPlural("hour", hours) + ", ";
            }
        }
        if (timeLeft >= 60) {
            final int minutes = (int) timeLeft / (60);
            timeLeft -= 60L * minutes;
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
            stringTimeLeft += Integer.toString(secs) + " " + lConfig.getPlural("second", secs);
        }

        return stringTimeLeft;
    }

    public static int parseInt(final String arg) {
        int newInt = 0;
        try {
            newInt = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
        }
        return Math.max(newInt, 0);
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
