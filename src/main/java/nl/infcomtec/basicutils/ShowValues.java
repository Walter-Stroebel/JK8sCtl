/*
 * Copyright (c) 2015 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.basicutils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class provides a number of formatting routines to display common values.
 *
 * @author walter
 */
public class ShowValues {

    public static FormatDouble left9_5g() {
        return new FormatDouble() {

            @Override
            public String fmt(final double d) {
                return String.format("%-9.5g", d);
            }
        };
    }

    public static FormatDouble right9_5g() {
        return new FormatDouble() {

            @Override
            public String fmt(final double d) {
                return String.format("%9.5g", d);
            }
        };
    }

    public static FormatDouble time24() {
        return new FormatDouble() {

            @Override
            public String fmt(final double d) {
                return String.format("%1$tH:%1$tM:%1$tS", Math.round(d));
            }
        };
    }

    public static FormatDouble elapsedMillis() {
        return new FormatDouble() {

            @Override
            public String fmt(final double d) {
                return elaspedFromMillis(Math.round(d));
            }
        };
    }

    public static FormatDouble elapsedNanos() {
        return new FormatDouble() {

            @Override
            public String fmt(final double d) {
                return elaspedFromNanos(Math.round(d));
            }
        };
    }

    /**
     * Return a date and time string year-month-day SPACE hour:minute:second.
     *
     * @param millies Time in milli seconds.
     * @param dateSep The '-' between date parts.
     * @param dateTimeSep The SPACE between date and time.
     * @param timeSep The ':' between time parts.
     * @return Printable string.
     */
    public static String yyyymmddhhmmss(long millies, String dateSep, String dateTimeSep, String timeSep) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy" + dateSep + "MM" + dateSep + "dd" + dateTimeSep + "HH" + timeSep + "mm" + timeSep + "ss");
        return sdf.format(new Date(millies));
    }

    /**
     * Return as year-month-day hour:minute:second.
     *
     * @param millies Time in milli seconds.
     * @return Printable string.
     */
    public static String yyyymmddhhmmss(long millies) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(millies));
    }

    /**
     * Interpret a number as something human can easily read. Well, humans with
     * some technical background. Uses 1024 based tiers Kilo Mega Giga and Terra
     * in maximum 5 digit positions including the period.
     *
     * @param bytes Number of bytes to interpret.
     * @return A printable representation, 11 characters long, of the number of
     * bytes.
     */
    public static String humanLongDig5KMGT(long bytes) {
        if (bytes < 100000) {
            return String.format("%5d bytes", bytes);
        }
        double bts = bytes / 1024.0;
        if (bts < 1000) {
            return String.format("%5.1f Kbyte", bts);
        }
        bts /= 1024.0;
        if (bts < 1000) {
            return String.format("%5.1f Mbyte", bts);
        }
        bts /= 1024.0;
        if (bts < 1000) {
            return String.format("%5.1f Gbyte", bts);
        }
        bts /= 1024.0;
        return String.format("%5.1f Tbyte", bts);
    }

    /**
     * Interpret a number as something human can easily read. Well, humans with
     * some technical background. Uses ISO standard
     * Peta,Terra,Giga,Mega,Kilo,(unit),Milli,Micro,Nano,Pico based tiers in
     * maximum 6 digit positions including the period.
     *
     * @param number Number to interpret.
     * @return A printable representation, 8 characters long, of the number.
     */
    public static String humanDoubleISO(double number) {
        if (number == 0) {
            return String.format(Locale.US, "%8.3g", number);
        }
        double e = Math.log10(Math.abs(number));
        if (e > 18 || e < -18) {
            // no-one knows these, use general exponents
            return String.format(Locale.US, "%8.3g", number);
        }

        if (e > 15) {
            return String.format("%6.2f P", number / 1E15);
        }
        if (e > 12) {
            return String.format("%6.2f T", number / 1E12);
        }
        if (e > 9) {
            return String.format("%6.2f G", number / 1E9);
        }
        if (e > 6) {
            return String.format("%6.2f M", number / 1E6);
        }
        if (e > 3) {
            return String.format("%6.2f K", number / 1E3);
        }

        if (e < -15) {
            return String.format("%6.2f p", number * 1E15);
        }
        if (e < -12) {
            return String.format("%6.2f n", number * 1E12);
        }
        if (e < -9) {
            return String.format("%6.2f u", number * 1E9);
        }
        if (e < -3) {
            return String.format("%6.2f m", number * 1E6);
        }
        return String.format("%8.2f", number);
    }

    /**
     * Reduces the number to the rounded integer part. Eg. 3999 will be returned
     * as 4 K.
     *
     * @param number Number to interpret.
     * @return Printable ISO representation.
     */
    public static String humanLongIntegerISO(double number) {
        boolean neg = number < 0;
        if (neg) {
            number = -number;
        }
        String post = "";
        if (number > 1000) {
            post = "K";
            number /= 1000.0;
        }
        if (number > 1000) {
            post = "M";
            number /= 1000.0;
        }
        if (number > 1000) {
            post = "G";
            number /= 1000.0;
        }
        if (number > 1000) {
            post = "T";
            number /= 1000.0;
        }
        if (number < 10) {
            if (neg) {
                number = -number;
            }
            return String.format(Locale.US, "%.1f", number) + post;
        }
        if (neg) {
            number = -number;
        }
        return String.format(Locale.US, "%.0f", number) + post;
    }

    /**
     * Reduces the number to the rounded down integer part. Eg. 4095 will be
     * returned as 3 Kbyte, 4096 as 4 Kbyte.
     *
     * @param bytes Number to interpret.
     * @return Printable pessimistic representation.
     */
    public static String humanLongIntegerKMGT(long bytes) {
        String post = " bytes";
        if (bytes > 1024) {
            post = " Kbyte";
            bytes /= 1024;
        }
        if (bytes > 1024) {
            post = " Mbyte";
            bytes /= 1024;
        }
        if (bytes > 1024) {
            post = " Gbyte";
            bytes /= 1024;
        }
        if (bytes > 1024) {
            post = " Tbyte";
            bytes /= 1024;
        }
        return bytes + post;
    }

    /**
     * Return elapsed time in a human-sensible form.
     *
     */
    public static String elaspedFromMillis(long millis) {
        return elaspedFromNanos(millis * 1000000L);
    }

    /**
     * Return elapsed time in a human-sensible form.
     *
     */
    public static String elaspedFromNanos(long nanos) {
        if (nanos < 1000) {
            return String.format("%5d ns", nanos);
        }
        double us = nanos / 1000.0;
        if (us < 1000) {
            return String.format("%5.1f us", us);
        }
        double ms = us / 1000.0;
        if (ms < 1000) {
            return String.format("%5.1f ms", ms);
        }
        double hours = ms / 3600000L;
        if (hours < 1) {
            TimeZone here = TimeZone.getDefault();
            return String.format("%1$tM min %1$tS s", new Date(Math.round(ms) - here.getRawOffset()));
        }
        double days = hours / 24;
        if (days < 1) {
            TimeZone here = TimeZone.getDefault();
            return String.format("%1$tH:%1$tM:%1$tS", new Date(Math.round(ms) - here.getRawOffset()));
        } else if (days < 7) {
            long day = 1000000000L * 3600L * 24L;
            long ldays = nanos / day;
            return String.format("%d days, %s", ldays, elaspedFromNanos(nanos % day));
        } else {
            long week = 1000000000L * 3600L * 24L * 7L;
            long weeks = nanos / week;
            if (weeks < 52) {
                return String.format("%d weeks, %s", weeks, elaspedFromNanos(nanos % week));
            } else {
                return String.format("%d years, %d weeks, %s", weeks / 52, weeks % 52, elaspedFromNanos(nanos % week));
            }
        }
    }
}
