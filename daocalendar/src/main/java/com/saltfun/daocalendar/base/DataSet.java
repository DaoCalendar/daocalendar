package com.saltfun.daocalendar.base;

public class DataSet
{
    public static final int DATA = 0;
    public static final int PICK = 1;
    public static final int MAX_TYPE = 2;
    public static final String STYLE_MARKER;
    private static final String[] ALTERNATE_MAP;
    private static String[] special_map;
    private String footer;
    private int[] last_index;
    private int[] max_entry;
    private DataEntry[][] entries;
    
    static {
        STYLE_MARKER = new String(new char[] { '\u0006' });
        ALTERNATE_MAP = new String[] { "@a", "@b", "@c", "@d", "@e", "@f" };
    }
    
    public DataSet() {
        (this.last_index = new int[2])[0] = (this.last_index[1] = -1);
        this.max_entry = new int[2];
        this.entries = new DataEntry[2][];
    }
    
    public String getFooter() {
        return this.footer;
    }
    
    public void setFooter(final String val) {
        this.footer = val;
    }
    
    public boolean hasDataEntry(final int index, final int type) {
        return this.entries[type][index] != null;
    }
    
    public DataEntry getDataEntry(final int index, final int type) {
        if (this.entries[type][index] == null) {
            this.entries[type][index] = new DataEntry();
        }
        return this.entries[type][index];
    }
    
    public void setDataEntry(final int index, final DataEntry entry, final int type) {
        this.entries[type][index] = entry;
    }
    
    public int getMaxDataEntry(final int type) {
        return this.max_entry[type];
    }
    
    public void setMaxDataEntry(final int val, final int type) {
        this.max_entry[type] = val;
        if (this.entries[type] != null) {
            if (this.entries[type].length >= val) {
                return;
            }
            final DataEntry[] new_entries = new DataEntry[val];
            for (int i = 0; i < this.entries[type].length; ++i) {
                new_entries[i] = this.entries[type][i];
            }
            this.entries[type] = new_entries;
        }
        else {
            this.entries[type] = new DataEntry[val];
        }
    }
    
    public int getLastIndex(final int type) {
        return this.last_index[type];
    }
    
    public void setLastIndex(final int index, final int type) {
        this.last_index[type] = index;
    }
    
    public static String[] getMapString() {
        return DataSet.special_map;
    }
    
    public static void setMapString(final String[] map) {
        DataSet.special_map = map;
    }
    
    public static void setAlternateMapString() {
        DataSet.special_map = DataSet.ALTERNATE_MAP;
    }
    
    public static String removeStyle(String str) {
        final int index = str.indexOf(DataSet.STYLE_MARKER);
        if (index >= 0) {
            str = str.substring(0, index);
        }
        return str;
    }
    
    private static String mapString(String str) {
        if (DataSet.special_map == null) {
            str = str.replaceAll("\"", new String(new char[] { '\u0001' }));
            str = str.replaceAll("\r", new String(new char[] { '\u0002' }));
            str = str.replaceAll("\n", new String(new char[] { '\u0003' }));
            str = str.replaceAll("=", new String(new char[] { '\u0004' }));
            str = str.replaceAll("#", new String(new char[] { '\u0005' }));
        }
        else {
            str = str.replaceAll("\"", DataSet.special_map[0]);
            str = str.replaceAll("\r", DataSet.special_map[1]);
            str = str.replaceAll("\n", DataSet.special_map[2]);
            if (DataSet.special_map.length > 3) {
                str = str.replaceAll("=", DataSet.special_map[3]);
                str = str.replaceAll("#", DataSet.special_map[4]);
                if (DataSet.special_map.length > 5) {
                    str = str.replaceAll(DataSet.STYLE_MARKER, DataSet.special_map[5]);
                }
            }
        }
        return Character.isWhitespace(str.codePointAt(0)) ? (String.valueOf(DataSet.special_map[3]) + str) : str;
    }
    
