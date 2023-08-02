package com.saltfun.daocalendar.base;

import java.awt.*;
import java.util.*;

public class Resource {
    public static boolean trace;
    public static final String NAME = "MartianFx";
    public static final String NUMBER = "2.00 Demo";
    public static final String COPYRIGHT_1 = "Copyright ";
    public static final String COPYRIGHT_2 = " 2019-2022 Immartian";
    public static final String DATA_EXT = "mri";
    public static final String RSRC_EXT = "prop";
    public static final String RULE_EXT = "rule";
    public static final String SIMPLIFIED_SUFFIX = "_s.";
    public static final String TRADITIONAL_SUFFIX = "_t.";
    private static final String SIMPLIFIED = "simplified";
    protected static final String DATA_PREFIX = "martian";
    private static final String CUSTOM_PREFIX = "custom";
    public static String[] DATA_EXTENSIONS;
    public static String[] RSRC_EXTENSIONS;
    public static String[] RULE_EXTENSIONS;
    public static String[] ALL_EXTENSIONS;
    private static final String[] reject_font_array;
    private static Font[] font_array;
    static private boolean simplified, alt_exclusive;
    private static int pref_changed;
    private static FileIO resource;
    static private MyPrefs prefs;

    private static Hashtable<String, String> alt_prefs;
    static private String font_name, en_font_name, pref_font_name, alt_command;
    static private int font_style, data_font_size, swt_data_font_size,
            swt_small_data_font_size;

    static public final String LOCAL_PREFIX = "local:";

    static public final int DIAGRAM_WIDTH = 640;

    static {
        trace = true;
        DATA_EXTENSIONS = new String[]{"*." + DATA_EXT};
        RSRC_EXTENSIONS = new String[]{"*." + RSRC_EXT};
        RULE_EXTENSIONS = new String[]{"*." + RULE_EXT};
        ALL_EXTENSIONS = new String[]{"*." + DATA_EXT, "*." + RSRC_EXT,
                "*." + RULE_EXT};
        reject_font_array = new String[]{"Arial Unicode MS"};
        prefs = null;
        alt_prefs = null;
    }

    public Resource(Class clss, String language, String prefer_font_name, String mod_name, String eval_name) {
        resource = null;
        prefs = (clss == null) ? null : new MyPrefs(clss);
        simplified = (hasPrefKey("simplified") ? (getPrefInt("simplified") != 0) : isSimplifiedLocale());
        if (language != null && language.equalsIgnoreCase("simplified")) {
            simplified = true;
        }
        pref_font_name = prefer_font_name;
        FileIO.setProgress(10);
        font_array = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        FileIO.setProgress(20);
        if (trace) {
            this.loadModEval(mod_name, eval_name);
        } else {
            try {
                this.loadModEval(mod_name, eval_name);
            } catch (Exception e) {
                removeModEvalPref();
            }
        }
        putPrefInt("show_eval", RuleEntry.hasRuleEntry(true) ? 1 : 0);
    }

    private void loadModEval(String mod_name, String eval_name) {
        if (hasPrefKey("modification")) {
            mod_name = getPrefString("modification");
        }
        loadModification(mod_name);
        ChartMode.setChartMode();
        final String prefix = ChartMode.isChartMode(2) ? "pick_" : "";
        if (hasPrefKey(prefix + "evaluation")) {
            eval_name = getPrefString(prefix + "evaluation");
        }
        loadEvaluation(eval_name, false);
    }

    public static void removeModEvalPref() {
        removePref("modification");
        final String prefix = ChartMode.isChartMode(ChartMode.PICK_MODE) ? "pick_" : "";
        removePref(prefix + "evaluation");
    }

    public static boolean isSimplifiedLocale() {
        return Locale.getDefault().toString().equals("zh_CN");
    }

