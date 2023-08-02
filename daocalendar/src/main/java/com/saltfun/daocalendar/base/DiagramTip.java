package com.saltfun.daocalendar.base;

import java.awt.*;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

public class DiagramTip
{
    private static final double SCALER = 1000000.0;
    private static final int TWO_PI_SCALED = 6283185;
    private static final double TWO_PI_MINUS = 6.283184307179586;
    private final double RADIAN = 57.29577951308232;
    private final int INIT_TIP_COUNT = 3;
    private final int DEGREE_MARKER = -1000;
    private final int INVALID_INT = Integer.MIN_VALUE;
    private final double INVALID_DOUBLE = Double.MIN_VALUE;
    private int center_x;
    private int center_y;
    private int radius;
    private int init_tip_count;
    private double scaler;
    private boolean enabled;
    private boolean need_update;
    private Entry last_entry;
    private Entry next_tip;
    private LinkedList region;
    private LinkedList planet;
    
    public DiagramTip() {
        this.init_tip_count = 0;
        this.enabled = true;
        this.need_update = true;
        this.next_tip = new Entry();
    }
    
    public void reset() {
        this.region = new LinkedList();
        this.planet = new LinkedList();
        this.clearNextTip();
        final boolean b = true;
        this.need_update = b;
        this.enabled = b;
    }
    
    public void init(final int[] size, final boolean enable) {
        if (!(this.enabled = enable)) {
            return;
        }
        this.center_x = size[0];
        this.center_y = size[1];
        this.radius = size[2];
        this.scaler = 1.0;
        this.region = new LinkedList();
        this.clearNextTip();
    }
    
    public void setTipScale(final int[] data) {
        this.center_x = data[0];
        this.center_y = data[1];
        this.scaler = this.radius / (double)data[2];
    }
    
    public void resetInitCount() {
        this.init_tip_count = 0;
    }
    
    public boolean getNeedUpdate() {
        return this.need_update;
    }
    
    public void setNeedUpdate(final boolean set) {
        this.need_update = set;
    }
    
    public String formatTip(String tip, final String indent, final int max_len) {
        if (tip.length() <= max_len) {
            return tip;
        }
        final String break_char = Resource.getString("break_char");
        final StringTokenizer st = new StringTokenizer(tip, break_char);
        tip = "";
        int cur_len = 0;
        while (st.hasMoreTokens()) {
            final String str = st.nextToken().trim();
            final int len = str.length();
            if (len == 0) {
                continue;
            }
            if (cur_len > 0 && cur_len + len + 2 > max_len) {
                tip = String.valueOf(tip) + "\n" + indent + str;
                cur_len = indent.length() + len;
            }
            else {
                if (!tip.equals("")) {
                    tip = String.valueOf(tip) + break_char.substring(0, 1) + " ";
                }
                tip = String.valueOf(tip) + str;
                cur_len += len + 2;
            }
        }
        return tip;
    }
    
    public void addTip(final int planet_no, final boolean birth_data, final String tip) {
        if (!this.enabled) {
            return;
        }
        Entry entry = this.getTipEntry(planet_no, birth_data);
        if (entry == null) {
            entry = new Entry();
            entry.planet_no = planet_no;
            entry.birth_data = birth_data;
            this.planet.add(entry);
            this.checkAddNextTip(entry);
        }
        entry.tip = tip;
    }
    
    public String getTip(final int planet_no, final boolean birth_data) {
        final Entry entry = this.getTipEntry(planet_no, birth_data);
        return (entry != null) ? entry.tip : null;
    }
    
    public void addTip(final double lower_radius, final double upper_radius, final double lower_radian, final double upper_radian, final int planet_no, final boolean birth_data) {
        if (!this.enabled) {
            return;
        }
        final Entry entry = this.getTipEntry(planet_no, birth_data);
        if (entry != null) {
            this.addTip(lower_radius, upper_radius, lower_radian, upper_radian, birth_data, planet_no + 1, entry.tip);
        }
    }
    
    public void addTip(final double lower_radius, final double upper_radius, double lower_radian, double upper_radian, final boolean birth_data, final int planet_no, final String tip) {
        if (!this.enabled) {
            return;
        }
        Entry entry = new Entry();
        entry.l_radius2 = lower_radius * lower_radius;
        entry.u_radius2 = upper_radius * upper_radius;
        lower_radian = this.boundRadian(lower_radian);
        upper_radian = this.boundRadian(upper_radian);
        if (upper_radian < lower_radian) {
            final double bound = 6.283185307179586;
            if (upper_radian < bound) {
                entry.l_radian = lower_radian;
                entry.u_radian = bound;
                entry.birth_data = birth_data;
                entry.planet_no = planet_no;
                entry.tip = tip;
                this.checkAddNextTip(entry);
                this.region.addFirst(entry);
                entry = new Entry();
                entry.l_radius2 = lower_radius * lower_radius;
                entry.u_radius2 = upper_radius * upper_radius;
            }
            lower_radian = 0.0;
        }
        if (lower_radian < upper_radian) {
            entry.l_radian = lower_radian;
            entry.u_radian = upper_radian;
            entry.birth_data = birth_data;
            entry.planet_no = planet_no;
            entry.tip = tip;
            this.checkAddNextTip(entry);
            this.region.addFirst(entry);
        }
    }
    