    private static String unmapString(String str) {
        if (DataSet.special_map == null) {
            str = str.replaceAll(new String(new char[] { '\u0001' }), "\"");
            str = str.replaceAll(new String(new char[] { '\u0002' }), "\r");
            str = str.replaceAll(new String(new char[] { '\u0003' }), "\n");
            str = str.replaceAll(new String(new char[] { '\u0004' }), "=");
            str = str.replaceAll(new String(new char[] { '\u0005' }), "#");
        }
        else {
            str = str.replaceAll(DataSet.special_map[0], "\"");
            str = str.replaceAll(DataSet.special_map[1], "\r");
            str = str.replaceAll(DataSet.special_map[2], "\n");
            if (DataSet.special_map.length > 3) {
                str = str.replaceAll(DataSet.special_map[3], "=");
                str = str.replaceAll(DataSet.special_map[4], "#");
                if (DataSet.special_map.length > 5) {
                    str = str.replaceAll(DataSet.special_map[5], DataSet.STYLE_MARKER);
                }
            }
        }
        return str.startsWith("=") ? str.substring(1) : str;
    }
    
    public boolean loadData(final String file_name) {
        final FileIO file = new FileIO(file_name, false);
        setMapString((String[])(file.hasKey("map_seq") ? file.getStringArray("map_seq") : null));
        if (file.hasKey("desc")) {
            this.setFooter(unmapString(file.getString("desc")));
        }
        Resource.setAlternatePref(file.hasKey("pref") ? file.getString("pref") : null);
        boolean has_data = false;
        for (int iter = 0; iter < 2; ++iter) {
            String prefix = "";
            if (iter > 0) {
                prefix = "t" + Integer.toString(iter) + "_";
            }
            String key = "max_entry";
            if (iter > 0) {
                key = String.valueOf(prefix) + key;
            }
            this.max_entry[iter] = (file.hasKey(key) ? file.getInt(key) : 1);
            key = "index";
            if (iter > 0) {
                key = String.valueOf(prefix) + key;
            }
            if (this.max_entry[iter] > 1 && file.hasKey(key)) {
                this.last_index[iter] = file.getInt(key);
            }
            this.entries[iter] = new DataEntry[this.max_entry[iter]];
            boolean has_empty = false;
            for (int i = 0; i < this.max_entry[iter]; ++i) {
                final String suffix = (i > 0) ? Integer.toString(i) : "";
                final DataEntry entry = this.getDataEntry(i, iter);
                if (!loadDataEntry(file, entry, prefix, suffix, iter)) {
                    this.entries[iter][i] = null;
                    has_empty = true;
                }
            }
            if (has_empty) {
                int count = -1;
                for (int j = 0; j < this.max_entry[iter]; ++j) {
                    if (this.entries[iter][j] == null) {
                        if (count < 0) {
                            count = j;
                        }
                    }
                    else if (count >= 0) {
                        this.entries[iter][count++] = this.entries[iter][j];
                    }
                }
                this.max_entry[iter] = count;
                this.last_index[iter] = 0;
            }
            if (this.max_entry[iter] > 0) {
                has_data = true;
            }
        }
        file.dispose();
        return has_data;
    }
    
    public static boolean loadDataEntry(final FileIO file, final DataEntry entry, final String prefix, final String suffix, final int type) {
        final int[] date = new int[5];
        final boolean good = file.getIntArray(String.valueOf(prefix) + "date" + suffix, date) == 5;
        if (!good) {
            return false;
        }
        entry.setBirthDay(date);
        String key = String.valueOf(prefix) + "name" + suffix;
        entry.setName(file.hasKey(key) ? file.getString(key) : null);
        key = String.valueOf(prefix) + "sex" + suffix;
        entry.setSex(!file.hasKey(key) || file.getString(key).equals("male"));
        if (type == 1) {
            key = String.valueOf(prefix) + "dayset" + suffix;
            entry.setChoice(!file.hasKey(key) || file.getString(key).equals("day_choice"));
            key = String.valueOf(prefix) + "degree" + suffix;
            entry.setMountainPos(file.hasKey(key) ? file.getString(key) : "0.0");
        }
        else {
            key = String.valueOf(prefix) + "now" + suffix;
            if (file.hasKey(key)) {
                if (file.getIntArray(key, date) != 5) {
                    entry.setNowDay(null);
                }
                else {
                    entry.setNowDay(date);
                }
            }
        }
        setCountryCity(entry, file.getString(String.valueOf(prefix) + "country" + suffix), file.getString(String.valueOf(prefix) + "city" + suffix));
        entry.setZone(file.getString(String.valueOf(prefix) + "zone" + suffix));
        key = String.valueOf(prefix) + "override" + suffix;
        entry.setOverride(file.hasKey(key) ? file.getString(key) : null);
        key = String.valueOf(prefix) + "note" + suffix;
        entry.setNote(file.hasKey(key) ? unmapString(file.getString(key)) : null);
        return true;
    }
    