    public static void loadModification(String mod_name) {
        if (resource != null) {
            resource.dispose();
        }
        final String lang_suffix = simplified ? SIMPLIFIED_SUFFIX : TRADITIONAL_SUFFIX;
        final String res_name = DATA_PREFIX + lang_suffix + RSRC_EXT;
        if (mod_name == null) {
            mod_name = CUSTOM_PREFIX + lang_suffix + RSRC_EXT;
        }
        RuleEntry.reset(false);
        resource = new FileIO(res_name, mod_name);
        RuleEntry.processRule();
        RuleEntry.saveTable(false);
        setDefaultFont(pref_font_name);
        font_style = getPrefInt("font_style");
        if (font_style == Integer.MIN_VALUE) {
            font_style = Font.PLAIN;
        }
        data_font_size = getPrefInt("data_font_size");
        swt_data_font_size = getPrefInt("swt_data_font_size");
        swt_small_data_font_size = getPrefInt("swt_small_data_font_size");
    }

    public static boolean loadEvaluation(String eval_name, final boolean check) {
        if (eval_name == null) {
            String lang_suffix = simplified ? SIMPLIFIED_SUFFIX : TRADITIONAL_SUFFIX;
            eval_name = DATA_PREFIX + lang_suffix + RULE_EXT;
        }
        if (check) {
            String cur_eval_name = getPrefString("evaluation_loaded");
            if (cur_eval_name.equals(eval_name)) {
                return false;
            }
        }
        putPrefString("evaluation_loaded", eval_name);
        RuleEntry.reset(true);
        final FileIO eval = new FileIO(eval_name, false);
        if (eval != null) {
            RuleEntry.processRule();
            RuleEntry.saveTable(true);
            eval.dispose();
        }
        return true;
    }

    public static boolean hasCustomData() {
        return resource.hasCustomData();
    }

    public static boolean isSimplified() {
        return simplified;
    }

    public static void setSimplified(final boolean yes) {
        putPrefInt(SIMPLIFIED, yes ? 1 : 0);
    }

    private static void setDefaultFont(String prefer_font_name) {
        if (prefer_font_name == null) {
            prefer_font_name = getPrefString("font_name");
        }
        font_name = mapFont(prefer_font_name, null);
        FileIO.setProgress(30);
        en_font_name = mapFont(getString("en_font_name"), "abc");
    }

    static private String mapFont(String name_array, String font_check)
    {
        String f_name = null;
        StringTokenizer st = new StringTokenizer(name_array, ",");
        while (st.hasMoreTokens()) {
            f_name = st.nextToken().trim();
            String fn_name = getFontNameIfAvailable(f_name);
            if (fn_name != null && isFontAcceptable(fn_name, font_check))
                return fn_name;
        }
        String[] font_name_array = getFontArray(font_check);
        return (font_name_array == null) ? f_name : font_name_array[0];
    }


    public static boolean isFontAcceptable(final String name, String font_check) {
        if (isRejectedFont(name)) {
            return false;
        }
        if (font_check == null) {
            font_check = getString("font_check");
        }
        final Font font = new Font(name, font_style, data_font_size);
        return font.canDisplayUpTo(font_check) < 0;
    }

    private static String getFontNameIfAvailable(final String name) {
        for (int i = 0; i < font_array.length; ++i) {
            if (name.equalsIgnoreCase(font_array[i].getFontName()) || name.equalsIgnoreCase(font_array[i].getFontName(Locale.ENGLISH)) || name.equalsIgnoreCase(font_array[i].getFontName(Locale.TRADITIONAL_CHINESE)) || name.equalsIgnoreCase(Resource.font_array[i].getFontName(Locale.SIMPLIFIED_CHINESE))) {
                return font_array[i].getFontName();
            }
        }
        return null;
    }

    static public String[] getPossibleFontName(String name)
    {
        for (Font font : font_array) {
            if (name.equalsIgnoreCase(font.getFontName())) {
                String[] array = new String[3];
                array[0] = font.getFontName(Locale.ENGLISH);
                array[1] = font
                        .getFontName(Locale.TRADITIONAL_CHINESE);
                array[2] = font.getFontName(Locale.SIMPLIFIED_CHINESE);
                return array;
            }
        }
        return null;
    }

