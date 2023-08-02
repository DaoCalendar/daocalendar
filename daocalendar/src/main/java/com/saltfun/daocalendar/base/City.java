package com.saltfun.daocalendar.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;

public class City
{
    public static final double INVALID = Double.NEGATIVE_INFINITY;
    public static final String UNKNOWN_ZONE = "Unknown";
    private static final double MIN_ERROR_RATIO = 0.99;
    private static final double MATCH_ERROR = 0.125;
    private static final double TIGHT_MATCH_ERROR = 0.01;
    private static final double ANY_MATCH_ERROR = 180.0;
    public static final double MATCH_ERROR_SQ = 0.015625;
    public static final double TIGHT_MATCH_ERROR_SQ = 1.0E-4;
    public static final double ANY_MATCH_ERROR_SQ = 32400.0;
    private static City[] cities;
    private static City[] map_cities;
    private static Hashtable<String, LinkedList<DstEntry>> dst_override;
    private static DstEntry dst_last;
    private String country;
    private String city;
    private String zone;
    private double longitude;
    private double latitude;
    
    public City(final String which_country, final String which_city, final double long_val, final double lat_val, final String which_zone) {
        this.country = which_country;
        this.city = which_city;
        this.longitude = long_val;
        this.latitude = lat_val;
        this.zone = which_zone;
    }
    
    public String getCountryName() {
        return this.country;
    }
    
    public String getCityName() {
        return this.city;
    }
    
    public double getLongitude() {
        return this.longitude;
    }
    
    public double getLatitude() {
        return this.latitude;
    }
    
    public String getZoneName() {
        return this.zone;
    }
    
    public void setZoneName(final String t_zone) {
        this.zone = t_zone;
    }
    