    private static void setCountryCity(final DataEntry entry, final String country, final String city) {
        final City c = City.mapCountryCity(country, city);
        if (c != null) {
            entry.setCountry(c.getCountryName());
            entry.setCity(c.getCityName());
        }
        else {
            entry.setCountry(country);
            entry.setCity(city);
        }
    }
    
    public void saveData(final String file_name) {
        final FileIO file = new FileIO(file_name, false, true);
        if (DataSet.special_map != null) {
            file.putStringArray("map_seq", DataSet.special_map);
        }
        if (this.footer != null && !this.footer.trim().equals("")) {
            file.putString("desc", mapString(this.footer));
        }
        final String pref = Resource.getAlternatePref();
        if (pref != null) {
            file.putString("pref", pref);
        }
        for (int iter = 0; iter < 2; ++iter) {
            String prefix = "";
            if (iter > 0) {
                prefix = "t" + Integer.toString(iter) + "_";
            }
            if (this.max_entry[iter] > 1) {
                if (this.last_index[iter] > 0) {
                    file.putInt(String.valueOf(prefix) + "index", this.last_index[iter]);
                }
                file.putInt(String.valueOf(prefix) + "max_entry", this.max_entry[iter]);
            }
            for (int i = 0; i < this.max_entry[iter]; ++i) {
                if (this.hasDataEntry(i, iter)) {
                    final String suffix = (i > 0) ? Integer.toString(i) : "";
                    final DataEntry entry = this.getDataEntry(i, iter);
                    saveDataEntry(file, entry, prefix, suffix, iter);
                }
            }
        }
        file.dispose();
    }
    
    public static void saveDataEntry(final FileIO file, final DataEntry entry, final String prefix, final String suffix, final int type) {
        if (entry.getName() != null) {
            file.putString(String.valueOf(prefix) + "name" + suffix, entry.getName());
        }
        file.putString(String.valueOf(prefix) + "sex" + suffix, entry.getSex() ? "male" : "female");
        if (type == 1) {
            file.putString(String.valueOf(prefix) + "dayset" + suffix, entry.getChoice() ? "day_choice" : "night_choice");
            file.putString(String.valueOf(prefix) + "degree" + suffix, entry.getMountainPos());
        }
        else {
            final int[] date = entry.getNowDay();
            if (date != null && !BaseCalendar.withinDateRange(date, 7)) {
                file.putIntArray(String.valueOf(prefix) + "now" + suffix, date);
            }
        }
        file.putIntArray(String.valueOf(prefix) + "date" + suffix, entry.getBirthDay());
        file.putString(String.valueOf(prefix) + "country" + suffix, entry.getCountry());
        file.putString(String.valueOf(prefix) + "city" + suffix, entry.getCity());
        file.putString(String.valueOf(prefix) + "zone" + suffix, entry.getZone());
        if (entry.getOverride() != null) {
            file.putString(String.valueOf(prefix) + "override" + suffix, entry.getOverride());
        }
        final String note = entry.getNote(true);
        if (note != null) {
            file.putString(String.valueOf(prefix) + "note" + suffix, mapString(note));
        }
    }
}
