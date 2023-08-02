package com.saltfun.daocalendar.base;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileIO
{
    private static BaseIO base;
    private Hashtable table;
    private Hashtable table_save;
    private boolean has_custom_data;
    private BufferedWriter writer;
    private StringWriter string_writer;
    private String search_key;
    
    static {
        FileIO.base = null;
    }
    
    public FileIO(final String file_name, final String custom_name) {
        this.table = null;
        this.table_save = null;
        this.has_custom_data = false;
        this.writer = null;
        this.string_writer = null;
        this.search_key = null;
        this.initRead(file_name, custom_name, false);
    }
    
    public FileIO(final String file_name, final boolean string_reader) {
        this.table = null;
        this.table_save = null;
        this.has_custom_data = false;
        this.writer = null;
        this.string_writer = null;
        this.initRead(file_name, this.search_key = null, string_reader);
    }
    
    public FileIO(final String file_name, final String search, final boolean string_reader) {
        this.table = null;
        this.table_save = null;
        this.has_custom_data = false;
        this.writer = null;
        this.string_writer = null;
        this.search_key = null;
        this.search_key = search;
        this.initRead(file_name, null, string_reader);
    }
    
    public FileIO(final String file_name, final boolean append, final boolean unicode) {
        this.table = null;
        this.table_save = null;
        this.has_custom_data = false;
        this.writer = null;
        this.string_writer = null;
        this.search_key = null;
        try {
            if (file_name == null) {
                this.string_writer = new StringWriter();
                this.writer = new BufferedWriter(this.string_writer);
            }
            else if (unicode) {
                this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file_name, append), "UTF-16"));
            }
            else {
                this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file_name, append)));
            }
        }
        catch (IOException e) {
            this.string_writer = null;
            this.writer = null;
        }
    }
    
    public String getDataInString() {
        if (this.string_writer == null || this.writer == null) {
            return null;
        }
        try {
            this.writer.close();
        }
        catch (Exception ex) {}
        this.writer = null;
        return this.string_writer.toString();
    }
    
    private void initRead(final String file_name, String custom_name, final boolean string_reader) {
        int rule_index = 1;
        URL url = null;
        if (string_reader) {
            if (file_name == null || file_name.trim().equals("")) {
                return;
            }
        }
        else {
            url = getURL(file_name);
            if (url == null) {
                return;
            }
        }
        final Hashtable hashtable = new Hashtable();
        this.table_save = hashtable;
        this.table = hashtable;
        try {
            for (int i = 0; i < 2; ++i) {
                BufferedReader reader;
                if (string_reader) {
                    reader = new BufferedReader(new StringReader(file_name.trim()));
                }
                else {
                    reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-16"));
                }
                String line;
                while ((line = reader.readLine()) != null) {
                    while (line.endsWith("\\")) {
                        line = line.substring(0, line.length() - 1);
                        final String n_line = reader.readLine();
                        if (n_line == null) {
                            break;
                        }
                        line = String.valueOf(line) + n_line;
                    }
                    line = line.trim();
                    if (line.length() != 0) {
                        if (line.startsWith("#")) {
                            continue;
                        }
                        final int index = line.indexOf("#");
                        if (index >= 0) {
                            line = line.substring(0, index).trim();
                        }
                        final char c = line.charAt(0);
                        if (c == '?' || c == '{') {
                            line = line.replaceAll(" |\t", "");
                            final int n = line.indexOf(58);
                            if (n <= 0) {
                                continue;
                            }
                            String key = line.substring(1, n);
                            String info = line.substring(n + 1);
                            if (c == '{') {
                                final String rank = RuleEntry.getRankString(info);
                                final String new_key = "+__" + rule_index;
                                final String remain = info.substring(rank.length());
                                key = rank + key.substring(0, key.length() - 1);
                                RuleEntry.addRule(new_key, key);
                                key = "__" + rule_index++;
                                info = remain;
                            }
                            if (info.length() == 0) {
                                info = null;
                                while ((line = reader.readLine()) != null) {
                                    if (line.length() > 0 && line.charAt(0) == '$') {
                                        break;
                                    }
                                    while (line.endsWith("\\")) {
                                        line = line.substring(0, line.length() - 1);
                                        final String n_line2 = reader.readLine();
                                        if (n_line2 == null) {
                                            break;
                                        }
                                        line = String.valueOf(line) + n_line2;
                                    }
                                    if (info == null) {
                                        info = line;
                                    }
                                    else {
                                        info = String.valueOf(info) + "\n" + line;
                                    }
                                }
                            }
                            if (info == null) {
                                continue;
                            }
                            RuleEntry.addOutput(key, info);
                        }
                        else {
                            final boolean expr = c == '+' || c == '-' || c == '=';
                            final StringTokenizer st = new StringTokenizer(line, expr ? ":" : "=");
                            final int n_tok = st.countTokens();
                            if (n_tok != 1 && n_tok != 2) {
                                continue;
                            }
                            String key2 = st.nextToken().trim().toUpperCase();
                            boolean assign = false;
                            if (key2.endsWith(":")) {
                                assign = true;
                                key2 = key2.substring(0, key2.length() - 1).trim();
                            }
                            if (n_tok == 1) {
                                if (!line.endsWith("=")) {
                                    continue;
                                }
                                this.table.remove(key2);
                            }
                            else {
                                String info2 = st.nextToken().trim();
                                if (assign) {
                                    info2 = "^" + info2;
                                }
                                else if (info2.startsWith("\"") && info2.endsWith("\"")) {
                                    info2 = info2.substring(1, info2.length() - 1).trim();
                                }
                                if (expr) {
                                    RuleEntry.addRule(key2.replaceAll(" |\t", ""), info2.replaceAll(" |\t", ""));
                                }
                                else {
                                    this.table.put(key2, info2);
                                    if (this.search_key != null && this.search_key.equalsIgnoreCase(key2)) {
                                        break;
                                    }
                                }
                                if (i <= 0) {
                                    continue;
                                }
                                this.has_custom_data = true;
                            }
                        }
                    }
                }
                reader.close();
                if (custom_name == null) {
                    break;
                }
                url = getURL(custom_name);
                if (url == null) {
                    break;
                }
                custom_name = null;
            }
        }
        catch (IOException ex) {}
    }
    
    public boolean fileDiff(final String file1, final String file2, final String filter_prefix, final boolean inline) {
        boolean same = true;
        final Vector<String> data1 = new Vector<>(500);
        final Vector<String> data2 = new Vector<>(500);
        int size1 = 0;
        int size2 = 0;
        BufferedReader in1 = null;
        BufferedReader in2 = null;
        try {
            URL url = (new File(file1)).toURI().toURL();
            in1 = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-16"));
            url = (new File(file2)).toURI().toURL();
            in2 = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-16"));
            if (inline) {
                while (true) {
                    final String line1;
                    if ((line1 = in1.readLine()) != null) {
                        if (filter_prefix != null && line1.startsWith(filter_prefix)) {
                            continue;
                        }
                    }
                    String line2;
                    while ((line2 = in2.readLine()) != null && filter_prefix != null && line2.startsWith(filter_prefix)) {}
                    if (line1 == null && line2 == null) {
                        return true;
                    }
                    if (line1 == null || !line1.equals(line2)) {
                        if (line1 != null) {
                            this.putLine("< " + line1);
                        }
                        if (line2 != null) {
                            this.putLine("> " + line2);
                        }
                        return false;
                    }
                }
            }
            else {
                String line3;
                while ((line3 = in1.readLine()) != null) {
                    if (filter_prefix != null && line3.startsWith(filter_prefix)) {
                        continue;
                    }
                    data1.add(size1++, line3);
                }
                while ((line3 = in2.readLine()) != null) {
                    if (filter_prefix != null && line3.startsWith(filter_prefix)) {
                        continue;
                    }
                    data2.add(size2++, line3);
                }
                final int[][] opt = new int[size1 + 1][size2 + 1];
                for (int i = size1 - 1; i >= 0; --i) {
                    for (int j = size2 - 1; j >= 0; --j) {
                        if (data1.get(i).equals(data2.get(j))) {
                            opt[i][j] = opt[i + 1][j + 1] + 1;
                        }
                        else {
                            opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
                        }
                    }
                }
                int i = 0;
                int j = 0;
                while (i < size1) {
                    if (j >= size2) {
                        break;
                    }
                    if (data1.get(i).equals(data2.get(j))) {
                        ++i;
                        ++j;
                    }
                    else {
                        same = false;
                        if (opt[i + 1][j] >= opt[i][j + 1]) {
                            this.putLine("< " + data1.get(i++));
                        }
                        else {
                            this.putLine("> " + data2.get(j++));
                        }
                    }
                }
                while (i < size1 || j < size2) {
                    if (i == size1 || j == size2) {
                        same = false;
                        if (i == size1) {
                            this.putLine("> " + data2.get(j++));
                        }
                        else {
                            this.putLine("< " + data1.get(i++));
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            same = false;
            this.putLine("  Cannot read from file " + ((in1 == null) ? file1 : file2) + "!");
        }
        return same;
    }
    
    public boolean hasCustomData() {
        return this.has_custom_data;
    }
    
    public String getString(final String key) {
        return this.getKey(key);
    }
    
    public void putString(final String key, final String val) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(String.valueOf(key) + "=\"" + val + "\"");
            this.writer.newLine();
        }
        catch (Exception ex) {}
    }
    
    public void putString(final String val) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(val);
        }
        catch (Exception ex) {}
    }
    
    public void putLine(final String val) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(val);
            this.writer.newLine();
        }
        catch (Exception ex) {}
    }
    
    public int getInt(final String key) {
       String value = getKey(key);
        try {
            int n = Integer.parseInt(value);
            return n;
        }
        catch (NumberFormatException e) {
            if (value.toLowerCase().startsWith("0x")) {
                try {
                    final int n2 = Integer.parseInt(value.substring(2), 16);
                    return n2;
                }
                catch (NumberFormatException er) {
                    return Integer.MIN_VALUE;
                }
            }
            return Integer.MIN_VALUE;
        }
    }
    
    public void putInt(final String key, final int val) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(String.valueOf(key) + "=" + val);
            this.writer.newLine();
        }
        catch (Exception ex) {}
    }
    
    public double getDouble(final String key) {
        final String value = this.getKey(key);
        try {
            final double f = Double.parseDouble(value);
            return f;
        }
        catch (NumberFormatException e) {
            return Double.NEGATIVE_INFINITY;
        }
    }
    
    public void putDouble(final String key, final double val) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(String.valueOf(key) + "=" + val);
            this.writer.newLine();
        }
        catch (Exception ex) {}
    }
    
    public int getStringArray(final String key, final String[] array) {
        final String value = this.getKey(key);
        final StringTokenizer st = new StringTokenizer(value, ",");
        int size = st.countTokens();
        if (size <= 0 || array == null) {
            return 0;
        }
        size = Math.min(size, array.length);
        for (int i = 0; i < size; ++i) {
            String field = st.nextToken().trim();
            if (field.startsWith("'") && field.endsWith("'")) {
                field = field.substring(1, field.length() - 1);
            }
            array[i] = field;
        }
        return size;
    }
    
    public String[] getStringArray(final String key) {
        return toStringArray(this.getKey(key));
    }
    
    public static String[] toStringArray(final String value) {
        final StringTokenizer st = new StringTokenizer(value, ",");
        final int size = st.countTokens();
        if (size <= 0) {
            return null;
        }
        final String[] array = new String[size];
        for (int i = 0; i < size; ++i) {
            String field = st.nextToken().trim();
            if (field.startsWith("'") && field.endsWith("'")) {
                field = field.substring(1, field.length() - 1);
            }
            array[i] = field;
        }
        return array;
    }
    
    public static LinkedList<String> toStringList(final String value) {
        final LinkedList<String> head = new LinkedList<>();
        final String[] array = toStringArray(value);
        if (array != null) {
            for (int i = 0; i < array.length; ++i) {
                head.addLast(array[i]);
            }
        }
        return head;
    }
    
    public void putStringArray(final String key, final String[] array) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(String.valueOf(key) + "=");
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    this.writer.write(", ");
                }
                this.writer.write(array[i]);
            }
            this.writer.newLine();
        }
        catch (Exception ex) {}
    }
    
    public int getIntArray(final String key, final int[] array) {
        final String value = this.getKey(key);
        final StringTokenizer st = new StringTokenizer(value, ", ");
        int size = st.countTokens();
        if (size <= 0 || array == null) {
            return 0;
        }
        size = Math.min(size, array.length);
        for (int i = 0; i < size; ++i) {
            final String field = st.nextToken();
            try {
                array[i] = Integer.parseInt(field);
            }
            catch (NumberFormatException e) {
                if (field.toLowerCase().startsWith("0x")) {
                    try {
                        array[i] = Integer.parseInt(field.substring(2), 16);
                        continue;
                    }
                    catch (NumberFormatException er) {
                        return -1;
                    }
                }
                return -1;
            }
        }
        return size;
    }
    
    public int[] getIntArray(final String key) {
        return toIntArray(this.getKey(key));
    }
    
    public static int[] toIntArray(final String value) {
        final StringTokenizer st = new StringTokenizer(value, ", ");
        final int size = st.countTokens();
        if (size <= 0) {
            return null;
        }
        final int[] array = new int[size];
        for (int i = 0; i < size; ++i) {
            final String field = st.nextToken();
            try {
                array[i] = Integer.parseInt(field);
            }
            catch (NumberFormatException e) {
                if (field.toLowerCase().startsWith("0x")) {
                    try {
                        array[i] = Integer.parseInt(field.substring(2), 16);
                        continue;
                    }
                    catch (NumberFormatException er) {
                        return null;
                    }
                }
                return null;
            }
        }
        return array;
    }
    
    public void putIntArray(final String key, final int[] array) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(String.valueOf(key) + "=");
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    this.writer.write(", ");
                }
                this.writer.write(Integer.toString(array[i]));
            }
            this.writer.newLine();
        }
        catch (Exception ex) {}
    }
    
    public int getDoubleArray(final String key, final double[] array) {
        final String value = this.getKey(key);
        try {
            final StringTokenizer st = new StringTokenizer(value, ", ");
            int size = st.countTokens();
            if (size <= 0 || array == null) {
                return 0;
            }
            size = Math.min(size, array.length);
            for (int i = 0; i < size; ++i) {
                final String field = st.nextToken();
                array[i] = Double.parseDouble(field);
            }
            return size;
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }
    
    public double[] getDoubleArray(final String key) {
        return toDoubleArray(this.getKey(key));
    }
    
    public static double[] toDoubleArray(final String value) {
        final StringTokenizer st = new StringTokenizer(value, ", ");
        final int size = st.countTokens();
        if (size <= 0) {
            return null;
        }
        final double[] array = new double[size];
        for (int i = 0; i < size; ++i) {
            final String field = st.nextToken();
            try {
                array[i] = Double.parseDouble(field);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
        return array;
    }
    
    public void putDoubleArray(final String key, final double[] array) {
        if (this.writer == null) {
            return;
        }
        try {
            this.writer.write(String.valueOf(key) + "=");
            for (int i = 0; i < array.length; ++i) {
                if (i > 0) {
                    this.writer.write(", ");
                }
                this.writer.write(Double.toString(array[i]));
            }
            this.writer.newLine();
        }
        catch (Exception ex) {}
    }
    
    public boolean hasKey(final String key) {
        final String str = (String) this.table.get(key.toUpperCase());
        if (str != null && str.length() > 0 && str.charAt(0) == '^') {
            return this.hasKey(str.substring(1));
        }
        return str != null;
    }
    
    private String getKey(final String key) {
        final String str = (String) table.get(key.toUpperCase());
        if (str != null && str.length() > 0 && str.charAt(0) == '^') {
            return getKey(str.substring(1));
        }
        return (str == null) ? key.toUpperCase() : str;
    }
    
    public void setTable(final Hashtable alt_table) {
        this.table = ((alt_table == null) ? this.table_save : alt_table);
    }
    
    public static int parseInt(final String str, final int def_val, final boolean positive) {
        int val;
        try {
            val = Integer.parseInt(str.trim());
            if (positive && val < 0) {
                val = def_val;
            }
        }
        catch (NumberFormatException e) {
            val = def_val;
        }
        return val;
    }
    
    public static double parseDouble(final String str, final double def_val, final boolean positive) {
        double val;
        try {
            val = Double.parseDouble(str.trim());
            if (positive && val < 0.0) {
                val = def_val;
            }
        }
        catch (NumberFormatException e) {
            val = def_val;
        }
        return val;
    }
    
    public static double parseAscDec(String str, final double def_val, final boolean dec) {
        final boolean negative = str.startsWith("-");
        if (negative) {
            str = str.substring(1);
        }
        final StringTokenizer st = new StringTokenizer(str, ":");
        final int size = st.countTokens();
        double degree;
        if (size == 0) {
            degree = parseDouble(str, def_val, false);
        }
        else {
            if (size != 2 && size != 3) {
                return def_val;
            }
            final int hour = parseInt(st.nextToken().trim(), 0, true);
            final int minute = parseInt(st.nextToken().trim(), 0, true);
            final int second = st.hasMoreTokens() ? parseInt(st.nextToken().trim(), 0, true) : 0;
            degree = (minute + second / 60.0) / 60.0;
            if (dec) {
                degree += hour;
            }
            else {
                degree = 15.0 * (hour + degree);
            }
        }
        if (negative) {
            degree = -degree;
        }
        return dec ? degree : City.normalizeDegree(degree);
    }
    
    public static String formatInt(final int val, final int width) {
        String str;
        for (str = Integer.toString(val); str.length() < width; str = " " + str) {}
        return str;
    }
    
    public static String formatDouble(double val, final int width, final int fraction_width, final boolean align, final boolean sign) {
        final boolean negative = val < 0.0;
        if (negative && sign) {
            val = -val;
        }
        String seq = "";
        if (align) {
            for (int i = 1; i < width; ++i) {
                seq = String.valueOf(seq) + "0";
            }
        }
        seq = String.valueOf(seq) + "0.";
        for (int i = 0; i < fraction_width; ++i) {
            seq = String.valueOf(seq) + (align ? "0" : "#");
        }
        final DecimalFormat format = new DecimalFormat(seq);
        return String.valueOf(sign ? (negative ? "-" : "+") : "") + format.format(val);
    }
    
    public static int boundNumber(int val, final int max) {
        val %= max;
        if (val < 0) {
            val += max;
        }
        return val;
    }
    
    public static boolean isAsciiString(final String key, final boolean all) {
        final char[] array = key.toCharArray();
        for (int i = 0; i < array.length; ++i) {
            if (all) {
                if (array[i] > '\u00ff') {
                    return false;
                }
            }
            else if (isAlphaDigit(array[i])) {
                return true;
            }
        }
        return all;
    }
    
    private static boolean isAlphaDigit(final char c) {
        return Character.isUpperCase(c) || Character.isLowerCase(c) || Character.isDigit(c);
    }
    
    public static String formatString(String val, final int width) {
        final char[] array = val.toCharArray();
        int remain = 2 * width;
        for (int i = 0; i < array.length; ++i) {
            --remain;
            if (array[i] > '\u00ff') {
                --remain;
            }
        }
        while (remain-- > 0) {
            val = String.valueOf(val) + " ";
        }
        return val;
    }
    
    public static int getArrayIndex(final String name, final String[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (name.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }
    
    public void dispose() {
        final Hashtable hashtable = null;
        this.table_save = hashtable;
        this.table = hashtable;
        if (this.writer != null) {
            try {
                this.writer.close();
            }
            catch (Exception ex) {}
        }
        this.string_writer = null;
    }
    
    public static void setBaseIO(final BaseIO io) {
        FileIO.base = io;
    }
    
    public static String getFileName(final String file_name) {
        return (FileIO.base == null) ? null : FileIO.base.getFileName(file_name);
    }
    
    public static URL getURL(final String file_name) {
        return (FileIO.base == null) ? null : FileIO.base.getURL(file_name);
    }
    
    public static String getTempFileName(final String suffix) {
        String file_name = null;
        try {
            final File file = File.createTempFile("Xiaxiaozheng", suffix);
            file.deleteOnExit();
            file_name = file.getAbsolutePath();
        }
        catch (IOException e) {
            return null;
        }
        return file_name;
    }
    
    public static void setTempFile(final String file_name) {
        try {
            final File file = new File(file_name);
            file.deleteOnExit();
        }
        catch (NullPointerException ex) {}
    }
    
    public static void setProgress(final int val) {
        if (FileIO.base != null) {
            FileIO.base.setProgress(val);
        }
    }
}
