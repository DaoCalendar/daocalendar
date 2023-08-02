package com.saltfun.daocalendar.base;

public class ChartMode
{
    public static final int TRADITIONAL_MODE = 0;  //七政四模式
    public static final int SIDEREAL_MODE = 1;
    public static final int PICK_MODE = 2;  //天星择日
    public static final int ASTRO_MODE = 3; //占星模式
    public static final int NUM_MODE = 4;
    public static final int NATAL_MODE = 0;
    public static final int ALT_NATAL_MODE = 1;
    public static final int SOLAR_RETURN_MODE = 2;
    public static final int LUNAR_RETURN_MODE = 3;
    public static final int RELATIONSHIP_MODE = 4;
    public static final int COMPOSITE_MODE = 5;
    public static final int TRANSIT_MODE = 6;
    public static final int PRIMARY_DIRECTION_MODE = 7;
    public static final int SECONDARY_PROGRESSION_MODE = 8;
    public static final int SOLAR_ARC_MODE = 9;
    public static final int COMPARISON_MODE = 10;
    public static final int NUM_ASTRO_MODE = 11;
    public static final int MOUNTAIN_MODE = 0;
    public static final int ZODIAC_MODE = 1;
    private static int chart_mode;
    private static int astro_mode;
    private static boolean single_wheel_mode;
    
    public static void initChartMode() {
        ChartMode.chart_mode = Resource.getPrefInt("chart_mode");
        if (ChartMode.chart_mode < 0 || ChartMode.chart_mode >= NUM_MODE) {
            ChartMode.chart_mode = TRADITIONAL_MODE;
        }
        ChartMode.astro_mode = NATAL_MODE;
        ChartMode.single_wheel_mode = false;
    }
    
    public static boolean hasChartMode() {
        return Resource.hasPrefInt("chart_mode");
    }
    
    public static void setChartMode(final int mode) {
        Resource.putPrefInt("chart_mode", ChartMode.chart_mode = mode);
    }
    
    public static void setChartMode() {
        ChartMode.chart_mode = Resource.getPrefInt("chart_mode");
    }
    
    public static int getChartMode() {
        return ChartMode.chart_mode;
    }
    
    public static int modeToGroup(final int mode) {
        return (mode == 1) ? 0 : mode;
    }
    
    public static boolean isChartMode(final int mode) {
        return getChartMode() == mode;
    }
    
    public static void setAstroMode(final int mode) {
        ChartMode.astro_mode = mode;
    }
    
    public static int getAstroMode() {
        return ChartMode.astro_mode;
    }
    
    public static boolean isAstroMode(final int mode) {
        return ChartMode.astro_mode == mode;
    }
    
    public static boolean isAstroDualRingMode() {
        return !ChartMode.single_wheel_mode && ChartMode.astro_mode >= 6;
    }
    
    public static void setSingleWheelMode(final boolean set) {
        ChartMode.single_wheel_mode = (set && hasSingleWheelMode());
    }
    
    public static boolean isSingleWheelMode() {
        return ChartMode.single_wheel_mode;
    }
    
    public static boolean hasSingleWheelMode() {
        return ChartMode.astro_mode == 8;
    }
    
    public static boolean isMultipleMode(final boolean extended) {
        return isMultipleMode(ChartMode.astro_mode, extended);
    }
    
    public static boolean isMultipleMode(final int mode, final boolean extended) {
        return mode == 10 || mode == 4 || mode == 5 || (extended && mode == 1);
    }
    
    public static boolean isReturnMode() {
        return ChartMode.astro_mode == 2 || ChartMode.astro_mode == 3;
    }
    
    public static String getModeName() {
        final boolean show_gauquelin = ChartData.getData().getShowGauquelin();
        final String[] chart_names = Resource.getStringArray(show_gauquelin ? "gauquelin_mode_name" : "astro_mode_name");
        return chart_names[ChartMode.astro_mode];
    }
    
    public static String getModePrefix() {
        final String[] chart_prefix = Resource.getStringArray("astro_mode_prefix");
        final String prefix = chart_prefix[ChartMode.astro_mode];
        return prefix.equalsIgnoreCase("x") ? "" : prefix;
    }
    
    public static boolean hasReturnRingMode() {
        return ChartMode.astro_mode <= LUNAR_RETURN_MODE && ChartMode.astro_mode != ALT_NATAL_MODE;
    }
    
    public static String getModeName(final boolean label, final boolean full) {
        switch (getChartMode()) {
            case 1: {
                return Resource.getString("sidereal_mode");
            }
            case 2: {
                if (label) {
                    String str = null;
                    if (full) {
                        final boolean sidereal_mode = Resource.getPrefInt("pick_sidereal_mode") != 0;
                        final boolean house_mode = Resource.getPrefInt("pick_house_mode") != 0;
                        final boolean adjust_mode = Resource.getPrefInt("pick_adjust_mode") != 0;
                        if (sidereal_mode) {
                            str = Resource.getString("sidereal_mode");
                        }
                        else {
                            str = (house_mode ? Resource.getString(adjust_mode ? "ancient_adjust_house_mode" : "ancient_house_mode") : null);
                        }
                    }
                    return Resource.getString("pick_chart") + ((str == null) ? "" : (" - " + str));
                }
                return null;
            }
            case 3: {
                return getModeName() + Resource.getString("chart_char");
            }
            default: {
                final boolean house_mode2 = Resource.getPrefInt("house_mode") != 0;
                final boolean adjust_mode2 = Resource.getPrefInt("adjust_mode") != 0;
                if (label) {
                    return house_mode2 ? Resource.getString(adjust_mode2 ? "ancient_adjust_house_mode" : "ancient_house_mode") : null;
                }
                return Resource.getString(house_mode2 ? (adjust_mode2 ? "tropical_ancient_adjust_house_mode" : "tropical_ancient_house_mode") : "tropical_mode");
            }
        }
    }
    
    public static String getModeTitle() {
        if (Resource.hasPrefKey("alternate_title")) {
            return Resource.getPrefString("alternate_title");
        }
        if (!Resource.hasPrefInt("chart_mode")) {
            return Resource.getString("dialog_mode_selection");
        }
        switch (Resource.getPrefInt("chart_mode")) {
            case 2: {
                return Resource.getString("pick_title");  //天星择日
            }
            case 3: {
                return Resource.getString("western_title");
            }
            default: {
                return Resource.getString("eastern_title");
            }
        }
    }
    
    public static String getSystemName(final String prefix) {
        final String[] systems = Resource.getStringArray("house_system");
        final int index = Resource.getPrefInt(prefix + "house_system_index");
        return Resource.getString("house_system_name") + ":" + systems[index];
    }
    
    public static String getComputationMethod() {
        final boolean topo = ChartData.getData().getShowHoriz() || Resource.getPrefInt("topocentric") != 0;
        return Resource.getString(topo ? "topocentric_method" : "geocentric_method");
    }
    
    public static String getSidrealSystem() {
        if (ChartMode.chart_mode != 3 || Resource.getPrefInt("astro_system_mode") == 0) {
            return null;
        }
        final String[] systems = Resource.getStringArray("astro_sidereal_system");
        final int index = Resource.getPrefInt("astro_sidereal_index");
        return Resource.getString("sidereal_mode") + ":" + systems[index];
    }
    
    public static int getDegreeMode(final boolean quick_azimuth) {
        int degree_mode = Resource.getPrefInt("degree_mode");
        if (quick_azimuth) {
            degree_mode = 1;
        }
        return degree_mode;
    }
    
    public static boolean mountainBased(final int degree_mode) {
        return degree_mode == MOUNTAIN_MODE;
    }
}