    public static void loadCities(final String file_name) {
        City.dst_override = new Hashtable<>();
        final LinkedList[] head_group = new LinkedList[2];
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(FileIO.getURL(file_name).openStream(), "UTF-16"));
            final String[] field = new String[5];
            for (int iter = 0; iter < 3; ++iter) {
                int index;
                if ((index = iter) == 2) {
                    index = (Resource.isSimplified() ? 0 : 1);
                }
                if (head_group[index] == null) {
                    head_group[index] = new LinkedList();
                }
                final LinkedList head = head_group[index];
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() != 0) {
                        if (line.startsWith("#")) {
                            continue;
                        }
                        if (line.equals("---")) {
                            break;
                        }
                        final StringTokenizer st = new StringTokenizer(line, "|");
                        final int n = st.countTokens();
                        if (n != 4 && n != 5) {
                            continue;
                        }
                        if (n == 4) {
                            field[4] = null;
                        }
                        for (int i = 0; i < n; ++i) {
                            field[i] = st.nextToken().trim();
                        }
                        try {
                            final City city = new City(field[0], field[1], Double.parseDouble(field[2]), Double.parseDouble(field[3]), field[4]);
                            head.addLast(city);
                        }
                        catch (Exception ex) {}
                    }
                }
            }
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() != 0) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.equals("---")) {
                        break;
                    }
                    final StringTokenizer st2 = new StringTokenizer(line, "|");
                    final int n2 = st2.countTokens();
                    if (n2 != 4) {
                        continue;
                    }
                    final String zone_name = st2.nextToken().trim();
                    LinkedList<DstEntry> head2 = City.dst_override.get(zone_name);
                    if (head2 == null) {
                        head2 = new LinkedList<>();
                        City.dst_override.put(zone_name, head2);
                    }
                    final TimeZone zone = TimeZone.getTimeZone(zone_name);
                    final DstEntry entry = new DstEntry();
                    final int[] date = new int[5];
                    for (int j = 0; j < 2; ++j) {
                        final StringTokenizer nst = new StringTokenizer(st2.nextToken().trim(), ",");
                        for (int k = 0; k < 5; ++k) {
                            date[k] = FileIO.parseInt(nst.nextToken().trim(), 0, true);
                        }
                        if (j == 0) {
                            entry.start = BaseCalendar.getDate(date, zone);
                        }
                        else {
                            entry.end = BaseCalendar.getDate(date, zone);
                        }
                    }
                    entry.offset = FileIO.parseInt(st2.nextToken().trim(), 0, false);
                    head2.addLast(entry);
                }
            }
            reader.close();
        }
        catch (IOException ignored) {}
        if (Resource.isSimplified()) {
            City.cities = (City[]) head_group[0].toArray(new City[1]);
            City.map_cities = (City[]) head_group[1].toArray(new City[1]);
        }
        else {
            City.cities = (City[]) head_group[1].toArray(new City[1]);
            City.map_cities = (City[]) head_group[0].toArray(new City[1]);
        }
    }
    
    static boolean inDaylightTime(final TimeZone zone, final String zone_name, final int year, final Date date) {
        City.dst_last = null;
        final LinkedList<DstEntry> head = City.dst_override.get(zone_name);
        if (head != null) {
            for (DstEntry dst : head) {
                if (!date.before(dst.start) && !date.after(dst.end)) {
                    City.dst_last = dst;
                    return dst.offset != 0;
                }
                if (date.before(dst.start)) {
                    break;
                }
            }
        }
        return zone.inDaylightTime(date);
    }
    
    public static int getDstOffset(final Calendar cal) {
        return (City.dst_last == null) ? (cal.get(16) / 60000) : City.dst_last.offset;
    }
    
    public static String getDefaultCountry() {
        return City.cities[0].getCountryName();
    }
    
    public static String getDefaultCity() {
        return City.cities[0].getCityName();
    }
    
    public static String getUnknownCountry() {
        return City.cities[City.cities.length - 1].getCountryName();
    }
    
    public static String toMinuteSeconds(final double degree, final boolean has_second) {
        int second = (int)Math.round(3600.0 * degree);
        final int minute = second / 60;
        second -= 60 * minute;
        final DecimalFormat format = new DecimalFormat("00");
        if (has_second) {
            return String.valueOf(format.format(minute)) + "'" + format.format(second);
        }
        return format.format(minute + ((second >= 30) ? 1 : 0));
    }
    
    public static String formatLongLatitude(double degree, final boolean is_long, final boolean show_second, final boolean align) {
        if (degree == Double.NEGATIVE_INFINITY) {
            return "?invalid?";
        }
        String str;
        if (align) {
            final DecimalFormat format = new DecimalFormat("000");
            str = format.format((int)Math.abs(degree));
        }
        else {
            str = Integer.toString((int)Math.abs(degree));
        }
        if (degree < 0.0) {
            degree = -degree;
            str = String.valueOf(str) + (is_long ? "W" : "S");
        }
        else {
            str = String.valueOf(str) + (is_long ? "E" : "N");
        }
        degree -= (int)degree;
        return String.valueOf(str) + toMinuteSeconds(degree, show_second);
    }
    
    public static boolean parseLongLatitude(final String city, final String country, final double[] long_lat) {
        final City c = matchCity(city, country, false);
        if (c != null) {
            long_lat[0] = c.getLongitude();
            long_lat[1] = c.getLatitude();
            return true;
        }
        return parseLongLatitude(city, long_lat);
    }
    
    public static boolean parseLongLatitude(final String str, final double[] long_lat) {
        final String key = str.toUpperCase();
        final StringTokenizer st = new StringTokenizer(key, ", ");
        if (st.countTokens() == 2) {
            final double long_val = parseLongLatitude(st.nextToken(), 'E', 'W');
            final double lat_val = parseLongLatitude(st.nextToken(), 'N', 'S');
            if (long_val != Double.NEGATIVE_INFINITY && lat_val != Double.NEGATIVE_INFINITY) {
                long_lat[0] = long_val;
                long_lat[1] = lat_val;
                return true;
            }
        }
        return false;
    }
    
    public static double parseLongLatitude(String val, final char p_char, final char n_char) {
        try {
            boolean negative = false;
            double degree = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < 2; ++i) {
                final char c = (i == 0) ? p_char : n_char;
                int ch_index = val.indexOf(c);
                if (ch_index < 0) {
                    ch_index = val.indexOf(Character.toLowerCase(c));
                }
                if (ch_index >= 0) {
                    if (ch_index == val.length() - 1) {
                        final int co_index = val.indexOf(58);
                        if (co_index >= 0) {
                            degree = Double.parseDouble(val.substring(0, co_index));
                            val = val.substring(co_index + 1, ch_index);
                        }
                        else {
                            degree = Double.parseDouble(val.substring(0, ch_index));
                            val = "0";
                        }
                    }
                    else {
                        degree = Double.parseDouble(val.substring(0, ch_index));
                        val = val.substring(ch_index + 1);
                    }
                    negative = (i != 0);
                }
            }
            if (degree != Double.NEGATIVE_INFINITY) {
                final int ch_index = val.indexOf(39);
                double minute;
                if (ch_index >= 0) {
                    minute = Double.parseDouble(val.substring(0, ch_index));
                    if (val.length() > ch_index + 1) {
                        val = val.substring(ch_index + 1);
                        final double second = Double.parseDouble(val);
                        minute += second / 60.0;
                    }
                }
                else {
                    minute = Double.parseDouble(val);
                }
                degree += minute / 60.0;
                if (negative) {
                    degree = -degree;
                }
                return degree;
            }
            return Double.parseDouble(val);
        }
        catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }
    
    public static String mapZoneName(final String name) {
        if (name.startsWith("Etc/GMT+")) {
            return name.replace('+', '-');
        }
        if (name.startsWith("Etc/GMT-")) {
            return name.replace('-', '+');
        }
        return name;
    }
    
    public static String[] getAllZoneNames() {
        final String[] zone_ids = TimeZone.getAvailableIDs();
        final String[] zone_names = zone_ids.clone();
        for (int i = 0; i < zone_names.length; ++i) {
            zone_names[i] = mapZoneName(zone_names[i]);
        }
        return zone_names;
    }
    
    public static String[] getCountryList() {
        final LinkedList<String> head = new LinkedList<String>();
        for (int len = City.cities.length, i = 0; i < len; ++i) {
            final City c = City.cities[i];
            final String name = c.getCountryName();
            if (i == 0 || !City.cities[i - 1].getCountryName().equals(name)) {
                head.add(name);
            }
        }
        return (String[]) head.toArray(new String[1]);
    }
    
    public static String[] getCityList(final String country_name) {
        final LinkedList<String> head = new LinkedList<>();
        for (int len = City.cities.length, i = 0; i < len; ++i) {
            final City c = City.cities[i];
            if (c.getCountryName().equals(country_name)) {
                head.add(c.getCityName());
            }
        }
        return (String[]) head.toArray(new String[1]);
    }
    
    public static City matchCity(final String city_name, final String country_name, final boolean use_map) {
        final City[] array = use_map ? City.map_cities : City.cities;
        final int len = array.length;
        City s_c = null;
        for (final City c : array) {
            if (c.getCountryName().equalsIgnoreCase(country_name)) {
                if (c.getCityName().equalsIgnoreCase(city_name)) {
                    return c;
                }
            }
            else if (!use_map && c.getCityName().equalsIgnoreCase(city_name)) {
                s_c = c;
            }
        }
        return s_c;
    }
    
    static int matchCityIndex(final double long_val, final double lat_val, final double error) {
        if (Resource.getPrefInt("match_city") == 0) {
            return -1;
        }
        double min_error = 2.0 * error;
        int index = -1;
        for (int len = City.cities.length, i = 0; i < len; ++i) {
            final City c = City.cities[i];
            double val = c.getLongitude() - long_val;
            final double long_error = val * val;
            val = c.getLatitude() - lat_val;
            final double lat_error = val * val;
            if (long_error < error && lat_error < error && long_error + lat_error < min_error) {
                index = i;
                min_error = MIN_ERROR_RATIO * (long_error + lat_error);
            }
        }
        return index;
    }
    
    public static City matchCity(final double long_val, final double lat_val, final double error) {
        final int index = matchCityIndex(long_val, lat_val, error);
        if (index >= 0) {
            return City.cities[index];
        }
        return null;
    }
    
    public static City getCity(final int index) {
        if (index >= 0) {
            return City.cities[index];
        }
        return null;
    }
    
    public static City mapCountryCity(final String country, final String city) {
        if (country.equals(getDefaultCountry())) {
            for (int i = 0; i < City.cities.length; ++i) {
                final City c = City.cities[i];
                if (c.getCityName().equals(city)) {
                    return c;
                }
            }
        }
        else if (City.map_cities != null) {
            City c2 = matchCity(city, country, true);
            if (c2 != null) {
                c2 = matchCity(c2.getLongitude(), c2.getLatitude(), 1.0E-4);
            }
            return c2;
        }
        return null;
    }
    
    public static double normalizeDegree(double degree) {
        degree %= 360.0;
        if (degree < 0.0) {
            degree += 360.0;
        }
        return degree;
    }
    
    public static double parsePos(final String pos, final double def_val) {
        double degree;
        try {
            degree = normalizeDegree(Double.parseDouble(pos));
        }
        catch (NumberFormatException e) {
            degree = def_val;
        }
        return degree;
    }
    
    public static String formatPos(final double val, final int width, final int fraction_width, final boolean align) {
        return FileIO.formatDouble(normalizeDegree(val), width, fraction_width, align, false);
    }
    
    public static double parseMapPos(String pos) {
        if (pos == null || pos.trim().equals("")) {
            pos = "0.0";
        }
        return normalizeDegree(315.0 - parsePos(pos, 0.0));
    }
    
    public static String formatMapPos(final double val, final boolean align) {
        return formatPos(315.0 - val, 3, 2, align);
    }
    
    private static class DstEntry
    {
        public Date start;
        public Date end;
        int offset;
    }
}