    public void addDataToNextTip(final int val, final String init_tip) {
        if (this.next_tip == null) {
            this.next_tip = new Entry();
        }
        this.next_tip.int_data = val;
        this.next_tip.double_data = Double.MIN_VALUE;
        this.next_tip.init_tip = init_tip;
    }
    
    public void addDataToNextTip(final double val, final String init_tip) {
        if (this.next_tip == null) {
            this.next_tip = new Entry();
        }
        this.next_tip.double_data = val;
        this.next_tip.int_data = Integer.MIN_VALUE;
        this.next_tip.init_tip = init_tip;
    }
    
    public void clearNextTip() {
        this.next_tip.int_data = Integer.MIN_VALUE;
        this.next_tip.double_data = Double.MIN_VALUE;
        this.next_tip.init_tip = null;
    }
    
    private void checkAddNextTip(final Entry entry) {
        entry.int_data = this.next_tip.int_data;
        entry.double_data = this.next_tip.double_data;
        entry.init_tip = this.next_tip.init_tip;
    }
    
    public void addDegreeMarkerToNextTip(final String init_tip) {
        this.addDataToNextTip(-1000, init_tip);
    }
    
    public void addTip(final double lower_radius, final double upper_radius, final boolean first, final String tip) {
        if (!this.enabled) {
            return;
        }
        final Entry entry = new Entry();
        entry.l_radius2 = lower_radius * lower_radius;
        entry.u_radius2 = upper_radius * upper_radius;
        entry.l_radian = 0.0;
        entry.u_radian = 6.283184307179586;
        entry.tip = tip;
        this.checkAddNextTip(entry);
        if (first) {
            this.region.addFirst(entry);
        }
        else {
            this.region.addLast(entry);
        }
    }
    
    public void addTip(final int lx, final int ly, final int width, final int height, final boolean first, final String tip) {
        if (!this.enabled) {
            return;
        }
        final Entry entry = new Entry();
        entry.rectangle = true;
        entry.l_radian = lx;
        entry.u_radian = ly;
        entry.l_radius2 = lx + width;
        entry.u_radius2 = ly + height;
        entry.tip = tip;
        this.checkAddNextTip(entry);
        if (first) {
            this.region.addFirst(entry);
        }
        else {
            this.region.addLast(entry);
        }
    }
    
    public int getIntFromLastPoint() {
        return (this.last_entry != null) ? this.last_entry.int_data : Integer.MIN_VALUE;
    }
    
    public double getDoubleFromLastPoint() {
        return (this.last_entry != null) ? this.last_entry.double_data : Double.MIN_VALUE;
    }
    
    public int getIntFromPoint(final int x, final int y) {
        if (this.hasDataFromPoint(x, y)) {
            return this.getIntFromLastPoint();
        }
        return Integer.MIN_VALUE;
    }
    
    public double getDoubleFromPoint(final int x, final int y) {
        if (this.hasDataFromPoint(x, y)) {
            return this.getDoubleFromLastPoint();
        }
        return Double.MIN_VALUE;
    }
    
    public boolean hasDataFromPoint(final int x, final int y) {
        final double d_x = this.scaler * (x - this.center_x);
        final double d_y = this.scaler * (y - this.center_y);
        final double radius2 = d_x * d_x + d_y * d_y;
        double radian = Math.atan2(-d_y, d_x);
        radian = this.boundRadian(radian);
        this.last_entry = null;
        final ListIterator iter = this.region.listIterator();
        while (iter.hasNext()) {
            final Entry entry = (Entry) iter.next();
            if (entry.rectangle) {
                continue;
            }
            if (radius2 >= entry.l_radius2 && radius2 < entry.u_radius2 && radian >= entry.l_radian && radian < entry.u_radian && (entry.int_data != Integer.MIN_VALUE || entry.double_data != Double.MIN_VALUE)) {
                this.last_entry = entry;
                return true;
            }
        }
        return false;
    }
    
