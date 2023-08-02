package com.saltfun.daocalendar.base;

import java.util.Arrays;

public class DataEntry
{
    private static final int SHORT_DESC_MIN_LENGTH = 10;
    private static int now_field_diff;
    private final int SHORT_NOTE_LINE = 5;
    private final int LINE_WRAP_LENGTH = 30;
    private final int SHORT_NOTE_LENGTH = 150;
    private boolean selected;
    private boolean sex;
    private boolean choice;
    private String name;
    private String country;
    private String city;
    private String zone;
    private String mountain_pos;
    private String override;
    private String note;
    private int[] birth_day;
    private int[] now_day;
    
    public DataEntry() {
        this.sex = true;
    }
    
    public String getName() {
        return (this.name == null) ? "" : this.name;
    }
    
    public void setName(final String str) {
        this.name = str;
    }
    
    public boolean getSex() {
        return this.sex;
    }
    
    public void setSex(final boolean set) {
        this.sex = set;
    }
    
    public boolean getChoice() {
        return this.choice;
    }
    
    public void setChoice(final boolean set) {
        this.choice = set;
    }
    
    public String getMountainPos() {
        return this.mountain_pos;
    }
    
    public void setMountainPos(final String val) {
        this.mountain_pos = val;
    }
    
    public boolean getSelected() {
        return this.selected;
    }
    
    public void setSelected(final boolean yes) {
        this.selected = yes;
    }
    
    public String getCountry() {
        return this.country;
    }
    
    public void setCountry(final String str) {
        this.country = str;
    }
    
    public String getCity() {
        return this.city;
    }
    
    public void setCity(final String str) {
        this.city = str;
    }
    
    public String getZone() {
        return this.zone;
    }
    
    public void setZone(final String str) {
        this.zone = str;
    }
    
    public String getOverride() {
        return this.override;
    }
    
    public void setOverride(final String str) {
        this.override = str;
    }
    
    public String getNote(final boolean full) {
        if (full || this.note == null) {
            return this.note;
        }
        String str = this.note.replaceAll("\t", "    ");
        if (str.length() > 150) {
            str = str.substring(0, 147);
        }
        String f_str = "";
        int count = 0;
        for (int i = 0; i < 5; ++i) {
            int index = str.indexOf("\r");
            if (index < 0) {
                index = str.indexOf("\n");
            }
            if (index < 0) {
                if (str.trim().equals("")) {
                    ++count;
                    continue;
                }
                index = str.length();
            }
            boolean wrap = false;
            if (index > 35) {
                index = 30;
                wrap = true;
            }
            ++count;
            if (!f_str.equals("")) {
                f_str = String.valueOf(f_str) + "\r";
            }
            f_str = String.valueOf(f_str) + str.substring(0, index);
            str = str.substring(index);
            if (!wrap) {
                if (str.startsWith("\r")) {
                    str = str.substring(1);
                }
                if (str.startsWith("\n")) {
                    str = str.substring(1);
                }
            }
        }
        if (f_str.equals("")) {
            f_str = str;
        }
        if (count > 1) {
            f_str = String.valueOf(f_str) + "...";
        }
        return f_str;
    }
    
    public void setNote(final String str) {
        if (str != null && str.trim().equals("")) {
            this.note = null;
        }
        else {
            this.note = str;
        }
    }
    
    public int[] getBirthDay() {
        return (int[])((this.birth_day != null) ? ((int[])this.birth_day.clone()) : null);
    }
    
    public int[] getBirthDayDirect() {
        return this.birth_day;
    }
    
    public void setBirthDay(final int[] date) {
        this.birth_day = (int[])((date != null) ? ((int[])date.clone()) : null);
    }
    
    public int[] getNowDay() {
        return (int[])((this.now_day != null) ? ((int[])this.now_day.clone()) : null);
    }
    
    public void setNowDay(final int[] date) {
        this.now_day = (int[])((date != null) ? ((int[])date.clone()) : null);
    }
    
    public boolean isValid() {
        return this.birth_day != null && this.country != null && this.city != null && this.zone != null;
    }
    
    public String packEntry(final int type) {
        final String[] map = DataSet.getMapString();
        DataSet.setMapString(null);
        final FileIO string_io = new FileIO(null, false, true);
        DataSet.saveDataEntry(string_io, this, "", "", type);
        final String data = string_io.getDataInString();
        string_io.dispose();
        DataSet.setMapString(map);
        return data;
    }
    
    public boolean unpackEntry(final String data, final int type) {
        final String[] map = DataSet.getMapString();
        DataSet.setMapString(null);
        final FileIO string_io = new FileIO(data, true);
        final boolean success = DataSet.loadDataEntry(string_io, this, "", "", type);
        string_io.dispose();
        DataSet.setMapString(map);
        return success;
    }
    
    public boolean equals(final DataEntry entry, final boolean basic) {
        DataEntry.now_field_diff = -1;
        if (this.sex != entry.sex || !sameString(this.name, entry.name) || !this.samePlace(entry) || !sameDate(this.birth_day, entry.birth_day)) {
            return false;
        }
        if (basic) {
            return true;
        }
        if (this.choice != entry.choice || !sameString(this.mountain_pos, entry.mountain_pos) || !sameString(this.override, entry.override) || !sameString(this.note, entry.note)) {
            return false;
        }
        DataEntry.now_field_diff = (sameDate(this.now_day, entry.now_day) ? 0 : 1);
        return DataEntry.now_field_diff == 0;
    }
    
    public static boolean nowFieldDifferOnly() {
        return DataEntry.now_field_diff == 1;
    }
    
    public boolean samePlace(final DataEntry entry) {
        return sameString(this.country, entry.country) && sameString(this.city, entry.city) && sameString(this.zone, entry.zone);
    }
    
    public boolean sameNote(String str) {
        if (str != null && str.trim().equals("")) {
            str = null;
        }
        return sameString(this.note, str);
    }
    
    public static boolean sameString(final String a, final String b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.equals(b);
    }
    
    public static boolean sameDate(final int[] a, final int[] b) {
        if (a == null || b == null) {
            return a == b;
        }
        return Arrays.equals(a, b);
    }
    
    public static String getOneLineDesc(final String str, final int width, final boolean always) {
        if (str == null) {
            return null;
        }
        int r_index = str.indexOf("\r");
        if (r_index < 0) {
            r_index = str.indexOf("\n");
        }
        if (r_index < 0) {
            r_index = Integer.MAX_VALUE;
        }
        if (width > 10) {
            r_index = Math.min(r_index, width - 3);
        }
        if (r_index >= str.trim().length()) {
            return str;
        }
        if (!always && r_index <= width) {
            return str.substring(0, r_index).trim();
        }
        return String.valueOf(str.substring(0, r_index)) + "...";
    }
}
