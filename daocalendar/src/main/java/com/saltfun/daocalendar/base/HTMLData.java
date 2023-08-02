package com.saltfun.daocalendar.base;

import java.io.File;
import java.util.StringTokenizer;

public class HTMLData
{
    static int num_page;
    static int cur_page;
    static String base_name;
    static String page_title;
    private static FileIO file;
    
    public static void init(final String file_name, final String title, final int n_page) {
        HTMLData.cur_page = 0;
        HTMLData.num_page = n_page;
        HTMLData.base_name = file_name;
        HTMLData.page_title = title;
    }
    
    public static void header() {
        final String file_name = getFileName(HTMLData.cur_page);
        if (HTMLData.cur_page > 0) {
            FileIO.setTempFile(file_name);
        }
        (HTMLData.file = new FileIO(file_name, false, false)).putLine("<html>");
        HTMLData.file.putLine("<head>");
        HTMLData.file.putLine("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
        HTMLData.file.putLine("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">");
        HTMLData.file.putLine("<title>" + htmlString(HTMLData.page_title) + "</title>");
        HTMLData.file.putLine("</head>");
        HTMLData.file.putLine("<body>");
    }
    
    public static void paragraph(final String data) {
        HTMLData.file.putLine("<p>");
        final StringTokenizer st = new StringTokenizer(data, "|");
        while (st.hasMoreTokens()) {
            HTMLData.file.putLine(String.valueOf(htmlString(st.nextToken())) + "<br>");
        }
        HTMLData.file.putLine("</p>");
    }
    
    public static void tableHeader(final String[] row_header) {
        if (HTMLData.num_page > 1) {
            addLink();
        }
        HTMLData.file.putLine("<table border=\"1\" id=\"table1\" cellpadding=\"4\">");
        HTMLData.file.putLine("<tr>");
        for (int i = 0; i < row_header.length; ++i) {
            HTMLData.file.putLine("<th>" + htmlString(row_header[i]) + "</th>");
        }
        HTMLData.file.putLine("</tr>");
    }
    
    public static void tableFooter() {
        HTMLData.file.putLine("</table>");
        if (HTMLData.num_page > 1) {
            addLink();
        }
    }
    
    private static void addLink() {
        HTMLData.file.putLine("<p>");
        if (HTMLData.cur_page > 0) {
            HTMLData.file.putLine("<a href=\"" + getFileName(HTMLData.cur_page - 1) + "\"><img border=\"0\" src=\"" + FileIO.getURL("icon/backward.ico").toString() + "\" width=\"16\" height=\"12\"></a>");
        }
        for (int i = 0; i < HTMLData.num_page; ++i) {
            final String name = getFileName(i);
            String num = Integer.toString(i + 1);
            if (i == HTMLData.cur_page) {
                num = "[" + num + "]";
            }
            HTMLData.file.putLine("<a href=\"" + name + "\">" + num + "</a>");
        }
        if (HTMLData.cur_page < HTMLData.num_page - 1) {
            HTMLData.file.putLine("<a href=\"" + getFileName(HTMLData.cur_page + 1) + "\"><img border=\"0\" src=\"" + FileIO.getURL("icon/forward.ico").toString() + "\" width=\"16\" height=\"12\"></a>");
        }
        HTMLData.file.putLine("</p>");
    }
    
    private static String getFileName(final int page) {
        final int index = HTMLData.base_name.lastIndexOf(".");
        String file_name = HTMLData.base_name.substring(0, index);
        if (page > 0) {
            file_name = String.valueOf(file_name) + "_" + Integer.toString(page);
        }
        return String.valueOf(file_name) + ".html";
    }
    
    public static void footer() {
        HTMLData.file.putLine("</body>");
        HTMLData.file.putLine("</html>");
        HTMLData.file.dispose();
        ++HTMLData.cur_page;
    }
    
    public static void tableRow(final double jd_ut, final String date, final String s_sign, final String aspect, final String e_sign) {
        tableRow(Double.toString(jd_ut), date, s_sign, aspect, e_sign);
    }
    
    public static void tableRow(final String target, final String date, final String s_sign, final String aspect, final String e_sign) {
        HTMLData.file.putLine("<tr>");
        HTMLData.file.putLine("<td align=\"center\"><a href=\"" + target + "\">" + date + "</a></td>");
        if (s_sign != null) {
            HTMLData.file.putLine("<td align=\"center\">" + htmlString(s_sign) + "</td>");
        }
        if (aspect != null) {
            HTMLData.file.putLine("<td align=\"center\">" + htmlString(aspect) + "</td>");
        }
        if (e_sign != null) {
            HTMLData.file.putLine("<td align=\"center\">" + htmlString(e_sign) + "</td>");
        }
        HTMLData.file.putLine("</tr>");
    }
    
    public static void tableRow(final String[] data) {
        HTMLData.file.putLine("<tr>");
        for (int i = 0; i < data.length; ++i) {
            HTMLData.file.putLine("<td align=\"center\">" + htmlString(data[i]) + "</td>");
        }
        HTMLData.file.putLine("</tr>");
    }
    
    public static String htmlString(final String str) {
        final char[] array = str.toCharArray();
        String val = "";
        for (int i = 0; i < array.length; ++i) {
            val = String.valueOf(val) + "&#" + Integer.toString(array[i]) + ";";
        }
        return val;
    }
    
    public static String extractData(final String str) {
        if (str == null) {
            return null;
        }
        final int index = str.lastIndexOf(File.separator);
        return (index >= 0) ? str.substring(index + 1) : str;
    }
}
