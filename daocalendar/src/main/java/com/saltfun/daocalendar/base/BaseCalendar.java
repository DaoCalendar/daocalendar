package com.saltfun.daocalendar.base;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaseCalendar
{
    public static final int DATE_WITHIN_RANGE = 7;
    public static final int MILLISECOND_PER_MINUTE = 60000;
    private static boolean dst_adjust;
    private static boolean dst_adjust_save;
    private static TimeZone gmt_zone;
    private static int[] zone_buf;
    private static Calendar work_cal;
    
    static {
        BaseCalendar.dst_adjust = true;
        BaseCalendar.gmt_zone = TimeZone.getTimeZone("GMT");
        BaseCalendar.zone_buf = new int[5];
        BaseCalendar.work_cal = null;
    }
    
    public static String chineseNumber(final int num, final boolean month, final boolean day) {
        final String[] array = Resource.getStringArray("numbers");
        if (num <= 10) {
            if (month) {
                return (num == 1) ? array[12] : array[num];
            }
            if (day) {
                return String.valueOf(array[11]) + array[num];
            }
            return array[num];
        }
        else {
            if (num < 20) {
                return String.valueOf(array[10]) + array[num % 10];
            }
            String str = String.valueOf(array[num / 10]) + array[10];
            if (num % 10 != 0) {
                str = String.valueOf(str) + array[num % 10];
            }
            return str;
        }
    }
    
    public static int[] timePeriod(final int[] s_date, final int day) {
        final int[] period = s_date.clone();
        addTime(period, 5, day);
        for (int i = 2; i >= 0; --i) {
            final int[] array = period;
            final int n = i;
            array[n] -= s_date[i];
            if (period[i] < 0) {
                if (i == 2) {
                    final int[] array2 = period;
                    final int n2 = i;
                    array2[n2] += 30;
                }
                else if (i == 1) {
                    final int[] array3 = period;
                    final int n3 = i;
                    array3[n3] += 12;
                }
                final int[] array4 = period;
                final int n4 = i - 1;
                --array4[n4];
            }
        }
        return period;
    }
    
    public static String formatDate(final double longitude, final int[] date, final int[] result, final double solar_adj, final boolean subtract, final boolean time_only) {
        int minute = (int)Math.round(1440.0 * longitude / 360.0 + solar_adj);
        if (subtract) {
            minute = -minute;
        }
        initTime(date);
        BaseCalendar.work_cal.add(12, minute);
        final String sign = (minute < 0) ? "-" : "+";
        if (minute < 0) {
            minute = -minute;
        }
        final int hour = minute / 60;
        minute -= hour * 60;
        final DecimalFormat format = new DecimalFormat("00");
        final String zone_str = " (GMT" + sign + hour + ":" + format.format(minute) + ")";
        if (result != null) {
            getTime(BaseCalendar.work_cal, result);
        }
        final String str = getDateString();
        int h = BaseCalendar.work_cal.get(10);
        if (h == 0) {
            h = 12;
        }
        final String time = String.valueOf(format.format(h)) + ":" + format.format(BaseCalendar.work_cal.get(12)) + ((BaseCalendar.work_cal.get(9) == 0) ? "AM" : "PM");
        if (time_only) {
            return time;
        }
        return String.valueOf(str) + " " + time + " " + zone_str;
    }
    
    public static String formatDate(String zone_name, final int[] date, final int[] result, boolean adjust_dst, final boolean time_only) {
        if (zone_name == null || zone_name.equals("Unknown")) {
            zone_name = "GMT";
        }
        final TimeZone zone = TimeZone.getTimeZone(City.mapZoneName(zone_name));
        initTime(date);
        int minute = getZoneOffset(zone);
        BaseCalendar.work_cal.add(12, minute);
        final boolean in_dst = City.inDaylightTime(zone, zone_name, date[0], getDate(zone));
        if (!BaseCalendar.dst_adjust) {
            adjust_dst = false;
        }
        if (adjust_dst && in_dst) {
            final int delta = getDstOffset(zone);
            BaseCalendar.work_cal.add(12, delta);
            minute += delta;
        }
        final String sign = (minute < 0) ? "-" : "+";
        if (minute < 0) {
            minute = -minute;
        }
        final int hour = minute / 60;
        minute -= hour * 60;
        final DecimalFormat format = new DecimalFormat("00");
        String zone_str = zone.getDisplayName(adjust_dst && in_dst, 0);
        if (!zone_str.startsWith("GMT")) {
            zone_str = String.valueOf(zone_str) + " (GMT" + sign + hour + ":" + format.format(minute) + ")";
        }
        final String str = getDateString();
        if (result != null) {
            getTime(BaseCalendar.work_cal, result);
        }
        int h = BaseCalendar.work_cal.get(10);
        if (h == 0) {
            h = 12;
        }
        final String time = String.valueOf(format.format(h)) + ":" + format.format(BaseCalendar.work_cal.get(12)) + ((BaseCalendar.work_cal.get(9) == 0) ? "AM" : "PM");
        if (time_only) {
            return time;
        }
        return String.valueOf(str) + " " + time + " " + zone_str;
    }
    
    public static String formatDate(final int[] date, final boolean time_only, final boolean html) {
        initTime(date);
        date[0] = BaseCalendar.work_cal.get(1);
        date[1] = BaseCalendar.work_cal.get(2) + 1;
        date[2] = BaseCalendar.work_cal.get(5);
        final DecimalFormat format = new DecimalFormat("00");
        int h = BaseCalendar.work_cal.get(10);
        if (h == 0) {
            h = 12;
        }
        final String str = String.valueOf(format.format(h)) + ":" + format.format(BaseCalendar.work_cal.get(12)) + ((BaseCalendar.work_cal.get(9) == 0) ? "AM" : "PM");
        if (time_only) {
            return str;
        }
        final boolean bc = BaseCalendar.work_cal.get(0) == 0;
        if (bc && !html) {
            date[0] = -date[0] + 1;
        }
        return String.valueOf(format.format(date[1])) + "/" + format.format(date[2]) + "/" + format.format(date[0]) + ((bc && html) ? " B.C." : "") + " " + str;
    }
    
    public static String formatDate(String zone_name, final int[] date) {
        if (zone_name == null || zone_name.equals("Unknown")) {
            zone_name = "GMT";
        }
        final TimeZone zone = TimeZone.getTimeZone(City.mapZoneName(zone_name));
        initTime(date);
        int minute = getZoneOffset(zone);
        BaseCalendar.work_cal.add(12, minute);
        final boolean in_dst = City.inDaylightTime(zone, zone_name, date[0], getDate(zone));
        if (BaseCalendar.dst_adjust && in_dst) {
            final int delta = getDstOffset(zone);
            BaseCalendar.work_cal.add(12, delta);
            minute += delta;
        }
        final boolean bc = BaseCalendar.work_cal.get(0) == 0;
        final int year = BaseCalendar.work_cal.get(1);
        final int month = BaseCalendar.work_cal.get(2) + 1;
        final int day = BaseCalendar.work_cal.get(5);
        return String.valueOf(Integer.toString(month)) + Resource.getString("month_char") + Integer.toString(day) + Resource.getString("day_char") + Integer.toString(year) + Resource.getString("year_char") + (bc ? " B.C." : "");
    }
    
    public static void addZoneOffset(final String zone_name, final int[] date, final int mode, final boolean gmt_to_local) {
        if (zone_name == null || zone_name.equals("Unknown")) {
            return;
        }
        final TimeZone zone = TimeZone.getTimeZone(City.mapZoneName(zone_name));
        initTime(date);
        if (mode <= 0 && gmt_to_local) {
            final int m = getZoneOffset(zone);
            BaseCalendar.work_cal.add(12, m);
        }
        if (mode >= 0 && BaseCalendar.dst_adjust && City.inDaylightTime(zone, zone_name, date[0], getDate(zone))) {
            final int delta = getDstOffset(zone);
            BaseCalendar.work_cal.add(12, gmt_to_local ? delta : (-delta));
        }
        if (mode <= 0 && !gmt_to_local) {
            final int m = getZoneOffset(zone);
            BaseCalendar.work_cal.add(12, -m);
        }
        getTime(BaseCalendar.work_cal, date);
    }
    
    public static void addZoneOffset(final int[] date, final boolean gmt_to_local) {
        final TimeZone zone = TimeZone.getDefault();
        initTime(date);
        if (gmt_to_local) {
            final int m = getZoneOffset(zone);
            BaseCalendar.work_cal.add(12, m);
        }
        if (BaseCalendar.dst_adjust && City.inDaylightTime(zone, zone.getDisplayName(), date[0], getDate(zone))) {
            final int delta = getDstOffset(zone);
            BaseCalendar.work_cal.add(12, gmt_to_local ? delta : (-delta));
        }
        if (!gmt_to_local) {
            final int m = getZoneOffset(zone);
            BaseCalendar.work_cal.add(12, -m);
        }
        getTime(BaseCalendar.work_cal, date);
    }
    
    public static boolean withinDateRange(final int[] date, final int within) {
        final Calendar cal = Calendar.getInstance();
        cal.add(5, -within);
        final Date begin_date = cal.getTime();
        cal.add(5, 2 * within);
        final Date end_date = cal.getTime();
        setTime(cal, date);
        final Date now_date = cal.getTime();
        return !now_date.before(begin_date) && !now_date.after(end_date);
    }
    
    public static Date getDate(final int[] date_buf, final TimeZone zone) {
        initTime(date_buf);
        return getDate(zone);
    }
    
    public static void addTime(final int[] date, final int field, final int val) {
        initTime(date);
        BaseCalendar.work_cal.add(field, val);
        getTime(BaseCalendar.work_cal, date);
    }
    
    public static void getCalendar(Calendar cal, final int[] date) {
        if (cal == null) {
            cal = Calendar.getInstance();
        }
        getTime(cal, date);
    }
    
    public static void initTime(final int[] date) {
        if (BaseCalendar.work_cal == null) {
            (BaseCalendar.work_cal = Calendar.getInstance()).setTimeZone(BaseCalendar.gmt_zone);
        }
        setTime(BaseCalendar.work_cal, date);
    }
    
    public static void setTime(final Calendar cal, final int[] date) {
        final boolean bc = date[0] <= 0;
        cal.set(0, bc ? 0 : 1);
        cal.set(1, bc ? (-date[0] + 1) : date[0]);
        cal.set(2, date[1] - 1);
        cal.set(5, date[2]);
        cal.set(11, date[3]);
        cal.set(12, date[4]);
    }
    
    public static void getTime(final Calendar cal, final int[] date) {
        date[0] = cal.get(1);
        if (cal.get(0) == 0) {
            date[0] = -date[0] + 1;
        }
        date[1] = cal.get(2) + 1;
        date[2] = cal.get(5);
        date[3] = cal.get(11);
        date[4] = cal.get(12);
    }
    
    public static String getDateString() {
        final TimeZone old_zone = TimeZone.getDefault();
        TimeZone.setDefault(BaseCalendar.gmt_zone);
        final DateFormat date_format = new SimpleDateFormat("MMM dd, yyyy");
        String str = date_format.format(BaseCalendar.work_cal.getTime());
        TimeZone.setDefault(old_zone);
        if (BaseCalendar.work_cal.get(0) == 0) {
            str = String.valueOf(str) + " B.C.";
        }
        return str;
    }
    
    public static int getZoneOffset(final TimeZone zone) {
        getTime(BaseCalendar.work_cal, BaseCalendar.zone_buf);
        BaseCalendar.work_cal.setTimeZone(zone);
        final int minute = BaseCalendar.work_cal.get(15) / 60000;
        BaseCalendar.work_cal.setTimeZone(BaseCalendar.gmt_zone);
        initTime(BaseCalendar.zone_buf);
        return minute;
    }
    
    public static int getDstOffset(final TimeZone zone) {
        getTime(BaseCalendar.work_cal, BaseCalendar.zone_buf);
        BaseCalendar.work_cal.setTimeZone(zone);
        initTime(BaseCalendar.zone_buf);
        final int minute = City.getDstOffset(BaseCalendar.work_cal);
        BaseCalendar.work_cal.setTimeZone(BaseCalendar.gmt_zone);
        initTime(BaseCalendar.zone_buf);
        return minute;
    }
    
    public static Date getDate(final TimeZone zone) {
        getTime(BaseCalendar.work_cal, BaseCalendar.zone_buf);
        BaseCalendar.work_cal.setTimeZone(zone);
        initTime(BaseCalendar.zone_buf);
        final Date date = BaseCalendar.work_cal.getTime();
        BaseCalendar.work_cal.setTimeZone(BaseCalendar.gmt_zone);
        initTime(BaseCalendar.zone_buf);
        return date;
    }
    
    public static String auditDay(String str, int[] date) {
        str = str.trim().toLowerCase();
        int am_pm = 0;
        if (str.endsWith("am")) {
            am_pm = -1;
        }
        else if (str.endsWith("pm")) {
            am_pm = 1;
        }
        if (am_pm != 0) {
            str = str.substring(0, str.length() - 2);
        }
        final StringTokenizer st = new StringTokenizer(str, "/,: \t");
        if (date == null) {
            date = new int[5];
        }
        Arrays.fill(date, 0);
        int i = 0;
        boolean year_first = false;
        while (st.hasMoreTokens() && i < 5) {
            str = st.nextToken();
            if (str.equals("")) {
                continue;
            }
            try {
                final int n = Integer.parseInt(str);
                if (year_first) {
                    date[i] = n;
                }
                else if (i == 0) {
                    if (n > 100 || n < 0) {
                        year_first = true;
                        date[0] = n;
                    }
                    else {
                        date[1] = n;
                    }
                }
                else if (i == 1) {
                    date[2] = n;
                }
                else if (i == 2) {
                    date[0] = n;
                }
                else {
                    date[i] = n;
                }
                ++i;
            }
            catch (NumberFormatException ex) {}
        }
        if (i < 3) {
            getCalendar(null, date);
        }
        else if (i == 5 && am_pm != 0) {
            if (am_pm > 0) {
                if (date[3] != 12) {
                    final int[] array = date;
                    final int n2 = 3;
                    array[n2] += 12;
                }
            }
            else if (date[3] == 12) {
                date[3] = 0;
            }
        }
        return formatDate(date, false, false);
    }
    
    public static void pushSetDstAdjust(final boolean set) {
        BaseCalendar.dst_adjust_save = BaseCalendar.dst_adjust;
        BaseCalendar.dst_adjust = set;
    }
    
    public static void popDstAdjust() {
        BaseCalendar.dst_adjust = BaseCalendar.dst_adjust_save;
    }
    
    public static void setDstAdjust(final boolean set) {
        BaseCalendar.dst_adjust = set;
    }
    
    public static boolean getDstAdjust() {
        return BaseCalendar.dst_adjust;
    }
    
    public void getCalendar(final int[] date) {
    }
}
