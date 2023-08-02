package com.saltfun.daocalendar.base;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;

public abstract class BaseTab
{
    protected final String  EOL = "\r\n";
    private JTextArea area;
    private int column_height;
    private int column_width;
    private int column_gap;
    private int column_extra;
    private int num_pages;
    
    public void clear() {
    }
    
    public void setWrapMode(final boolean set) {
    }
    
    public void append(final String str) {
    }
    
    public void appendLine(final String str) {
    }
    
    public void appendLine() {
    }
    
    public void setName(final String name, final boolean sex, final boolean replace) {
    }
    
    public void setName(final String name, final boolean replace) {
    }
    
    public void replace(final String src, final String dst) {
    }
    
    public int initPrint(final Graphics2D g2d, final String data, final Point page_size) {
        (area = new JTextArea(data)).setDoubleBuffered(false);
        int scaler = Resource.getInt("print_scaler");
        int size = scaler * Resource.getInt("g2d_print_data_font_size");
        Font area_font = new Font(Resource.getFontName(), Resource.getFontStyle(), size);
        FontMetrics metric = g2d.getFontMetrics(area_font);
        column_gap = (int)(page_size.x * Resource.getDouble("print_dual_page_gap"));
        column_width = (page_size.x - column_gap) / 2;
        column_height = (page_size.y / metric.getHeight() - 1) * metric.getHeight();
        column_extra = (int)(0.05 * column_width);
        area.setFont(area_font);
        area.setLineWrap(true);
        area.setWrapStyleWord(false);
        area.setTabSize(4);
        num_pages = 1;
        while (num_pages <= 10) {
            final int new_height = 2 * num_pages * column_height;
            area.setSize(column_width, new_height);
            try {
                // Fix warning about modelToView deprecation
                // https://github.com/JetBrains/intellij-community/commit/2620846138ba86489c768184d10f520f0941b26f
                final Rectangle rect = area.modelToView2D(data.length() - 1).getBounds();
                if (rect.y + rect.height <= new_height) {
                    break;
                }
            }
            catch (BadLocationException ignored) {}
            ++num_pages;
        }
        return num_pages;
    }
    
    public boolean print(final Graphics2D g2d, final Point page_size, final int page, final int offset, final boolean page_no) {
        if (page > num_pages + offset - 1) {
            return true;
        }
        final int scaler = Resource.getInt("print_scaler");
        final DrawAWT draw = new DrawAWT();
        draw.init(g2d, scaler, "", false, true);
        ChartData.showDescFootNote(draw, page_size, null, false, (!page_no && num_pages == 1) ? 0 : page);
        final int y = 2 * (page - offset) * column_height;
        g2d.translate(0, -y);
        g2d.setClip(0, y, column_width + column_extra, column_height);
        area.update(g2d);
        g2d.translate(column_width + column_gap, -column_height);
        g2d.setClip(0, y + column_height, column_width + column_extra, column_height);
        area.update(g2d);
        return false;
    }
}
