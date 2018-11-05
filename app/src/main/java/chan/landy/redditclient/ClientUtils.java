package chan.landy.redditclient;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ClientUtils {


    public static String getTimeAgo(long timeCreatedMillis) {
        long millisecondsAgo = System.currentTimeMillis() - timeCreatedMillis;
        long timeUnitNumber;
        String timeUnit;
        String timeAgoString;

        if(millisecondsAgo < Constants.MINUTE_MILLIS) {
            // show seconds
            timeUnitNumber = TimeUnit.SECONDS.convert(millisecondsAgo, TimeUnit.MILLISECONDS);
            timeUnit = "s";
        } else if(millisecondsAgo < Constants.HOUR_MILLIS) {
            // show minutes
            timeUnitNumber = TimeUnit.MINUTES.convert(millisecondsAgo, TimeUnit.MILLISECONDS);
            timeUnit = "m";
        } else if (millisecondsAgo < Constants.DAY_MILLIS) {
            // show hours
            timeUnitNumber = TimeUnit.HOURS.convert(millisecondsAgo, TimeUnit.MILLISECONDS);
            timeUnit = "h";
        } else {
            long days = TimeUnit.DAYS.convert(millisecondsAgo, TimeUnit.MILLISECONDS);

            if(days < 30) {
                // show days
                timeUnitNumber = TimeUnit.DAYS.convert(millisecondsAgo, TimeUnit.MILLISECONDS);
                timeUnit = "d";

            } else if(days < 365) {
            // show months
                timeUnitNumber = days / 30;
                timeUnit = "mo";
            } else {
                // show years
                timeUnitNumber = days / 365;
                timeUnit = "y";
            }
        }

        timeAgoString = String.format(Locale.US, "%d%s", (int)timeUnitNumber, timeUnit);

        return timeAgoString;
    }

    public static String numberToShortFormat(Number number) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

}