    public double getDegreeFromPoint(final int x, final int y, final boolean radius_check) {
        final double d_x = this.scaler * (x - this.center_x);
        final double d_y = this.scaler * (y - this.center_y);
        final double radius2 = d_x * d_x + d_y * d_y;
        double radian = Math.atan2(-d_y, d_x);
        radian = this.boundRadian(radian);
        if (radius_check) {
            return (this.last_entry != null && radius2 >= this.last_entry.l_radius2 && radius2 < this.last_entry.u_radius2) ? (radian * 57.29577951308232) : Double.MIN_VALUE;
        }
        final ListIterator iter = this.region.listIterator();
        while (iter.hasNext()) {
            final Entry entry = (Entry) iter.next();
            if (entry.rectangle) {
                continue;
            }
            if (radius2 >= entry.l_radius2 && radius2 < entry.u_radius2 && radian >= entry.l_radian && radian < entry.u_radian) {
                return (entry.int_data == -1000) ? (radian * 57.29577951308232) : Double.MIN_VALUE;
            }
        }
        return Double.MIN_VALUE;
    }
    
    public int getPlanetFromPoint(final int x, final int y) {
        final double d_x = this.scaler * (x - this.center_x);
        final double d_y = this.scaler * (y - this.center_y);
        final double radius2 = d_x * d_x + d_y * d_y;
        double radian = Math.atan2(-d_y, d_x);
        radian = this.boundRadian(radian);
        final ListIterator iter = this.region.listIterator();
        while (iter.hasNext()) {
            final Entry entry = (Entry) iter.next();
            if (entry.rectangle) {
                continue;
            }
            if (radius2 >= entry.l_radius2 && radius2 < entry.u_radius2 && radian >= entry.l_radian && radian < entry.u_radian && entry.planet_no > 0) {
                this.last_entry = entry;
                return entry.planet_no - 1;
            }
        }
        this.last_entry = null;
        return -1;
    }
    
    public boolean isBirthPlanet() {
        return this.last_entry != null && this.last_entry.birth_data;
    }
    
    public Point getCenterPoint() {
        if (this.last_entry == null || this.last_entry.rectangle) {
            return null;
        }
        final double radian = 0.5 * (this.last_entry.l_radian + this.last_entry.u_radian);
        final double rad = 0.5 * (Math.sqrt(this.last_entry.l_radius2) + Math.sqrt(this.last_entry.u_radius2)) / this.scaler;
        return new Point((int)(this.center_x + rad * Math.cos(radian)), (int)(this.center_y + rad * Math.sin(-radian)));
    }
    
    public boolean isIntValid(final int val) {
        return val != Integer.MIN_VALUE;
    }
    
    public boolean isDoubleValid(final double val) {
        return val != Double.MIN_VALUE;
    }
    
    public String getTipFromPoint(final int x, final int y) {
        final double d_x = this.scaler * (x - this.center_x);
        final double d_y = this.scaler * (y - this.center_y);
        final double radius2 = d_x * d_x + d_y * d_y;
        double radian = Math.atan2(-d_y, d_x);
        radian = this.boundRadian(radian);
        final ListIterator iter = this.region.listIterator();
        while (iter.hasNext()) {
            final Entry entry = (Entry) iter.next();
            if (entry.rectangle) {
                if (d_x < entry.l_radian || d_y < entry.u_radian || d_x > entry.l_radius2 || d_y > entry.u_radius2) {
                    continue;
                }
                if (entry.init_tip != null && (entry.int_data != Integer.MIN_VALUE || entry.double_data != Double.MIN_VALUE) && entry.int_data != -1000 && ++this.init_tip_count <= 3) {
                    return String.valueOf(entry.tip) + "\n\n" + entry.init_tip;
                }
                return entry.tip;
            }
            else {
                if (radius2 < entry.l_radius2 || radius2 >= entry.u_radius2 || radian < entry.l_radian || radian >= entry.u_radian) {
                    continue;
                }
                if (entry.init_tip != null && (entry.int_data != Integer.MIN_VALUE || entry.double_data != Double.MIN_VALUE) && entry.int_data != -1000 && ++this.init_tip_count <= 3) {
                    return String.valueOf(entry.tip) + "\n\n" + entry.init_tip;
                }
                return entry.tip;
            }
        }
        return null;
    }
    
    private Entry getTipEntry(final int planet_no, final boolean birth_data) {
        final ListIterator iter = this.planet.listIterator();
        while (iter.hasNext()) {
            final Entry entry = (Entry) iter.next();
            if (planet_no == entry.planet_no && birth_data == entry.birth_data) {
                return entry;
            }
        }
        return null;
    }
    
    private double boundRadian(final double radian) {
        int val;
        for (val = (int)(1000000.0 * radian); val >= 6283185; val -= 6283185) {}
        while (val < 0) {
            val += 6283185;
        }
        return val / 1000000.0;
    }
    
    private class Entry
    {
        int planet_no;
        boolean birth_data;
        boolean rectangle;
        double l_radius2;
        double u_radius2;
        double l_radian;
        double u_radian;
        int int_data;
        double double_data;
        String init_tip;
        String tip;
        
        public Entry() {
            this.int_data = Integer.MIN_VALUE;
            this.double_data = Double.MIN_VALUE;
        }
    }
}