    public static String[] getFontArray(String font_check) {
        if (font_check == null) {
            font_check = getString("font_check");
        }
        int size = 0;
        for (Font font : font_array) {
            if (font.canDisplayUpTo(font_check) < 0 && !isRejectedFont(font.getFontName())) {
                ++size;
            }
        }
        if (size == 0) {
            return null;
        }
        final String[] font_list = new String[size];
        size = 0;
        for (Font font : font_array) {
            if (font.canDisplayUpTo(font_check) < 0 && !isRejectedFont(font.getFontName())) {
                font_list[size++] = font.getFontName();
            }
        }
        return font_list;
    }

    private static boolean isRejectedFont(final String name) {
        for (String s : reject_font_array) {
            if (s.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static String getModName() {
        String str = getString("mod_name");
        if (str == null || str.trim().equals("")) {
            str = CUSTOM_PREFIX;
        }
        return str;
    }

    public static boolean isExclusive() {
        return alt_exclusive;
    }

    public static String getString(final String key) {
        return resource.getString(key);
    }

    public static String getProcessString(final String key) {
        return resource.getString(key).replaceAll("@", "\n");
    }

    public static int getInt(final String key) {
        return resource.getInt(key);
    }

    public static double getDouble(final String key) {
        return resource.getDouble(key);
    }

    public static int getStringArray(final String key, final String[] array) {
        return resource.getStringArray(key, array);
    }

    public static String[] getStringArray(final String key) {
        return resource.getStringArray(key);
    }

    public static LinkedList<String> getStringList(final String key) {
        return FileIO.toStringList(resource.getString(key));
    }

    public static int getIntArray(final String key, final int[] array) {
        return resource.getIntArray(key, array);
    }

    public static int[] getIntArray(final String key) {
        return resource.getIntArray(key);
    }

    public static int getDoubleArray(final String key, final double[] array) {
        return resource.getDoubleArray(key, array);
    }

    public static double[] getDoubleArray(final String key) {
        return resource.getDoubleArray(key);
    }

    public static void putPrefString(final String key, final String val) {
        if (Resource.prefs == null) {
            return;
        }
        if (Resource.alt_exclusive || hasAltKey(key)) {
            alt_prefs.put(key.toUpperCase(), val);
        } else {
            prefs.put(key, val);
        }
    }

    public static String getPrefString(final String key) {
        if (!hasPrefKey(key) && hasKey(key)) {
            return getString(key);
        }
        if (prefs == null) {
            return "?Invalid?";
        }
        if (hasAltKey(key)) {
            resource.setTable(alt_prefs);
            final String str = getString(key);
            resource.setTable(null);
            return str;
        }
        if (Resource.alt_exclusive) {
            return "?Invalid?";
        }
        return prefs.get(key, "?Invalid?");
    }

    public static boolean hasEitherKey(final String key) {
        return hasKey(key) || hasPrefKey(key);
    }

    public static boolean hasPrefKey(String key) {
        if (prefs == null) {
            return false;
        }
        if (hasAltKey(key)) {
            return true;
        }
        if (alt_exclusive) {
            return false;
        }
        return !"?Invalid?".equals(prefs.get(key, "?Invalid?"));
    }

    public static void removePref(final String key) {
        if (Resource.prefs == null) {
            return;
        }
        if (hasAltKey(key)) {
            alt_prefs.remove(key.toUpperCase());
        } else if (!Resource.alt_exclusive) {
            prefs.remove(key);
        }
    }

    public static void putPrefInt(final String key, final int val) {
        if (prefs == null) {
            return;
        }
        if (alt_exclusive || hasAltKey(key)) {
            alt_prefs.put(key.toUpperCase(), Integer.toString(val));
        }else {
            assert prefs != null;
            prefs.putInt(key, val);
        }
    }
    /**
     * 在查找颜色过程中
     * @author xiaxiaozheng
     * @date 04:37 1/16/2023
     * @param key 搜索内容
     * @return int
     **/
    public static int getPrefInt(String key) {
        if (!hasPrefInt(key) && hasKey(key)) {
            return getInt(key);  //一般会到这里
        } else if (prefs == null) {
            return Integer.MIN_VALUE;
        } else {
            if (hasAltKey(key)) {
                resource.setTable(alt_prefs);
                final int n = getInt(key);
                resource.setTable(null);
                return n;
            } else if (alt_exclusive) {
                return Integer.MIN_VALUE;
            }
            return prefs.getInt(key, Integer.MIN_VALUE);
        }
    }

    public static boolean hasPrefInt(final String key) {
        if (Resource.prefs == null) {
            return false;
        }
        if (hasAltKey(key)) {
            resource.setTable(Resource.alt_prefs);
            final int n = getInt(key);
            resource.setTable(null);
            return n != Integer.MIN_VALUE;
        }
        return !Resource.alt_exclusive && prefs.getInt(key, Integer.MIN_VALUE) != Integer.MIN_VALUE;
    }

    public static void putPrefDouble(final String key, final double val) {
        if (Resource.prefs == null) {
            return;
        }
        if (Resource.alt_exclusive || hasAltKey(key)) {
            alt_prefs.put(key.toUpperCase(), Double.toString(val));
        } else {
            prefs.putDouble(key, val);
        }
    }

    public static double getPrefDouble(final String key) {
        if (!hasPrefDouble(key) && hasKey(key)) {
            return getDouble(key);
        }
        if (Resource.prefs == null) {
            return Double.MIN_VALUE;
        }
        if (hasAltKey(key)) {
            resource.setTable(Resource.alt_prefs);
            final double d = getDouble(key);
            resource.setTable(null);
            return d;
        }
        if (Resource.alt_exclusive) {
            return Double.MIN_VALUE;
        }
        return prefs.getDouble(key, Double.MIN_VALUE);
    }

    public static boolean hasPrefDouble(final String key) {
        if (Resource.prefs == null) {
            return false;
        }
        if (hasAltKey(key)) {
            resource.setTable(Resource.alt_prefs);
            final double d = getDouble(key);
            resource.setTable(null);
            return d != Double.MIN_VALUE;
        }
        return !Resource.alt_exclusive && prefs.getDouble(key, Double.MIN_VALUE) != Double.MIN_VALUE;
    }

    public static void putPrefStringArray(final String key, final String[] data) {
        if (Resource.prefs == null || data == null) {
            return;
        }
        StringBuilder str = null;
        for (int i = 0; i < data.length; ++i) {
            if (i == 0) {
                str = Optional.ofNullable(data[i]).map(StringBuilder::new).orElse(null);
            } else {
//                str = str + "," + data[i];
                str = (str == null ? new StringBuilder("null") : str).append(",").append(data[i]);
            }
        }
        if (Resource.alt_exclusive || hasAltKey(key)) {
            alt_prefs.put(key.toUpperCase(), str == null ? null : str.toString());
        } else {
            prefs.put(key, str == null ? null : str.toString());
        }
    }

    public static String[] getPrefStringArray(final String key) {
        if (hasPrefKey(key)) {
            return FileIO.toStringArray(getPrefString(key));
        }
        return resource.getStringArray(key);
    }

    public static String[] getPrefStringArray(final String prefix, final String key) {
        final String[] array = getStringArray(hasKey(prefix + key) ? (prefix + key) : key);
        final String[] p_array = getPrefStringArray(hasPrefKey(prefix + key) ? (prefix + key) : key);
        return (p_array != null && p_array.length == array.length) ? p_array : array;
    }

    public static void putPrefIntArray(final String key, final int[] data) {
        if (prefs == null || data == null) {
            return;
        }
        StringBuilder str = null;
        for (int i = 0; i < data.length; ++i) {
            String val = Integer.toString(data[i]);
            if (i == 0) {
                str = new StringBuilder(val);
            } else {
                str.append(",").append(val);
            }
        }
        if (alt_exclusive || hasAltKey(key)) {
            alt_prefs.put(key.toUpperCase(), str == null ? null : str.toString());
        } else {
            prefs.put(key, str == null ? null : str.toString());
        }
    }

    public static int[] getPrefIntArray(final String key) {
        if (hasPrefKey(key)) {
            return FileIO.toIntArray(getPrefString(key));
        }
        return resource.getIntArray(key);
    }

    public static int[] getPrefIntArray(final String prefix, final String key) {
        final int[] array = getIntArray(hasKey(prefix + key) ? (prefix + key) : key);
        final int[] p_array = getPrefIntArray(hasPrefKey(prefix + key) ? (prefix + key) : key);
        return (p_array != null && p_array.length == array.length) ? p_array : array;
    }

    public static void putPrefDoubleArray(final String key, final double[] data) {
        if (Resource.prefs == null || data == null) {
            return;
        }
        StringBuilder str = null;
        for (int i = 0; i < data.length; ++i) {
            final String val = Double.toString(data[i]);
            if (i == 0) {
                str = val == null ? null : new StringBuilder(val);
            } else {
                str = (str == null ? new StringBuilder("null") : str).append(",").append(val);
            }
        }
        if (Resource.alt_exclusive || hasAltKey(key)) {
            alt_prefs.put(key.toUpperCase(), str == null ? null : str.toString());
        } else {
            prefs.put(key, str == null ? null : str.toString());
        }
    }

    public static double[] getPrefDoubleArray(final String key) {
        if (hasPrefKey(key)) {
            return FileIO.toDoubleArray(getPrefString(key));
        }
        return resource.getDoubleArray(key);
    }

    public static double[] getPrefDoubleArray(final String prefix, final String key) {
        final double[] array = getDoubleArray(hasKey(prefix + key) ? (prefix + key) : key);
        final double[] p_array = getPrefDoubleArray(hasPrefKey(prefix + key) ? (prefix + key) : key);
        return (p_array != null && p_array.length == array.length) ? p_array : array;
    }

    public static boolean hasKey(final String key) {
        return resource != null && resource.hasKey(key);
    }

    private static boolean hasAltKey(final String key) {
        return alt_prefs != null && alt_prefs.get(key.toUpperCase()) != null;
    }

    public static void prefClear(final boolean remove) {
        if (Resource.prefs != null) {
            prefs.clear();
            if (remove) {
                prefs = null;
            }
        }
    }

    public static boolean prefChanged() {
        return pref_changed != 0;
    }

    public static void enableAlternatePref(final boolean enable, final int changed) {
        pref_changed = changed;
        alt_prefs = (enable ? new Hashtable<>() : null);
        resetAlternate();
    }

    public static void setAlternatePref(final String data) {
        if (data == null) {
            if (Resource.pref_changed >= 0) {
                pref_changed = ((Resource.alt_prefs != null) ? 1 : 0);
            }
            alt_prefs = null;
            resetAlternate();
            return;
        }
        enableAlternatePref(true, 1);
        if (Resource.pref_changed >= 0) {
            pref_changed = 1;
        }
        final StringTokenizer st = new StringTokenizer(data, "|");
        while (st.hasMoreTokens()) {
            final String str = st.nextToken();
            final StringTokenizer nst = new StringTokenizer(str, ":");
            if (nst.countTokens() >= 2) {
                final String key = nst.nextToken().trim().toUpperCase();
                StringBuilder info = new StringBuilder(nst.nextToken().trim());
                while (nst.hasMoreTokens()) {
                    info.append(":").append(nst.nextToken().trim());
                }
                if (key.equalsIgnoreCase("exclusive")) {
                    alt_exclusive = !info.toString().equals("0");
                } else if (key.equalsIgnoreCase("command")) {
                    alt_command = info.toString();
                } else {
                    alt_prefs.put(key, info.toString());
                }
            }
        }
    }

    private static void resetAlternate() {
        alt_exclusive = false;
        alt_command = null;
    }

    public static boolean hasAlternatePref() {
        return alt_prefs != null;
    }

    public static String getAlternateCommand() {
        return alt_command;
    }

    public static String getAlternatePref() {
        if (Resource.alt_prefs == null || (Resource.alt_prefs.isEmpty() && !Resource.alt_exclusive)) {
            return null;
        }
        StringBuilder data = Optional.ofNullable(alt_exclusive ? "exclusive:1" : null).map(StringBuilder::new).orElse(null);
        final Enumeration<String> e = alt_prefs.keys();
        while (e.hasMoreElements()) {
            final String key = e.nextElement();
            final String val = alt_prefs.get(key);
            if (data == null) {
                data = new StringBuilder(key.toLowerCase() + ":" + val);
            } else {
                data.append("|").append(key.toLowerCase()).append(":").append(val);
            }
        }
        if (Resource.alt_command != null) {
            data = (data == null ? new StringBuilder("null") : data).append("|command:").append(alt_command);
        }
        return data == null ? null : data.toString();
    }

    public static void dispose() {
        resource.dispose();
        resource = null;
    }

    public static boolean hasCustomFootnote() {
        return hasPrefKey("footnote");
    }

    public static String getFootnote() {
        if (hasPrefKey("footnote")) {
            return getPrefString("footnote");
        } else {
            return Resource.NAME + " version " + Resource.NUMBER + ", "
                    + COPYRIGHT_1 + Resource.getString("copyright")
                    + COPYRIGHT_2;
        }
    }

    public static void setFootnote(final String str) {
        if (str != null && !str.equals("")) {
            putPrefString("footnote", str);
        } else {
            removePref("footnote");
        }
    }

    public static String getFontName() {
        return font_name;
    }

    public static String getEnFontName() {
        return en_font_name;
    }

    public static void setFontName(final String name) {
        putPrefString("font_name", font_name = name);
    }

    public static int getFontStyle() {
        return font_style;
    }

    public static void setFontStyle(final int style) {
        putPrefInt("font_style", font_style = style);
    }

    public static int getDataFontSize() {
        return data_font_size;
    }

    public static int getSwtDataFontSize() {
        return swt_data_font_size;
    }

    public static int getSwtSmallDataFontSize() {
        return swt_small_data_font_size;
    }

    public static void setSwtDataFontSize(final int size) {
        putPrefInt("swt_data_font_size", swt_data_font_size = size);
    }

    public static String preFilled(String str, final int width, final String fill) {
        StringBuilder strBuilder = new StringBuilder(str);
        while (strBuilder.length() < width) {
            strBuilder.insert(0, fill);
        }
        str = strBuilder.toString();
        return str;
    }

    public static String spacePreFilled(final String str, final int width) {
        return preFilled(str, width, " ");
    }

    public static String postFilled(String str, final int width, final String fill) {
        StringBuilder strBuilder = new StringBuilder(str);
        while (strBuilder.length() < width) {
            strBuilder.append(fill);
        }
        str = strBuilder.toString();
        return str;
    }

    static String spacePostFilled(final String str, final int width) {
        return postFilled(str, width, " ");
    }

    static String getSpaceFilled(final String key) {
        final char[] array = key.toCharArray();
        StringBuilder str = new StringBuilder();
        for (char c : array) {
            if (c > 'ÿ') {
                str.append("  ");
            } else {
                str.append(" ");
            }
        }
        return str.toString();
    }

    static public void saveMyPrefs() {
        prefs.save();
    }
}
