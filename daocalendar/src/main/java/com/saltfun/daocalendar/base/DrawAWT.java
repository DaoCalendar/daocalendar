package com.saltfun.daocalendar.base;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class DrawAWT
{
    private final int POLYGON_PRECISION = 10;
    public static final int LINE = 0;
    public static final int DASH = 1;
    public static final int SPARSE_DASH = 2;
    public static final int DENSE_DASH = 3;
    public static final int DOT_DASH = 4;
    public static final int DOT = 5;
    private static Graphics2D g2d;
    private static Hashtable font_table;
    static final float[] dash;
    static final float[] sparse_dash;
    static final float[] dense_dash;
    static final float[] dot_dash;
    static final float[] dot;
    static final BasicStroke dashed;
    static final BasicStroke sparse_dashed;
    static final BasicStroke dense_dashed;
    static final BasicStroke dot_dashed;
    static final BasicStroke dotted;
    private int fg_color_org;
    private int bg_color_org;
    private int fg_color;
    private int bg_color;
    private int font_scaler;
    private String prefix;
    private String[] color_key;
    private int[] color_key_color;
    private Color color_save;
    private FontMetrics cur_metric;
    private int font_size_override;
    private int cur_radius;
    private boolean vertical;
    private double init_angle;
    private Stroke stroke_save;
    private AffineTransform mark_tran;
    
    static {
        DrawAWT.g2d = null;
        DrawAWT.font_table = new Hashtable();
        dash = new float[] { 6.0f, 6.0f };
        sparse_dash = new float[] { 5.0f, 10.0f };
        dense_dash = new float[] { 10.0f, 5.0f };
        dot_dash = new float[] { 21.0f, 9.0f, 3.0f, 9.0f };
        dot = new float[] { 3.0f, 3.0f };
        dashed = new BasicStroke(1.0f, 0, 0, 10.0f, DrawAWT.dash, 0.0f);
        sparse_dashed = new BasicStroke(1.0f, 0, 0, 10.0f, DrawAWT.sparse_dash, 0.0f);
        dense_dashed = new BasicStroke(1.0f, 0, 0, 10.0f, DrawAWT.dense_dash, 0.0f);
        dot_dashed = new BasicStroke(1.0f, 0, 0, 10.0f, DrawAWT.dot_dash, 0.0f);
        dotted = new BasicStroke(1.0f, 0, 0, 10.0f, DrawAWT.dot, 0.0f);
    }
    
    public DrawAWT() {
        this.font_scaler = 1;
    }
    
    private void initColor() {
        final int n = 0;
        this.fg_color_org = n;
        this.fg_color = n;
        final int n2 = 16777215;
        this.bg_color_org = n2;
        this.bg_color = n2;
        this.mark_tran = DrawAWT.g2d.getTransform();
    }
    
    public void translate(final int x, final int y) {
        DrawAWT.g2d.translate(x, y);
    }
    
    public void rotate(final double angle) {
        DrawAWT.g2d.rotate(angle);
    }
    
    public AffineTransform getTransform() {
        return DrawAWT.g2d.getTransform();
    }
    
    public void setTransform(final AffineTransform tran) {
        DrawAWT.g2d.setTransform(tran);
    }
    
    public double getAngle() {
        final double[] matrix = new double[6];
        DrawAWT.g2d.getTransform().getMatrix(matrix);
        return Math.atan2(matrix[2], matrix[3]);
    }
    
    public void setColor(final int color) {
        if (color >= 0) {
            DrawAWT.g2d.setColor(new Color(color));
        }
        else {
            this.setColor();
        }
    }
    
    public void setColor() {
        if (this.fg_color < 0) {
            DrawAWT.g2d.setColor(Color.BLACK);
        }
        else {
            this.setColor(this.fg_color);
        }
    }
    
    public void pushSetColor(final int color) {
        this.color_save = DrawAWT.g2d.getColor();
        this.setColor(color);
    }
    
    public void popSetColor() {
        DrawAWT.g2d.setColor(this.color_save);
    }
    
    public int getColor(final String key, final boolean use_bw) {
        if (use_bw) {
            return this.fg_color;
        }
        final int color = Resource.getPrefInt(key);
        return (color >= 0) ? color : this.fg_color;
    }
    
    public int getBgColor(final String key, final boolean use_bw) {
        if (use_bw) {
            return this.bg_color;
        }
        final int color = Resource.getPrefInt(key);
        return (color >= 0) ? color : this.bg_color;
    }
    
    public static void setFillColor(final Graphics2D g, final String key, final boolean use_bw) {
        if (use_bw) {
            return;
        }
        final int color = Resource.getPrefInt(key);
        if (color >= 0) {
            g.setColor(new Color(color));
        }
    }
    
    public void setForeground(final int color) {
        this.fg_color = color;
    }
    
    public void setForeground() {
        this.fg_color = this.fg_color_org;
    }
    
    public void setBackground(final int color) {
        this.bg_color = color;
    }
    
    public void setBackground() {
        this.bg_color = this.bg_color_org;
    }
    
    public void setRingColor(final int ring, final int[] types, final int[] colors) {
        final int type = types[ring];
        final int color = (type < 0) ? -1 : colors[type];
        this.setColor(color);
    }
    
    public void drawRotatedStringHoriz(final int pos, final int space, final String key) {
        final FontRecord r = this.getFontRecord();
        final FontMetrics metric = r.getFontMetrics();
        final int height = metric.getHeight() - metric.getLeading();
        final int descent = metric.getDescent();
        final int center = height / 2 - descent;
        final int width = metric.stringWidth(key);
        final int length = key.length();
        int delta = 0;
        final boolean vert = this.vertical && !FileIO.isAsciiString(key, true);
        for (int k = 0; k < length; ++k) {
            final AffineTransform rot = DrawAWT.g2d.getTransform();
            final String str = key.substring(k, k + 1);
            final int char_width = metric.stringWidth(str);
            delta += char_width / 2;
            DrawAWT.g2d.rotate(-(0.5 * width - delta) / pos);
            DrawAWT.g2d.translate(pos + space / 2, 0);
            if (vert) {
                DrawAWT.g2d.rotate(this.getAngle() + this.init_angle);
            }
            else {
                DrawAWT.g2d.rotate(1.5707963267948966);
            }
            DrawAWT.g2d.translate(-char_width / 2, center);
            DrawAWT.g2d.drawString(str, 0, 0);
            delta += char_width - char_width / 2;
            DrawAWT.g2d.setTransform(rot);
        }
    }
    
    public int getFontWidth() {
        final FontRecord r = this.getFontRecord();
        final FontMetrics metric = r.getFontMetrics();
        return this.getMaxAdvance(metric);
    }
    
    public int drawRotatedStringVert(final int pos, final int space, final String key, final boolean vert) {
        final FontRecord r = this.getFontRecord();
        final FontMetrics metric = r.getFontMetrics();
        final int len = key.length();
        int max_width = 0;
        for (int i = 0; i < len; ++i) {
            final int w = metric.charWidth(key.charAt(i));
            max_width = Math.max(w, max_width);
        }
        final int height = metric.getHeight();
        final int leading = metric.getLeading();
        final int descent = metric.getDescent();
        final int t_height = height * len - leading;
        final int gap = (space - t_height) / 2;
        final int offset = pos + gap + descent;
        final int center = (height - leading) / 2 - descent;
        final AffineTransform rot = DrawAWT.g2d.getTransform();
        for (int j = 0; j < len; ++j) {
            final String str = key.substring(j, j + 1);
            final int char_width = metric.stringWidth(str);
            DrawAWT.g2d.setTransform(rot);
            DrawAWT.g2d.translate(offset + height * (len - j - 1) + center, 0);
            if (vert) {
                DrawAWT.g2d.rotate(this.getAngle() + this.init_angle);
            }
            else {
                DrawAWT.g2d.rotate(1.5707963267948966);
            }
            DrawAWT.g2d.translate(-char_width / 2, center);
            DrawAWT.g2d.drawString(str, 0, 0);
        }
        return gap;
    }
    
    public int drawRotatedStringVert(final int pos, final int space, final String key) {
        return this.drawRotatedStringVert(pos, space, key, this.vertical);
    }
    
    public void drawRotatedSign(final int pos, final int width, final boolean plus) {
        DrawAWT.g2d.translate(pos, 0);
        if (this.vertical) {
            DrawAWT.g2d.rotate(this.getAngle() + this.init_angle);
        }
        else {
            DrawAWT.g2d.rotate(1.5707963267948966);
        }
        DrawAWT.g2d.drawLine(-width / 2, 0, width / 2, 0);
        if (plus) {
            DrawAWT.g2d.drawLine(0, -width / 2, 0, width / 2);
        }
    }
    
    public void drawString(final String str, final int x, final int y) {
        DrawAWT.g2d.drawString(str, x, y);
    }
    
    public int stringWidth(final String str) {
        final FontRecord r = this.getFontRecord();
        final FontMetrics metric = r.getFontMetrics();
        return metric.stringWidth(str);
    }
    
    public double[] setFittedFont(final int max_height, final int radius, final int n_entry, final boolean single_row, final boolean high_res) {
        int single_max_entry = 1;
        final int[] font_size = new int[3];
        final int style = Resource.getFontStyle();
        Resource.getIntArray("g2d_font_size", font_size);
        if (this.font_scaler > 1) {
            for (int i = 0; i < font_size.length; ++i) {
                final int[] array = font_size;
                final int n = i;
                array[n] *= this.font_scaler;
            }
        }
        if (high_res) {
            final int[] array2 = font_size;
            final int n2 = 0;
            array2[n2] /= 2;
        }
        for (int i = 0; i < (single_row ? 1 : 2); ++i) {
            int n_col;
            int height;
            int size;
            if (i == 0) {
                n_col = n_entry;
                height = max_height;
                size = ((height == 0) ? font_size[0] : font_size[1]);
            }
            else {
                n_col = n_entry / 2 + 1;
                size = font_size[1];
                height = max_height / 2;
            }
            final int width = (int)(6.283185307179586 * radius / (12.0 * n_col));
            while (size >= font_size[0]) {
                final FontRecord r = this.getFontRecord(size, style);
                final int h = r.getHeight();
                final int w = r.getWidth();
                if ((h <= height && w <= width) || ((n_entry == 1 || i > 0 || single_row) && size == font_size[0])) {
                    DrawAWT.g2d.setFont(r.getFont());
                    if (n_entry > 1 && i > 0 && (h > height || w > width)) {
                        if (n_entry > single_max_entry) {
                            return this.setFittedFont(max_height, radius, single_max_entry, single_row, high_res);
                        }
                        i = 0;
                        n_col = n_entry;
                    }
                    double min_degree = this.degreeInWidth(radius);
                    final double max_entry = 30.0 / min_degree;
                    if (max_entry > n_col) {
                        min_degree *= max_entry / n_col;
                    }
                    else {
                        min_degree = 30.0 / (int)max_entry;
                    }
                    final double[] result = { min_degree, Math.min((int)max_entry, n_col), i };
                    return result;
                }
                if (i == 0 && size == font_size[0]) {
                    DrawAWT.g2d.setFont(r.getFont());
                    final double min_degree = this.degreeInWidth(radius);
                    single_max_entry = (int)(30.0 / min_degree);
                    if (single_max_entry < 0) {
                        single_max_entry = 0;
                    }
                }
                size -= font_size[2];
            }
        }
        return null;
    }
    
    private FontRecord getFontRecord(final int size, final int style) {
        final int map_style = style;
        final Point p = new Point(size, map_style);
        FontRecord r = (FontRecord) DrawAWT.font_table.get(p);
        if (r == null) {
            r = new FontRecord();
            final Font font = new Font(Resource.getFontName(), style, size);
            final AffineTransform tr = DrawAWT.g2d.getTransform();
            DrawAWT.g2d.setTransform(new AffineTransform());
            final Rectangle2D b = font.getMaxCharBounds(DrawAWT.g2d.getFontRenderContext());
            FontRecord.access$1(r, DrawAWT.g2d.getFontMetrics(font));
            FontRecord.access$2(r, b.getWidth());
            FontRecord.access$3(r, b.getHeight());
            DrawAWT.g2d.setTransform(tr);
            DrawAWT.font_table.put(p, r);
        }
        return r;
    }
    
    private FontRecord getFontRecord() {
        final Font font = DrawAWT.g2d.getFont();
        return this.getFontRecord(font.getSize(), font.getStyle());
    }
    
    public double radianInWidth(final int radius) {
        final FontRecord r = this.getFontRecord();
        return r.width / radius;
    }
    
    public double degreeInWidth(final int radius) {
        return this.radianInWidth(radius) / 6.283185307179586 * 360.0;
    }
    
    public double degreeInWidth(final String str, final int radius) {
        final FontRecord r = this.getFontRecord();
        final int width = r.getFontMetrics().stringWidth(str);
        return width / (6.283185307179586 * radius) * 360.0;
    }
    
    public void initFontMetric(final int radius) {
        final FontRecord r = this.getFontRecord();
        this.cur_metric = r.getFontMetrics();
        this.cur_radius = radius;
    }
    
    public double degreeInWidth(final String left, final String right) {
        char[] array = left.toCharArray();
        int l_width = 0;
        for (int i = 0; i < array.length; ++i) {
            l_width = Math.max(l_width, this.cur_metric.charWidth(array[i]));
        }
        array = right.toCharArray();
        int r_width = 0;
        for (int j = 0; j < array.length; ++j) {
            r_width = Math.max(r_width, this.cur_metric.charWidth(array[j]));
        }
        return 0.5 * (l_width + r_width) / (6.283185307179586 * this.cur_radius) * 360.0;
    }
    
    public int getFontHeight() {
        final FontRecord r = this.getFontRecord();
        return r.getHeight();
    }
    
    public void init(final Graphics2D new_g2d, final int scaler, final String pre, final boolean rotate, final boolean vert) {
        DrawAWT.g2d = new_g2d;
        this.initColor();
        this.font_scaler = scaler;
        this.prefix = pre;
        this.color_key = null;
        this.color_key_color = null;
        this.font_size_override = 0;
        this.vertical = vert;
        this.init_angle = (rotate ? 1.5707963267948966 : 0.0);
    }
    
    public void reset() {
        DrawAWT.g2d.setTransform(this.mark_tran);
        this.setColor();
    }
    
    public void drawRect(final int x, final int y, final int width, final int height) {
        this.setColor();
        DrawAWT.g2d.drawRect(x, y, width, height);
    }
    
    public void fillRect(final int x, final int y, final int width, final int height) {
        this.setColor(this.bg_color);
        DrawAWT.g2d.fillRect(x, y, width, height);
    }
    
    public void drawCircle(final int radius) {
        this.setColor();
        DrawAWT.g2d.drawOval(-radius, -radius, radius * 2, radius * 2);
    }
    
    public void drawDiamond(final int radius) {
        this.setColor();
        DrawAWT.g2d.drawLine(-radius, 0, 0, radius);
        DrawAWT.g2d.drawLine(0, radius, radius, 0);
        DrawAWT.g2d.drawLine(radius, 0, 0, -radius);
        DrawAWT.g2d.drawLine(0, -radius, -radius, 0);
    }
    
    public void drawDashCircle(final int dash_type, final int radius) {
        this.setColor();
        this.pushSetStroke(dash_type);
        DrawAWT.g2d.drawOval(-radius, -radius, radius * 2, radius * 2);
        this.popStroke();
    }
    
    public void fillCircle(final int x, final int y, final int width, final int height) {
        this.setColor(this.bg_color);
        DrawAWT.g2d.fillArc(x, y, width, height, 0, 360);
    }
    
    public void drawLine(final int x1, final int y1, final int x2, final int y2) {
        this.setColor();
        DrawAWT.g2d.drawLine(x1, y1, x2, y2);
    }
    
    public void drawDashLine(final int dash_type, final int x1, final int y1, final int x2, final int y2) {
        this.setColor();
        this.pushSetStroke(dash_type);
        DrawAWT.g2d.drawLine(x1, y1, x2, y2);
        this.popStroke();
    }
    
    public void drawWideLine(final int width, final int x1, final int y1, final int x2, final int y2) {
        this.setColor();
        final Stroke stroke = DrawAWT.g2d.getStroke();
        DrawAWT.g2d.setStroke(new BasicStroke((float)width, 0, 2));
        DrawAWT.g2d.drawLine(x1, y1, x2, y2);
        DrawAWT.g2d.setStroke(stroke);
    }
    
    private void pushSetStroke(final int dash_type) {
        this.stroke_save = DrawAWT.g2d.getStroke();
        switch (dash_type) {
            case 1: {
                DrawAWT.g2d.setStroke(DrawAWT.dashed);
                break;
            }
            case 2: {
                DrawAWT.g2d.setStroke(DrawAWT.sparse_dashed);
                break;
            }
            case 3: {
                DrawAWT.g2d.setStroke(DrawAWT.dense_dashed);
                break;
            }
            case 4: {
                DrawAWT.g2d.setStroke(DrawAWT.dot_dashed);
                break;
            }
            case 5: {
                DrawAWT.g2d.setStroke(DrawAWT.dotted);
                break;
            }
        }
    }
    
    private void popStroke() {
        DrawAWT.g2d.setStroke(this.stroke_save);
    }
    
    public void paintFillArc(final int color, final int lower_radius, final int upper_radius, final double start_rad, final double end_rad) {
        final Paint save = DrawAWT.g2d.getPaint();
        DrawAWT.g2d.setPaint(new Color(color));
        final Polygon p = this.ArcToPolygon(lower_radius, upper_radius, start_rad, end_rad);
        DrawAWT.g2d.fill(p);
        DrawAWT.g2d.setPaint(save);
    }
    
    public void fillArc(final int lower_radius, final int upper_radius, final double start_rad, final double end_rad) {
        final Polygon p = this.ArcToPolygon(lower_radius, upper_radius, start_rad, end_rad);
        this.setColor(this.bg_color);
        DrawAWT.g2d.fill(p);
    }
    
    private Polygon ArcToPolygon(final int lower_radius, final int upper_radius, final double start_rad, final double end_rad) {
        double delta = end_rad - start_rad;
        if (delta > 0.0) {
            delta -= 6.283185307179586;
        }
        int line_width = (int)Math.ceil(-delta * (lower_radius + upper_radius) / 2.0);
        int num_line = line_width / 10;
        if (num_line < 1) {
            num_line = 1;
        }
        line_width = (int)Math.ceil(line_width / (double)num_line);
        delta /= num_line;
        final AffineTransform rot = new AffineTransform();
        rot.rotate(-start_rad);
        final Point2D pt = new Point2D.Double(0.0, 0.0);
        final Polygon p = new Polygon();
        for (int i = 0; i <= num_line; ++i) {
            pt.setLocation(upper_radius, 0.0);
            rot.transform(pt, pt);
            p.addPoint((int)Math.round(pt.getX()), (int)Math.round(pt.getY()));
            rot.rotate(-delta);
        }
        for (int i = 0; i <= num_line; ++i) {
            rot.rotate(delta);
            pt.setLocation(lower_radius, 0.0);
            rot.transform(pt, pt);
            p.addPoint((int)Math.round(pt.getX()), (int)Math.round(pt.getY()));
        }
        return p;
    }
    
    public void setSpecialStringColor(final int[] color, final String[] key) {
        this.color_key = key;
        this.color_key_color = color;
    }
    
    public Point drawAlignStringVert(final String key, final int x, final int y, final boolean smaller, final boolean find_size) {
        final int big_size = Resource.getInt(String.valueOf(this.prefix) + "print_big_font_size") * this.font_scaler;
        FontRecord r = this.getFontRecord(big_size, 1);
        final Font big_font = r.getFont();
        DrawAWT.g2d.setFont(big_font);
        FontMetrics metric = DrawAWT.g2d.getFontMetrics();
        int big_width = this.getMaxAdvance(metric);
        final int big_height = metric.getHeight();
        final int size = Resource.getInt(String.valueOf(this.prefix) + (smaller ? "print_smaller_med_font_size" : "print_small_med_font_size")) * this.font_scaler;
        r = this.getFontRecord(size, 0);
        final Font med_font = r.getFont();
        DrawAWT.g2d.setFont(med_font);
        metric = DrawAWT.g2d.getFontMetrics();
        int width = this.getMaxAdvance(metric);
        final int height = metric.getHeight();
        int n_x = x;
        int n_y = y;
        width = -width;
        big_width = -big_width;
        n_x += big_width;
        int max_y = n_y;
        boolean use_big = false;
        for (int len = key.length(), i = 0; i < len; ++i) {
            final String str = key.substring(i, i + 1);
            if (str.equals("|")) {
                max_y = Math.max(max_y, n_y);
                n_y = y;
                n_x += (use_big ? big_width : width);
            }
            else if (str.equals("$")) {
                n_y = y;
                n_x += (use_big ? big_width : width) / 4;
            }
            else if (str.equals("%")) {
                n_y += height / 2;
            }
            else if (str.equals(">")) {
                use_big = true;
                DrawAWT.g2d.setFont(big_font);
            }
            else if (str.equals("<")) {
                use_big = false;
                DrawAWT.g2d.setFont(med_font);
            }
            else {
                n_y += (use_big ? big_height : height);
                if (!find_size) {
                    DrawAWT.g2d.drawString(str, n_x, n_y);
                }
            }
        }
        max_y = Math.max(max_y, n_y);
        return new Point(x - n_x, max_y - y);
    }
    
    public void drawLargeBoldStringHoriz(final String key, final int x, final int y, final boolean non_eng) {
        final int size = Resource.getInt(String.valueOf(this.prefix) + "print_med_big_font_size") * this.font_scaler;
        DrawAWT.g2d.setFont(new Font(non_eng ? Resource.getFontName() : Resource.getEnFontName(), 1, size));
        final FontMetrics metric = DrawAWT.g2d.getFontMetrics();
        DrawAWT.g2d.drawString(key, x, y + metric.getHeight());
    }
    
    public Point drawStyledStringHoriz(final String key, final int x, final int y, final int style, final boolean find_size) {
        final int size = Resource.getInt(String.valueOf(this.prefix) + "print_smallest_font_size") * this.font_scaler;
        final FontRecord r = this.getFontRecord(size, style);
        DrawAWT.g2d.setFont(r.getFont());
        final FontMetrics metric = DrawAWT.g2d.getFontMetrics();
        if (!find_size) {
            DrawAWT.g2d.drawString(key, x, y + metric.getHeight());
        }
        return new Point(metric.stringWidth(key), metric.getHeight());
    }
    
    public Point drawAlignStringHoriz(final String key, final int x, final int y, final String template, final int[] color_array, final boolean find_size) {
        final int size = ((this.font_size_override > 0) ? this.font_size_override : Resource.getInt(String.valueOf(this.prefix) + "print_smallest_font_size")) * this.font_scaler;
        final FontRecord r = this.getFontRecord(size, 0);
        DrawAWT.g2d.setFont(r.getFont());
        final FontMetrics metric = DrawAWT.g2d.getFontMetrics();
        final int width = this.getMaxAdvance(metric);
        final int height = metric.getHeight();
        int n_x = x;
        int n_y = y + height;
        int max_x = 0;
        final int len = key.length();
        int[] char_width = null;
        if (template != null) {
            char_width = new int[template.length()];
            for (int i = 0; i < char_width.length; ++i) {
                switch (template.charAt(i)) {
                    case 'h': {
                        char_width[i] = width / 2;
                        break;
                    }
                    case 'q': {
                        char_width[i] = 3 * width / 4;
                        break;
                    }
                    case 'd': {
                        char_width[i] = 2 * width;
                        break;
                    }
                    case 's': {
                        char_width[i] = height;
                        break;
                    }
                    case 'D': {
                        char_width[i] = -2 * width;
                        break;
                    }
                    case 'E': {
                        char_width[i] = -(width + width / 2);
                        break;
                    }
                    case '.': {
                        char_width[i] = 0;
                        break;
                    }
                    default: {
                        char_width[i] = width;
                        break;
                    }
                }
            }
        }
        int index = 0;
        int row = 0;
        int col = 0;
        for (int j = 0; j < len; ++j) {
            final String str = key.substring(j, j + 1);
            if (str.equals("|")) {
                max_x = Math.max(max_x, n_x);
                n_x = x;
                n_y += height;
                if (char_width != null && index < char_width.length && char_width[index] == 0) {
                    ++index;
                }
                ++row;
                col = 0;
            }
            else if (str.equals("$")) {
                n_x = x;
                n_y += height / 4;
                if (char_width != null && index < char_width.length && char_width[index] == 0) {
                    ++index;
                }
                ++row;
            }
            else {
                int advance = width;
                if (char_width != null) {
                    if (index >= char_width.length) {
                        index = 0;
                    }
                    advance = char_width[index++];
                    if (advance == 0) {
                        advance = char_width[--index - 1];
                    }
                    if (advance < 0) {
                        advance = -advance;
                        final int pre = (advance - width) / 2;
                        n_x += pre;
                        advance -= pre;
                    }
                }
                if (!find_size && !this.drawColorString(str, n_x, n_y)) {
                    if (color_array != null && ((row == 0 && col > 0) || (col == 0 && row > 0))) {
                        final Color color = DrawAWT.g2d.getColor();
                        this.setColor(color_array[ 0]);
                        DrawAWT.g2d.drawString(str, n_x, n_y);
                        DrawAWT.g2d.setColor(color);
                    }
                    else {
                        DrawAWT.g2d.drawString(str, n_x, n_y);
                    }
                }
                n_x += advance;
                ++col;
            }
        }
        max_x = Math.max(max_x, n_x);
        return new Point(max_x - x, n_y - y);
    }
    
    public void setFontSizeOverride(final int val) {
        this.font_size_override = val;
    }
    
    public Point drawStringHoriz(final String key, final int x, final int y, final boolean align_right, final boolean find_size) {
        return this.drawSizeStringHoriz("print_smallest_font_size", key, x, y, align_right, find_size);
    }
    
    public Point drawLargeStringHoriz(final String key, final int x, final int y, final boolean align_right, final boolean find_size) {
        return this.drawSizeStringHoriz("print_med_font_size", key, x, y, align_right, find_size);
    }
    
    public Point drawSizeStringHoriz(final String font_size, String key, final int x, final int y, final boolean align_right, final boolean find_size) {
        final int size = Resource.getInt(String.valueOf(this.prefix) + font_size) * this.font_scaler;
        final FontRecord r = this.getFontRecord(size, 0);
        DrawAWT.g2d.setFont(r.getFont());
        final FontMetrics metric = DrawAWT.g2d.getFontMetrics();
        int max_width = 0;
        final int height = metric.getHeight();
        int n_y = y + height;
        while (key != null && !key.equals("")) {
            final int index = key.indexOf("|");
            String str;
            if (index >= 0) {
                str = key.substring(0, index);
                key = key.substring(index + 1);
            }
            else {
                str = key;
                key = null;
            }
            final int width = metric.stringWidth(str);
            max_width = Math.max(width, max_width);
            if (!find_size) {
                final int n_x = align_right ? (x - width) : x;
                if (!this.drawColorString(str, n_x, n_y)) {
                    DrawAWT.g2d.drawString(str, n_x, n_y);
                }
            }
            n_y += height;
        }
        return new Point(max_width, n_y - y - height);
    }
    
    private boolean drawColorString(final String str, final int x, final int y) {
        if (this.color_key != null) {
            for (int i = 0; i < this.color_key.length; ++i) {
                if (str.equals(this.color_key[i])) {
                    final Color color = DrawAWT.g2d.getColor();
                    this.setColor(this.color_key_color[i]);
                    DrawAWT.g2d.drawString(str, x, y);
                    DrawAWT.g2d.setColor(color);
                    return true;
                }
            }
        }
        return false;
    }
    
    private int getMaxAdvance(final FontMetrics metric) {
        return Math.abs(metric.getMaxAdvance());
    }
    
    public void frameTable(final int dir, int start_x, int start_y, final int width, final int height, final LinkedList half_list, final LinkedList four_list) {
        final FontMetrics metric = DrawAWT.g2d.getFontMetrics();
        final int y_inc;
        final int x_inc = y_inc = metric.getHeight();
        start_x -= this.getMaxAdvance(metric) / 16;
        start_y += 3 * metric.getDescent();
        final int end_x = start_x + width;
        final int end_y = start_y + height;
        if (dir >= 0) {
            for (int y = start_y + y_inc, i = 0; y < end_y; y += y_inc, ++i) {
                for (int x = start_x + i * x_inc; x < end_x; x += x_inc) {
                    this.drawLine(x, y, end_x, y);
                }
            }
            for (int x = end_x - x_inc, i = 0; x > start_x; x -= x_inc, ++i) {
                for (int y = end_y - i * y_inc; y > start_y; y -= y_inc) {
                    this.drawLine(x, start_y, x, y);
                }
            }
        }
        if (dir <= 0) {
            for (int y = end_y - y_inc, i = 0; y > start_y; y -= y_inc, ++i) {
                for (int x = end_x - i * x_inc; x > start_x; x -= x_inc) {
                    this.drawLine(start_x, y, x, y);
                }
            }
            for (int x = start_x + x_inc, i = 0; x < end_x; x += x_inc, ++i) {
                for (int y = start_y + i * y_inc; y < end_y; y += y_inc) {
                    this.drawLine(x, y, x, end_y);
                }
            }
        }
        start_x += ((dir > 0) ? 0 : x_inc);
        start_y += ((dir < 0) ? 0 : y_inc);
        for (int i = 0; i < 2; ++i) {
            final LinkedList list = (i == 0) ? half_list : four_list;
            if (list != null) {
                try {
                    final ListIterator iter = list.listIterator();
                    while (true) {
                        final Point loc = (Point) iter.next();
                        final int x = start_x + loc.x * x_inc;
                        final int y = start_y + loc.y * y_inc;
                        this.drawLine(x + x_inc, y, x, y + y_inc);
                        if (i > 0) {
                            this.drawLine(x, y, x + x_inc, y + y_inc);
                        }
                    }
                }
                catch (NoSuchElementException ex) {}
            }
        }
    }
    
    public static void resetFontTable() {
        DrawAWT.font_table = new Hashtable();
    }
    
    private static class FontRecord
    {
        private double width;
        private double height;
        private FontMetrics font_metrics;
        
        public int getWidth() {
            return (int)this.width;
        }
        
        public int getHeight() {
            return (int)this.height;
        }
        
        public Font getFont() {
            return this.getFontMetrics().getFont();
        }
        
        FontMetrics getFontMetrics() {
            return this.font_metrics;
        }
        
        static /* synthetic */ void access$1(final FontRecord fontRecord, final FontMetrics font_metrics) {
            fontRecord.font_metrics = font_metrics;
        }
        
        static /* synthetic */ void access$2(final FontRecord fontRecord, final double width) {
            fontRecord.width = width;
        }
        
        static /* synthetic */ void access$3(final FontRecord fontRecord, final double height) {
            fontRecord.height = height;
        }
    }
}
