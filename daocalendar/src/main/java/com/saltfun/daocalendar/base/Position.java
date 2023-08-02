package com.saltfun.daocalendar.base;

import java.util.*;

public class Position
{
    private final double TWO_PI = 6.283185307179586;
    private final double HALF_PI = 1.5707963267948966;
    private final double QUARTER_PI = 0.7853981633974483;
    private final double EPSILON = 1.0E-5;
    private final double BEST_ANGLE = 1.5707963267948966;
    private final double SECOND_BEST_ANGLE = 4.71238898038469;
    private static double lx;
    private static double ly;
    private static double ux;
    private static double uy;
    private static double gap;
    private static double font_height;
    private static Angle[] prefer_angle;
    private int index;
    private int state;
    private String name;
    private boolean locked;
    private double pos;
    private double shift;
    private double pos_x;
    private double pos_y;
    private double radius;
    private double h_radian;
    private LinkedList block_list;
    
    public Position(final String str, final int ind, final double val, final int s_state, final boolean lock) {
        this.name = str;
        this.index = ind;
        this.pos = val;
        this.shift = 0.0;
        this.state = s_state;
        this.locked = lock;
    }
    
    public Position(final int ind, final int x, final int y, final int rad, final double h_rad) {
        this.index = ind;
        this.pos_x = x;
        this.pos_y = y;
        this.radius = rad;
        this.h_radian = h_rad;
        this.pos = Double.MIN_VALUE;
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public double getLocation() {
        return this.pos;
    }
    
    public double getZodiacDegree() {
        return this.pos % 30.0;
    }
    
    public int getState() {
        return this.state;
    }
    
    public boolean getLocked() {
        return this.locked;
    }
    
    public double getShift() {
        return this.shift;
    }
    
    public double getShiftedLocation() {
        if (this.pos == Double.NEGATIVE_INFINITY) {
            return Double.NEGATIVE_INFINITY;
        }
        double degree;
        for (degree = this.pos + this.shift; degree < 0.0; degree += 360.0) {}
        while (degree >= 360.0) {
            degree -= 360.0;
        }
        return degree;
    }
    
    public void setShift(final double val) {
        this.shift = val;
    }
    
    public void addShift(final double val) {
        this.shift += val;
    }
    
    public double getRadius() {
        return this.radius;
    }
    
    public double getHalfAngle() {
        return this.h_radian;
    }
    
    public void setAngle(final double angle) {
        this.pos = angle;
    }
    
    public double getAngle() {
        return this.pos;
    }
    
    public double getX() {
        return this.pos_x;
    }
    
    public double getY() {
        return this.pos_y;
    }
    
    public double getExtendedRadius() {
        return this.radius + Position.gap;
    }
    
    public double getRange() {
        return this.radius + Position.gap + Position.font_height;
    }
    
    public void reset() {
        this.shift = 0.0;
        this.locked = false;
        this.block_list = null;
    }
    
    public void blockBoundary(final double zoom) {
        final double range = this.getRange();
        double d = this.pos_x - zoom * Position.lx;
        if (d < range) {
            final double angle = Math.acos(d / range) + this.h_radian;
            this.blockAngle(3.141592653589793, angle, null);
        }
        d = zoom * Position.ux - this.pos_x;
        if (d < range) {
            final double angle = Math.acos(d / range) + this.h_radian;
            this.blockAngle(0.0, angle, null);
        }
        d = zoom * Position.uy - this.pos_y;
        if (d < range) {
            final double angle = Math.acos(d / range) + this.h_radian;
            this.blockAngle(1.5707963267948966, angle, null);
        }
        d = this.pos_y - zoom * Position.ly;
        if (d < range) {
            final double angle = Math.acos(d / range) + this.h_radian;
            this.blockAngle(4.71238898038469, angle, null);
        }
    }
    
    private void blockAngle(final double orientation, final double half_angle, final Position p) {
        if (this.block_list == null) {
            this.block_list = new LinkedList();
        }
        double lower = orientation - half_angle;
        double upper = orientation + half_angle;
        if (lower < 0.0) {
            final Range range = new Range(lower + 6.283185307179586, 6.283195307179586, orientation, p);
            this.block_list.add(range);
            lower = -1.0E-5;
        }
        if (upper >= 6.283185307179586) {
            final Range range = new Range(-1.0E-5, upper - 6.283185307179586, orientation, p);
            this.block_list.add(range);
            upper = 6.283195307179586;
        }
        final Range range = new Range(lower, upper, orientation, p);
        this.block_list.add(range);
    }
    
    public double distance(final Position p) {
        final double dx = p.pos_x - this.pos_x;
        final double dy = p.pos_y - this.pos_y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public boolean contain(final Position p) {
        return this.distance(p) + p.getRadius() <= this.getExtendedRadius();
    }
    
    public void block(final Position p, final double rad) {
        final double dx = p.pos_x - this.pos_x;
        final double dy = p.pos_y - this.pos_y;
        final double d = Math.sqrt(dx * dx + dy * dy);
        final double range = this.getRange();
        if (rad + range <= d) {
            return;
        }
        double orientation = Math.atan2(dy, dx);
        if (orientation < 0.0) {
            orientation += 6.283185307179586;
        }
        double angle;
        if (d + rad <= range) {
            angle = ((rad >= d) ? 1.5707863267948965 : (Math.asin(rad / d) + this.h_radian));
        }
        else {
            angle = Math.acos((d * d + range * range - rad * rad) / (2.0 * d * range)) + this.h_radian;
        }
        this.blockAngle(orientation, angle, p);
    }
    
    public void blockContain(final Position p) {
        final double dx = p.pos_x - this.pos_x;
        final double dy = p.pos_y - this.pos_y;
        double orientation = Math.atan2(dy, dx);
        if (orientation < 0.0) {
            orientation += 6.283185307179586;
        }
        this.blockAngle(orientation, 0.39269908169872414, p);
    }
    
    public boolean contain(final Position p, final double degree, final double scaler) {
        double dx = Math.abs(p.pos_x - this.pos_x) / scaler;
        if (dx > 180.0) {
            dx = 360.0 - dx;
        }
        final double dy = (p.pos_y - this.pos_y) / scaler;
        final double d = Math.sqrt(dx * dx + dy * dy);
        return d <= degree;
    }
    
    public double getValidAngle() {
        if (this.block_list == null) {
            return 1.5707963267948966;
        }
        if (Position.prefer_angle == null) {
            Position.prefer_angle = new Angle[8];
            for (int i = 0; i < Position.prefer_angle.length; ++i) {
                Position.prefer_angle[i] = new Angle(i * 0.7853981633974483);
            }
        }
        this.sortBlockList();
        final double orientation = this.getPreferOrientation();
        for (int j = 0; j < Position.prefer_angle.length; ++j) {
            Position.prefer_angle[j].setOrientation(orientation);
        }
        Arrays.sort(Position.prefer_angle, 0, Position.prefer_angle.length, new Comparator() {
            @Override
            public int compare(final Object a, final Object b) {
                final double d_a = ((Angle)a).delta;
                final double d_b = ((Angle)b).delta;
                if (d_a < d_b) {
                    return -1;
                }
                if (d_a > d_b) {
                    return 1;
                }
                return 0;
            }
        });
        for (int j = 0; j < Position.prefer_angle.length; ++j) {
            if (this.isVacant(Position.prefer_angle[j].angle)) {
                return Position.prefer_angle[j].angle;
            }
        }
        double val = 0.0;
        final ListIterator iter = this.block_list.listIterator();
        while (iter.hasNext()) {
            final Range range = (Range) iter.next();
            if (range.lower >= val) {
                return val;
            }
            if (range.upper <= val) {
                continue;
            }
            val = range.upper;
        }
        return Double.MIN_VALUE;
    }
    
    private boolean isVacant(final double val) {
        final ListIterator iter = this.block_list.listIterator();
        while (iter.hasNext()) {
            final Range range = (Range) iter.next();
            if (range.lower >= val) {
                return true;
            }
            if (range.upper > val) {
                return false;
            }
        }
        return true;
    }
    
    public double getValidGap() {
        if (this.block_list == null) {
            return 6.283185307179586;
        }
        this.sortBlockList();
        double val = 0.0;
        double valid_gap = 0.0;
        final ListIterator iter = this.block_list.listIterator();
        while (iter.hasNext()) {
            final Range range = (Range) iter.next();
            if (range.lower > val) {
                valid_gap += range.lower - val;
                val = range.upper;
            }
            else {
                if (range.upper <= val) {
                    continue;
                }
                val = range.upper;
            }
        }
        if (val < 6.283185307179586) {
            valid_gap += 6.283185307179586 - val;
        }
        return valid_gap;
    }
    
    private double getPreferOrientation() {
        double val = 0.0;
        double first_width = -1.0;
        double best_width = -1.0;
        double best_pos = 1.5707963267948966;
        boolean has_second_best = false;
        final ListIterator iter = this.block_list.listIterator();
        while (iter.hasNext()) {
            final Range range = (Range) iter.next();
            if (range.lower >= val) {
                final double width = range.lower - val;
                if (width > best_width) {
                    best_width = width;
                    if (this.cover(val, val + width, 1.5707963267948966)) {
                        return 1.5707963267948966;
                    }
                    if (this.cover(val, val + width, 4.71238898038469)) {
                        has_second_best = true;
                    }
                    else {
                        best_pos = val + 0.5 * width;
                    }
                }
                if (val == 0.0) {
                    first_width = width;
                }
                val = range.upper;
            }
            else {
                if (range.upper <= val) {
                    continue;
                }
                val = range.upper;
            }
        }
        double width = 6.283185307179586 - val;
        if (width >= 0.0) {
            if (first_width >= 0.0) {
                width += first_width;
            }
            if (width > best_width) {
                best_width = width;
                if (this.cover(val, val + width, 1.5707963267948966)) {
                    return 1.5707963267948966;
                }
                if (this.cover(val, val + width, 4.71238898038469)) {
                    has_second_best = true;
                }
                else {
                    best_pos = val + 0.5 * width;
                }
                if (best_pos >= 6.283185307179586) {
                    best_pos -= 6.283185307179586;
                }
            }
        }
        return has_second_best ? 4.71238898038469 : best_pos;
    }
    
    private boolean cover(final double lower, final double upper, final double target) {
        return lower + 0.7853981633974483 <= target && upper - 0.7853981633974483 >= target;
    }
    
    private void sortBlockList() {
        if (this.block_list.size() == 1) {
            return;
        }
        Collections.sort((List<Object>)this.block_list, new Comparator() {
            @Override
            public int compare(final Object a, final Object b) {
                final Range p_a = (Range)a;
                final Range p_b = (Range)b;
                if (p_a.lower < p_b.lower) {
                    return -1;
                }
                if (p_a.lower > p_b.lower) {
                    return 1;
                }
                return 0;
            }
        });
    }
    
    public static Position getPosition(final Position[] position, final int length, int index) {
        while (index < length) {
            index += length;
        }
        while (index >= length) {
            index -= length;
        }
        return position[index];
    }
    
    public static int getPositionIndex(final Position[] position, final int length, final int index) {
        for (int i = 0; i < length; ++i) {
            if (position[i].index == index) {
                return i;
            }
        }
        return -1;
    }
    
    public static void setBound(final int t_lx, final int t_ly, final int t_ux, final int t_uy) {
        Position.lx = t_lx;
        Position.ly = t_ly;
        Position.ux = t_ux;
        Position.uy = t_uy;
    }
    
    public static void setGapFontHeight(final int t_gap, final int t_height) {
        Position.gap = t_gap;
        Position.font_height = t_height;
    }
    
    public static int getGap() {
        return (int)Position.gap;
    }
    
    public static int getFontHeight() {
        return (int)Position.font_height;
    }
    
    private class Range
    {
        private double lower;
        private double upper;
        
        public Range(final double l_val, final double u_val, final double orient, final Position p) {
            this.lower = l_val;
            this.upper = u_val;
        }
    }
    
    private class Angle
    {
        private double angle;
        private double delta;
        
        public Angle(final double t_angle) {
            this.angle = t_angle;
        }
        
        public void setOrientation(final double orient) {
            this.delta = Math.abs(this.angle - orient);
            if (this.delta > 3.141592653589793) {
                this.delta = 6.283185307179586 - this.delta;
            }
        }
    }
}
