package com.saltfun.daocalendar.base;

import swisseph.*;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.*;

public class Calculate
{
    public static final int SE_START = 1000;
    public static final int SE_FORTUNE = 1000;
    public static final int SE_ASC = 1001;
    public static final int SE_MC = 1002;
    public static final int SE_END = 1002;
    public static final int SPEED_NORMAL = 0;
    public static final int SPEED_REVERSE = 1;
    public static final int SPEED_ECLIPSE = 2;
    public static final int SPEED_STATIONARY = 3;
    public static final int SPEED_INVISIBLE = 4;
    public static final int SPEED_SLOW = 5;
    public static final int SPEED_FAST = 6;
    public static final double TRANSIT_INC = 1.0;
    public static final double DEGREE_PRECISION = 1.0;
    public static final double HALF_DEGREE_PRECISION = 0.5;
    public static final double QUARTER_DEGREE_PRECISION = 0.25;
    public static final double MAX_SPEED = 1.0;
    static public final double MINUTE = 1.0 / (24.0 * 60.0);
    static public final double HALF_MINUTE = 0.5 * MINUTE;
    public static final double INVALID = Double.MIN_VALUE;
    private static final int SWITCH_YEAR = 1582;
    private static final int SWITCH_MONTH = 10;
    private static final int SWITCH_DAY = 15;
    private static final double SWITCH_UT = 2299160.5;
    private static final double BEIJING_TIME_OFFSET = 0.3333333333333333;
    private final double MIN_UT = -247640.0;
    private final double MAX_UT = 3690082.0;
    private final double ONE_HOUR = 0.041666666666666664;
    private final double REJECT_DEGREE_PRECISION = 1.0;
    private final double TO_SIDEREAL_SPEED = 0.9972695663290739;
    private final double TO_SIDEREAL_OFFSET = 0.002770193239802983;
    private final double JUMP_SPEED = 1.2;
    private final double JUMP_PERIOD = 30.0;
    private final double TIME_PERIOD = 1.05;
    private final double TIME_PRECISION = 6.613756613756613E-4;
    private final double PERIOD_RANGE = 0.5;
    private final double TRUE_NODE_AVERAGE_SPEED = -0.05299;
    private final double NEWTON_DEGREE_PRECISION = 0.01;
    private final int NEWTON_MAX_ITER = 100;
    private int[] sidereal_systems;
    private int ephe_flag;
    private int house_system_index;
    private double julian_day_ut;
    private double julian_day;
    private double mountain_offset;
    private static SweDate jdate;
    private SwissEph eph;
    private boolean computed;
    private boolean leap_month;
    private boolean day_fortune_mode;
    private boolean sidereal_mode;
    private double sun_pos;
    private double moon_pos;
    private double[] stationary_gap;
    private double[] invisible_gap;
    private double[] slow_speed;
    private double[] fast_speed;
    private double[] location;
    private double[] computation;
    private double[] ascmc;
    private double[] azimuth;
    private double[] pheno;
    private double[] orbit_data;
    private double[] temp_cusp;
    private String[] zodiac;
    private String[] full_zodiac;
    private String[] zodiac_name;
    private String[] speed_state;
    private String[] mountain_name;
    private String[] house_system_char;
    private String correction_key;
    private boolean equatorial_orbit;
    private int orbit_body;
    private Hashtable load_table;
    
    static {
        Calculate.jdate = new SweDate();
    }
    
    public Calculate() {
        sidereal_systems = new int[] { 0, 1, 7, 3, 8, 2, 4, 5, 6, 7, 15, 16, 9, 10, 11, 13, 12 };
        location = new double[3];
        computation = new double[6];
        ascmc = new double[10];
        azimuth = new double[3];
        pheno = new double[20];
        orbit_data = new double[4];
        temp_cusp = new double[13];
        load_table = new Hashtable();
        eph = new SwissEph();
        final double n = Double.MIN_VALUE;
        julian_day_ut = n;
        julian_day = n;
        computed = false;
        ephe_flag = SweConst.SEFLG_SWIEPH;
        location[0] = location[1] = location[2] = 0.0;
        setLocation(location);
    }
    
    public void loadResource() {
        zodiac = Resource.getStringArray("zodiac");
        full_zodiac = Resource.getStringArray("full_zodiac");
        speed_state = Resource.getStringArray("speed_state");
        house_system_char = Resource.getStringArray("house_system_char");
        zodiac_name = Resource.getStringArray(ChartMode.isChartMode(3) ? "astro_zodiac_marker" : "zodiac_marker");
        mountain_name = Resource.getStringArray("mountain_signs");
        stationary_gap = Resource.getDoubleArray("stationary_gap");
        invisible_gap = Resource.getDoubleArray("invisible_gap");
        slow_speed = Resource.getDoubleArray("slow_speed");
        fast_speed = Resource.getDoubleArray("fast_speed");
        correction_key = Resource.getString("fixstar_equ_adjustments");
        setTopocentricMode(false, false);
    }
    
    public void setEphMode(final boolean use_moseph) {
        ephe_flag &= 0xFFFFFFF9;
        ephe_flag |= (use_moseph ? 4 : 2);
        if (use_moseph) {
            eph.initSwephMosh();
        }
    }
    
    public boolean getEphMode() {
        return (ephe_flag & 0x4) == 0x4;
    }
    
    public void setTopocentricMode(final boolean override, final boolean val) {
        final boolean set = override ? val : (Resource.getPrefInt("topocentric") != 0);
        if (set) {
            ephe_flag |= 0x8000;
            location[2] = Resource.getPrefInt("altitude");
        }
        else {
            ephe_flag &= 0xFFFF7FFF;
            location[2] = 0.0;
        }
    }
    
    public void setChartMode() {
        if (ChartMode.isChartMode(3) && Resource.getPrefInt("astro_system_mode") != 0) {
            sidereal_mode = true;
            eph.swe_set_sid_mode(sidereal_systems[Resource.getPrefInt("astro_sidereal_index")], 0.0, 0.0);
        }
        else if (ChartMode.isChartMode(1) || (ChartMode.isChartMode(2) && Resource.getPrefInt("pick_sidereal_mode") != 0)) {
            sidereal_mode = true;
            if (Resource.hasKey("ayanamsha_base_degree") && Resource.hasKey("ayanamsha_base_date")) {
                eph.swe_set_sid_mode(255, getJulianDayUT(Resource.getIntArray("ayanamsha_base_date")), Resource.getDouble("ayanamsha_base_degree"));
            }
            else {
                eph.swe_set_sid_mode(0, 0.0, 0.0);
            }
        }
        else {
            sidereal_mode = false;
        }
        zodiac_name = Resource.getStringArray(ChartMode.isChartMode(3) ? "astro_zodiac_marker" : "zodiac_marker");
    }
    
    public double getAyanamsha() {
        if (Resource.hasKey("ayanamsha_base_degree") && Resource.hasKey("ayanamsha_base_date")) {
            final double offset = Resource.getDouble("ayanamsha_base_degree");
            eph.swe_set_sid_mode(255, getJulianDayUT(Resource.getIntArray("ayanamsha_base_date")), offset);
            return eph.swe_get_ayanamsa_ut(julian_day_ut);
        }
        return 0.0;
    }
    
    public boolean setJulianDay(final int[] date) {
        boolean success = setJulianDay(getJulianDayUT(date));
        if (julian_day_ut < -247640.0 || julian_day_ut > 3690082.0) {
            success = false;
        }
        return success;
    }
    
    public boolean setJulianDay(final double jd_ut) {
        computed = false;
        julian_day_ut = jd_ut;
        if (julian_day_ut != Double.MIN_VALUE) {
            julian_day = julian_day_ut + SweDate.getDeltaT(julian_day_ut);
            return true;
        }
        julian_day = Double.MIN_VALUE;
        return false;
    }
    
    public static double getJulianDayFromUT(final double ut) {
        return ut + SweDate.getDeltaT(ut);
    }
    
    public static double getJulianDayUT(final int[] date) {
        final double d_hour = date[3] + date[4] / 60.0;
        final boolean cal_type = date[0] >= SWITCH_YEAR  && (date[0] != SWITCH_YEAR  || (date[1] >= SWITCH_MONTH  && (date[1] != SWITCH_MONTH  || date[2] >= SWITCH_DAY )));
        if (Calculate.jdate.checkDate(date[0], date[1], date[2], d_hour, cal_type)) {
            return SweDate.getJulDay(date[0], date[1], date[2], d_hour, cal_type);
        }
        return INVALID;
    }
    
    public static void getDateFromJulianDayUT(final double jd_ut, final int[] date) {
//        Calculate.jdate.setCalendarType(jd_ut >= 2299160.5, false);
        jdate.setCalendarType((jd_ut < SWITCH_UT) ? SweDate.SE_JUL_CAL
                : SweDate.SE_GREG_CAL, SweDate.SE_KEEP_JD);
        Calculate.jdate.setJulDay(jd_ut + HALF_MINUTE);
        date[0] = Calculate.jdate.getYear();
        date[1] = Calculate.jdate.getMonth();
        date[2] = Calculate.jdate.getDay();
        final double hour = Calculate.jdate.getHour();
        date[3] = (int)Math.floor(hour);
        date[4] = (int)Math.floor(60.0 * (hour - date[3]));
    }
    
    public double getLATDateFromDate(final int[] date) {
        if ((ephe_flag & SweConst.SEFLG_MOSEPH) == SweConst.SEFLG_MOSEPH) {
            return 0.0;
        }
        double ut = getJulianDayUT(date);
        final StringBuilder error = new StringBuilder();
        final DblObj diff = new DblObj();
        double[] E = new double[1];
//        tjd_lmt0 = tjd_lmt - geolon / 360.0;
//        double E[] = new double[(int) diff.val];
        if (E.length > 0) {
            eph.swe_time_equ(ut, E, error);
        }else {
            return 1440.0 * diff.val;
        }
        return 1440.0 * diff.val;
    }
    
    public static void computeMidPoint(final DataEntry a, final DataEntry b, final DataEntry r) {
        int[] date_buf = a.getBirthDay();
        String zone = a.getZone();
        BaseCalendar.addZoneOffset(zone, date_buf, 0, false);
        final double a_ut = getJulianDayUT(date_buf);
        final double[] a_val = new double[2];
        City.parseLongLatitude(a.getCity(), a.getCountry(), a_val);
        date_buf = b.getBirthDay();
        zone = b.getZone();
        BaseCalendar.addZoneOffset(zone, date_buf, 0, false);
        final double b_ut = getJulianDayUT(date_buf);
        final double[] b_val = new double[2];
        City.parseLongLatitude(b.getCity(), b.getCountry(), b_val);
        getDateFromJulianDayUT(0.5 * (a_ut + b_ut), date_buf);
        r.setBirthDay(date_buf);
        r.setCity(City.formatLongLatitude(0.5 * (a_val[0] + b_val[0]), true, true, false) + ", " + City.formatLongLatitude(0.5 * (a_val[1] + b_val[1]), false, true, false));
        r.setCountry(City.getUnknownCountry());
        r.setZone("GMT");
    }
    
    public double getJulianDayUT() {
        return julian_day_ut;
    }
    
    public double getJulianDay() {
        return julian_day;
    }
    
    public void setLocation(final double[] loc) {
        location[0] = loc[0];
        location[1] = loc[1];
        eph.swe_set_topo(location[0], location[1], location[2]);
    }
    
    public void setLocation(final double longitude, final double latitude) {
        location[0] = longitude;
        location[1] = latitude;
        eph.swe_set_topo(location[0], location[1], location[2]);
    }
    
    public void getLocation(final double[] loc) {
        loc[0] = location[0];
        loc[1] = location[1];
    }
    
    public double getLongitude() {
        return location[0];
    }
    
    public double getLatitude() {
        return location[1];
    }
    
    public int getDifferenceInDays(final int[] from_date, final int[] to_date) {
        return (int)Math.rint(getJulianDayUT(to_date) - getJulianDayUT(from_date));
    }
    
    public double compute(final double jd_ut, final int body) {
        final double ut_sav = julian_day_ut;
        julian_day_ut = jd_ut;
        final double val = compute(body);
        julian_day_ut = ut_sav;
        return val;
    }
    
    public double compute(final int body) {
        if (body >= SE_START && body <= SE_END) {
            return computeSpecial(body);
        }
        if (body < 0) {
            return computeOrbit();
        }
        final StringBuilder error = new StringBuilder();
        int i_flag = ephe_flag | SweConst.SEFLG_SPEED;
        int o_flag = SweConst.ERR;
        if (sidereal_mode) {
            i_flag |= SweConst.SEFLG_SIDEREAL;
        }
        for (int iter = 0; iter < 2; ++iter) {
            try {
                o_flag = eph.swe_calc_ut(julian_day_ut, body, i_flag, computation, error);
            }
            catch (SwissephException e) {
                final String index = getEphIndex(e.getMessage());
                if (index != null && loadEphIndex(index)) {
                    return compute(body);
                }
                return computation[0] = Double.MIN_VALUE;
            }
            if (o_flag != SweConst.NOT_AVAILABLE) {
                break;
            }
            if (iter == 0) {
                final String index2 = getEphIndex(error.toString());
                if (index2 == null || !loadEphIndex(index2)) {
                    return computation[0] = Double.MIN_VALUE;
                }
            }
        }
        if (!(computed = (o_flag != -1))) {
            computation[0] = Double.MIN_VALUE;
        }
        return computation[0];
    }
    
    public double computeGauquelin(final int body) {
        final StringBuilder error = new StringBuilder();
        int i_flag = ephe_flag | SweConst.SEFLG_SPEED;
        int o_flag = -1;
        if (sidereal_mode) {
            i_flag |= SweConst.SEFLG_SIDEREAL;
        }
        double val = Double.MIN_VALUE;
        for (int iter = 0; iter < 2; ++iter) {
            try {
                final DblObj pos = new DblObj();
                o_flag = eph.swe_gauquelin_sector(julian_day_ut, body, null, i_flag, 3, location, 0.0, 20.0, pos, error);
                val = pos.val;
            }
            catch (SwissephException e) {
                final String index = getEphIndex(e.getMessage());
                if (index != null && loadEphIndex(index)) {
                    return computeGauquelin(body);
                }
                return Double.MIN_VALUE;
            }
            if (o_flag != -2) {
                break;
            }
            if (iter == 0) {
                final String index2 = getEphIndex(error.toString());
                if (index2 == null || !loadEphIndex(index2)) {
                    return Double.MIN_VALUE;
                }
            }
        }
        if (o_flag == -1) {
            val = Double.MIN_VALUE;
        }
        return val;
    }
    
    public double computePheno(final int body) {
        final StringBuilder error = new StringBuilder();
        int i_flag = ephe_flag | 0x100;
        int o_flag = -1;
        if (sidereal_mode) {
            i_flag |= SweConst.SEFLG_SIDEREAL;
        }
        pheno[3] = Double.MIN_VALUE;
        for (int iter = 0; iter < 2; ++iter) {
            try {
                o_flag = eph.swe_pheno_ut(julian_day_ut, body, i_flag, pheno, error);
            }
            catch (SwissephException e) {
                final String index = getEphIndex(e.getMessage());
                if (index != null && loadEphIndex(index)) {
                    return computePheno(body);
                }
                return Double.MIN_VALUE;
            }
            if (o_flag != -2) {
                break;
            }
            if (iter == 0) {
                final String index2 = getEphIndex(error.toString());
                if (index2 == null || !loadEphIndex(index2)) {
                    return Double.MIN_VALUE;
                }
            }
        }
        if (o_flag == -1) {
            pheno[3] = Double.MIN_VALUE;
        }
        return pheno[3];
    }
    
    public void setOrbitData(final double speed, final double base_date, final double base_degree) {
        orbit_data[0] = speed;
        orbit_data[1] = base_date;
        orbit_data[2] = base_degree;
        orbit_data[3] = 0.0;
        equatorial_orbit = false;
    }
    
    public void setEquOrbitData(final int body, final double speed, final double base_date, final double base_azimuth, final double base_altitude) {
        orbit_body = body;
        orbit_data[0] = speed * 0.9972695663290739;
        orbit_data[1] = base_date;
        azimuth[0] = base_azimuth;
        azimuth[1] = base_altitude;
        eph.swe_azalt_rev(base_date, 1, location, azimuth, computation);
        orbit_data[2] = computation[0];
        orbit_data[3] = computation[1];
        equatorial_orbit = true;
    }
    
    private double computeOrbit() {
        final double day = getJulianDayUT();
        if (day != Double.MIN_VALUE) {
            computed = true;
            final double offset = orbit_data[0] * (day - orbit_data[1]);
            double degree = orbit_data[2] + offset;
            if (equatorial_orbit) {
                final double ut = orbit_data[1] - 0.002770193239802983 * offset;
                compute(ut, orbit_body);
                eclToEqu(ut);
                degree = computation[0] + offset;
            }
            if (sidereal_mode) {
                degree -= eph.swe_get_ayanamsa_ut(day);
            }
            computation[0] = City.normalizeDegree(degree);
        }
        else {
            computed = false;
            computation[0] = Double.MIN_VALUE;
        }
        computation[1] = orbit_data[3];
        computation[2] = 1.0;
        if (equatorial_orbit) {
            equToEcl(day);
        }
        computation[3] = orbit_data[0];
        return computation[0];
    }
    
    private double getSpeed() {
        return computed ? computation[3] : 0.0;
    }
    
    public int getEclipseState(final boolean sun) throws IOException {
        if (sun) {
            final LinkedList head = computeSolarEclipse(julian_day_ut - ONE_HOUR, julian_day_ut + ONE_HOUR, true, false, false);
            if (!head.isEmpty()) {
                return SPEED_ECLIPSE;
            }
        }
        else {
            final LinkedList head = computeLunarEclipse(julian_day_ut - 0.5, julian_day_ut + 0.5, true, false);
            if (!head.isEmpty()) {
                return SPEED_ECLIPSE;
            }
        }
        return 0;
    }
    
    public int getSpeedState( int body,  int index) {
         double pos = computation[0];
         double speed = computation[3];
         double ut_sav = julian_day_ut;
        julian_day_ut = computeSpeedTransit(body, ut_sav, 0.0, true);
        if (julian_day_ut == Double.MIN_VALUE) {
            julian_day_ut = ut_sav;
            return getSpeedState(speed);
        }
         double p_pos = compute(body);
        if (getDegreeGap(pos, p_pos) <= stationary_gap[index]) {
             double p_ut = julian_day_ut;
            julian_day_ut = computeTransit(body, ut_sav, City.normalizeDegree(pos + 180.0), true, true);
            if (julian_day_ut > ut_sav || julian_day_ut < p_ut) {
                julian_day_ut = ut_sav;
                return SPEED_STATIONARY;
            }
        }
        julian_day_ut = computeSpeedTransit(body, ut_sav, 0.0, false);
        if (julian_day_ut == Double.MIN_VALUE) {
            julian_day_ut = ut_sav;
            return getSpeedState(speed);
        }
         double n_pos = compute(body);
        if (getDegreeGap(pos, n_pos) <= stationary_gap[index]) {
             double n_ut = julian_day_ut;
            julian_day_ut = computeTransit(body, ut_sav, City.normalizeDegree(pos + 180.0), true, false);
            if (julian_day_ut < ut_sav || julian_day_ut > n_ut) {
                julian_day_ut = ut_sav;
                return SPEED_STATIONARY;
            }
        }
        julian_day_ut = ut_sav;
        if (getDegreeGap(pos, sun_pos) <= invisible_gap[index]) {
            return SPEED_INVISIBLE;
        }
        if (speed < 0.0) {
            return SPEED_REVERSE;
        }
        if (speed >= fast_speed[index]) {
            return SPEED_FAST;
        }
        if (speed <= slow_speed[index]) {
            return SPEED_SLOW;
        }
        return SPEED_NORMAL;
    }
    
    public static double getDegreeGap( double pos1,  double pos2) {
        double gap = Math.abs(pos1 - pos2);
        if (gap > 180.0) {
            gap = 360.0 - gap;
        }
        return gap;
    }
    
    public int getSpeedState() {
        return getSpeedState(getSpeed());
    }
    
    private int getSpeedState( double speed) {
        return (speed < 0.0) ? SPEED_REVERSE : SPEED_NORMAL;
    }
    
    public String getSpeedStateName(final int state, final String blank) {
        return (state == SPEED_NORMAL) ? blank : speed_state[state];
    }
    
    public String[] getSpeedStateNameArray() {
        return speed_state;
    }
    
    public void setHouseSystemIndex(final int index) {
        house_system_index = index;
    }
    
    public void computeHouses(final double[] cusps) {
        computeHouses(cusps, julian_day_ut);
    }
    
    public void computeHouses(final double[] cusps, final double ut) {
        int i_flag = ephe_flag;
        if (sidereal_mode) {
            i_flag |= SweConst.SEFLG_SIDEREAL;
        }
        if (eph.swe_houses(ut, i_flag, location[1], location[0], house_system_char[house_system_index].charAt(0), cusps, ascmc) != 0) {
            ascmc[0] = (ascmc[1] = Double.MIN_VALUE);
        }
    }
    
    public void computeHousesFromMidHeaven(final double[] cusps, final double midheaven) {
        final double ut_save = julian_day_ut;
        while (true) {
            computeHouses(cusps);
            double delta = cusps[10] - midheaven;
            if (delta >= 180.0) {
                delta = 360.0 - delta;
            }
            else if (delta <= -180.0) {
                delta += 360.0;
            }
            final double d_ut = delta / 360.0;
            if (Math.abs(d_ut) <= 3.4722222222222224E-4) {
                break;
            }
            julian_day_ut -= d_ut;
        }
        julian_day_ut = ut_save;
    }
    
    public int[][] computeAspects(final double[] f_pos, final double[] t_pos, final double[] aspects_degree, final double[] aspects_tolerance) {
        final int[][] aspects = new int[f_pos.length][t_pos.length];
        for (int i = 0; i < f_pos.length; ++i) {
            if (f_pos[i] != Double.MIN_VALUE) {
                for (int j = 0; j < t_pos.length; ++j) {
                    if (t_pos[j] != Double.MIN_VALUE) {
                        if (i != j || f_pos != t_pos) {
                            final double angle = getDegreeGap(f_pos[i], t_pos[j]);
                            for (int k = 0; k < aspects_tolerance.length; ++k) {
                                if (Math.abs(angle - aspects_degree[k]) <= aspects_tolerance[k]) {
                                    aspects[i][j] = k + 1;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return aspects;
    }
    
    public void initSpecial(final double sun_long, final double moon_long, final boolean day) {
        sun_pos = sun_long;
        moon_pos = moon_long;
        day_fortune_mode = (day || Resource.getPrefInt("night_fortune_mode") == 0);
    }
    
    private double computeSpecial(int body) {
        computation[0] = Double.MIN_VALUE;
        computation[1] = computation[2] = computation[3] = 0.0;
        switch (body) {
            case 1001: {
                computation[0] = ascmc[0];
                break;
            }
            case 1002: {
                computation[0] = ascmc[1];
                break;
            }
            case 1000: {
                if (ascmc[0] != Double.MIN_VALUE) {
                    double gap = moon_pos - sun_pos;
                    if (!day_fortune_mode) {
                        gap = -gap;
                    }
                    computation[0] = City.normalizeDegree(ascmc[0] + gap);
                    break;
                }
                break;
            }
        }
        return computation[0];
    }
    
    public double computeAzimuth( double magnetic_shift) {
        if (computation[0] == Double.MIN_VALUE) {
            return Double.MIN_VALUE;
        }
        double t = computation[0];
        if (sidereal_mode) {
            computation[0] += eph.swe_get_ayanamsa_ut(julian_day_ut);
        }
        eph.swe_azalt(julian_day_ut, 0, location, 0.0, 20.0, computation, azimuth);
        computation[0] = t;
        return azimuth[0] = City.normalizeDegree(135.0 - azimuth[0] + magnetic_shift);
    }
    
    public double computeAzimuth(final double magnetic_shift, double degree, final double[] cusps) {
        if (degree != Double.MIN_VALUE) {
            computeAzimuth(magnetic_shift);
            double ratio = 0.0;
            double last_pos = cusps[12];
            for (int i = 1; i <= 12; ++i) {
                final double pos = cusps[i];
                boolean in_house;
                if (pos > last_pos) {
                    in_house = (degree >= last_pos && degree < pos);
                    if (in_house) {
                        ratio = (degree - last_pos) / (pos - last_pos);
                    }
                }
                else {
                    in_house = (degree >= last_pos || degree < pos);
                    if (in_house) {
                        degree -= last_pos;
                        if (degree < 0.0) {
                            degree += 360.0;
                        }
                        ratio = degree / (pos - last_pos + 360.0);
                    }
                }
                if (in_house) {
                    degree = 30.0 * ratio + 180.0 + (i - 1) * 30.0 + magnetic_shift;
                    return City.normalizeDegree(degree);
                }
                last_pos = pos;
            }
        }
        return Double.MIN_VALUE;
    }
    
    public double getAltitude() {
        if (computation[0] == Double.MIN_VALUE) {
            return Double.MIN_VALUE;
        }
        return azimuth[1];
    }
    
    public double computePlanetAzimuthTransit(final int body, final double start_ut, final double degree, final double magnetic_shift, final double precision, final boolean quick_azimuth, final boolean check_speed, final boolean backward, final boolean no_warn) {
        double e;
        double s;
        if (backward) {
            e = start_ut;
            s = e - 1.05;
        }
        else {
            s = start_ut;
            e = s + 1.05;
        }
        double ref = computePlanetAzimuth(body, s, magnetic_shift, quick_azimuth);
        final double deg = (degree > ref) ? (degree - 360.0) : degree;
        do {
            final double m = 0.5 * (s + e);
            double m_val = computePlanetAzimuth(body, m, magnetic_shift, quick_azimuth);
            if (m_val > ref) {
                m_val -= 360.0;
            }
            if (Math.abs(deg - m_val) < precision) {
                return m;
            }
            if (deg > m_val) {
                e = m;
            }
            else {
                s = m;
                ref = m_val;
            }
        } while (Math.abs(s - e) >= 6.613756613756613E-4);
        return computePlanetAzimuthTransitSearch(body, start_ut, degree, magnetic_shift, precision, quick_azimuth, check_speed, no_warn);
    }
    
    private double computePlanetAzimuthTransitSearch(final int body, final double start_ut, final double degree, final double magnetic_shift, final double precision, final boolean quick_azimuth, final boolean check_speed, final boolean no_warn) {
        double s = start_ut;
        final double e = s + 1.05;
        double best_v = Double.MIN_VALUE;
        double l_gap = Double.MAX_VALUE;
        double r_gap = Double.MAX_VALUE;
        while (s < e) {
            final double val = computePlanetAzimuth(body, s, magnetic_shift, quick_azimuth);
            final double gap = val - degree;
            if (Math.abs(gap) < precision && check_speed && Math.abs(gap) < DEGREE_PRECISION && computePlanetAzimuthSpeed(body, s, quick_azimuth) > JUMP_SPEED) {
                return s;
            }
            double gap_l;
            double gap_r;
            if (gap < 0.0) {
                gap_l = gap;
                gap_r = gap + 360.0;
            }
            else {
                gap_l = gap - 360.0;
                gap_r = gap;
            }
            if (-gap_l < l_gap) {
                l_gap = -gap_l;
                if (l_gap < r_gap) {
                    best_v = s;
                }
            }
            if (gap_r < r_gap) {
                r_gap = gap_r;
                if (r_gap < l_gap) {
                    best_v = s;
                }
            }
            s += TIME_PRECISION;
        }
        if (Math.min(l_gap, r_gap) > REJECT_DEGREE_PRECISION) {
            if (!no_warn) {
                DecimalFormat format = new DecimalFormat("#.##");
                String str = Resource.getString("dialog_best_value_found")
                        + " ";
                double val = City.normalizeDegree(315.0 - (degree - l_gap));
                str += format.format(val);
                str += " " + Resource.getString("or") + " ";
                val = City.normalizeDegree(315.0 - (degree + r_gap));
                str += format.format(val);
                Message.warn(str);
            }
            return INVALID;
        }
        return best_v;
    }
    
    public double computePlanetAzimuth(final int body, final double jd_ut, final double magnetic_shift, final boolean quick_azimuth) {
        final double ut_sav = julian_day_ut;
        julian_day_ut = jd_ut;
        double val = compute(body);
        if (quick_azimuth) {
            computeHouses(temp_cusp);
            val = computeAzimuth(magnetic_shift, val, temp_cusp);
        }
        else {
            val = computeAzimuth(magnetic_shift);
        }
        julian_day_ut = ut_sav;
        return val;
    }
    
    public double computePlanetAzimuthSpeed(final int body, final double jd_ut, final boolean quick_azimuth) {
        final double l_degree = computePlanetAzimuth(body, jd_ut - HALF_MINUTE, 0.0, quick_azimuth);
        final double u_degree = computePlanetAzimuth(body, jd_ut + HALF_MINUTE, 0.0, quick_azimuth);
        double gap = l_degree - u_degree;
        if (gap < -180.0) {
            gap += 360.0;
        }
        else if (gap > 180.0) {
            gap -= 360.0;
        }
        return gap;
    }
    
    public double getLocalJulianDayUT(final String zone, final boolean noon) {
        final int[] date = new int[5];
        getDateFromJulianDayUT(julian_day_ut, date);
        if (zone == null || zone.equals(City.UNKNOWN_ZONE)) {
            BaseCalendar.formatDate(location[0], date, date, 0.0, false, true);
        }
        else {
            BaseCalendar.addZoneOffset(zone, date, 0, true);
        }
        final double jd_ut = julian_day_ut - (date[3] + date[4] / 60.0) / 24.0;
        return noon ? (jd_ut + 0.5) : jd_ut;
    }
    
    public boolean computeRiseSet(final String zone, final int body, final double[] rise_set) throws IOException {
        final double when = getLocalJulianDayUT(zone, false);
        boolean equator = false;
        final int i_flag = ephe_flag;
        rise_set[0] = (rise_set[1] = Double.MIN_VALUE);
        final StringBuilder error = new StringBuilder();
        final DblObj rise = new DblObj();
        final DblObj set = new DblObj();
        if (eph.swe_rise_trans(when, body, null, i_flag, 1, location, 0.0, 20.0, rise, error) != 0) {
            final double lat_val = location[1];
            location[1] = 0.0;
            eph.swe_rise_trans(when, body, null, i_flag, 1, location, 0.0, 20.0, rise, error);
            location[1] = lat_val;
            equator = true;
        }
        if (eph.swe_rise_trans(when, body, null, i_flag, 2, location, 0.0, 20.0, set, error) != 0) {
            final double lat_val = location[1];
            location[1] = 0.0;
            eph.swe_rise_trans(when, body, null, i_flag, 2, location, 0.0, 20.0, set, error);
            location[1] = lat_val;
            equator = true;
        }
        rise_set[0] = rise.val;
        rise_set[1] = set.val;
        return equator;
    }
    
    public boolean isDayBirth(final double[] rise_set) {
        return julian_day_ut >= rise_set[0] && julian_day_ut < rise_set[1];
    }
    
    public boolean isDayBirthByZone(int hour, int rise_hour, int set_hour) {
        hour = toZone(hour);
        rise_hour = toZone(rise_hour);
        set_hour = toZone(set_hour);
        return hour >= rise_hour && hour < set_hour;
    }
    
    private int toZone(int hour) {
        hour = (hour + 1) / 2;
        if (hour == 12) {
            hour = 0;
        }
        return hour;
    }
    
    public double computeStar(StringBuilder name_buf, String equ_key) throws IOException {
         String star_name = name_buf.toString();
         StringBuilder error = new StringBuilder();
        int i_flag = ephe_flag;
        if (sidereal_mode) {
            i_flag |= SweConst.SEFLG_SIDEREAL;
        }
        int o_flag = eph.swe_fixstar_ut(name_buf, julian_day_ut, i_flag, computation, error);
        if (o_flag == -1) {
            if (star_name.startsWith(",")) {
                try {
                    computation[0] = Double.parseDouble(star_name.substring(1));
                    if (computation[0] < 0.0 || computation[0] >= 360.0) {
                        computation[0] = Double.MIN_VALUE;
                    }
                }
                catch (NumberFormatException e) {
                    computation[0] = Double.MIN_VALUE;
                }
            }
            else {
                computation[0] = Double.MIN_VALUE;
            }
        }
        else if (equ_key != null) {
            // correction in equatorial coordinate system
            String name = equ_key + correction_key;
            if (Resource.hasKey(name)) {
                double[] correction = Resource.getDoubleArray(name);
                if (correction != null && correction.length == 2) {
                    eclToEqu(julian_day_ut);
                    computation[0] = City.normalizeDegree(computation[0] - correction[0]);
                    computation[1] -= correction[1];
                    equToEcl(julian_day_ut);
                }
            }
        }
        else {
            String name = name_buf.toString();
            name = name.replaceFirst(".*,", "");
            if (Resource.hasKey(name)) {
                final double correction2 = Resource.getDouble(name);
                if (correction2 != Double.MIN_VALUE) {
                    computation[0] = City.normalizeDegree(computation[0] - correction2);
                }
            }
        }
        return computation[0];
    }
    
    private void eclToEqu(final double ut) {
        eph.swe_azalt(ut, 0, location, 0.0, 20.0, computation, azimuth);
        eph.swe_azalt_rev(ut, 1, location, azimuth, computation);
    }
    
    private void equToEcl(final double ut) {
        eph.swe_azalt(ut, 1, location, 0.0, 20.0, computation, azimuth);
        eph.swe_azalt_rev(ut, 0, location, azimuth, computation);
    }
    // https://github.com/search?l=Markdown&q=computeSolarTerms&type=Code
    public double[] computeSolarTerms( int[] birth_date) {
         int[] date = birth_date.clone();
         double[] solar_terms = new double[26];
         int n = 0;
        --date[n];
        date[1] = 12;
        date[2] = 1;
        double when = getJulianDayUT(date);
        setTopocentricMode(true, false);
        for (int i = 0; i < 26; ++i) {
            double degree = 15.0 * i - 90.0;
            if (degree < 0.0) {
                degree += 360.0;
            }
            when = computeTransit(0, when, degree, false, false);
            if (when == Double.MIN_VALUE) {
                setTopocentricMode(false, false);
                return null;
            }
            solar_terms[i] = BeijingTime(when);
            when += 13.0;
        }
        setTopocentricMode(false, false);
        return solar_terms;
    }

    // https://github.com/jakubprzybytek/Albedo/blob/master/albedo-math/src/main/java/jp/albedo/jeanmeeus/topocentric/RiseTransitSetEventCalculator.java 67

//    starbase/src/to/tetramorph/starbase/SynodicPeriodCalcDialog.java
    private double computeTransit( int body,  double start_ut,  double degree,  boolean sidereal_adjust,  boolean backward) {
        try {
            int i_flag = ephe_flag | SweConst.SEFLG_TRANSIT_LONGITUDE;
            if (sidereal_adjust && sidereal_mode) {
                i_flag |= SweConst.SEFLG_SIDEREAL;
            }
            TransitCalculator tc = new TCPlanet(eph,body,i_flag, degree);
            //            return eph.getNextTransitUT(body, degree, i_flag, start_ut, backward);
            return eph.getTransitUT(tc, start_ut, backward);
        }
        catch (SwissephException e) {
            final String index = getEphIndex(e.getMessage());
            if (index != null && loadEphIndex(index)) {
                return computeTransit(body, start_ut, degree, sidereal_adjust, backward);
            }
            return Double.MIN_VALUE;
        }
        catch (IllegalArgumentException e2) {
            return Double.MIN_VALUE;
        }
    }
    /**
     *
     * @author xiaxiaozheng
     * @date 16:15 1/2/2023
     * @param i_body
     * @param k_body
     * @param start_ut
     * @param end_ut
     * @param offset
     * @param sidereal_adjust
     * @param backward
     * @return double
     **/
    private double computeRelativeTransit(final int i_body, final int k_body, final double start_ut, final double end_ut, final double offset, final boolean sidereal_adjust, final boolean backward) {
        try {
            int i_flag = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE;
            TransitCalculator tc = new TCPlanetPlanet(eph,i_body,k_body,i_flag, offset);
//            eph.setTransitSearchBound(end_ut);

            //            final double ut = eph.getRelativeTransitUT(k_body, i_body, offset, i_flag, start_ut, backward);
//            eph.setTransitSearchBound(0.0);
            return eph.getTransitUT( tc, start_ut, backward );
        }
        catch (SwissephException e) {
            final String index = getEphIndex(e.getMessage());
            if (index != null && loadEphIndex(index)) {
                return computeRelativeTransit(i_body, k_body, start_ut, end_ut, offset, sidereal_adjust, backward);
            }
//            eph.setTransitSearchBound(0.0);
            return computation[0] = Double.MIN_VALUE;
        }
    }
    
    public double computeSpeedTransit(final int body, final double start_ut, final double speed, final boolean backward) {
        ephe_flag |= SweConst.SEFLG_TRANSIT_SPEED;
        double ut = computeTransit(body, start_ut, speed, true, backward);
        ephe_flag &= ~SweConst.SEFLG_TRANSIT_SPEED;
        return ut;
    }
    
    public double computePlanetTransit(final int body, double start_ut, final double degree, final boolean backward) {
        final double ut_sav = julian_day_ut;
        final double start_degree = compute(body);
        final boolean period = Math.abs(start_degree - degree) < 0.5;
        double ut;
        if (!isRealPlanet(body)) {
            if (period) {
                start_ut += (backward ? -2922.0 : 2922.0);
            }
            ut = computeNewtonRapshonTransit(body, start_ut, degree, backward);
        }
        else {
            if (period) {
                start_ut += (backward ? -1.0 : 1.0);
            }
            ut = computeTransit(body, start_ut, degree, true, backward);
        }
        julian_day_ut = ut_sav;
        return ut;
    }
    
    public double computePlanetRelativeTransit(final int i_body, final int k_body, final double start_ut, final double end_ut, final double offset, final boolean backward) {
        double ut_sav = julian_day_ut;
        if (!isRealPlanet(i_body) || !isRealPlanet(k_body)) {
            return Double.MIN_VALUE;
        }
        double ut = computeRelativeTransit(i_body, k_body, start_ut, end_ut, offset, true, backward);
        julian_day_ut = ut_sav;
        return ut;
    }
    
    private boolean isRealPlanet(final int body) {
        return body >= 0 && body != 11 && body != 12;
    }
    /**
     *
     * @author xiaxiaozheng
     * @date 17:13 1/2/2023
     * @param body
     * @param start_ut
     * @param degree
     * @param backward
     * @return double
     **/
    private double computeNewtonRapshonTransit(final int body, final double start_ut, double degree, boolean backward) {
        final boolean true_node = body == 11;
        double l_ut = 0.0;
        double u_ut = 0.0;
        double l_gap = Double.MAX_VALUE;
        double u_gap = Double.MAX_VALUE;
        double init_val = Double.MIN_VALUE;
        double new_ut = Double.MIN_VALUE;
        julian_day_ut = start_ut;
        boolean bisection = false;
        for (int i = 0; i < 100; ++i) {
            double val = compute(body);
            final double speed = true_node ? TRUE_NODE_AVERAGE_SPEED : getSpeed();
            if (val == Double.MIN_VALUE) {
                break;
            }
            if (init_val == Double.MIN_VALUE) {
                if (speed < 0.0) {
                    backward = !backward;
                }
                if (backward) {
                    if (val <= degree + 0.01) {
                        degree -= 360.0;
                    }
                    init_val = val + 0.01;
                }
                else {
                    if (val >= degree - 0.01) {
                        degree += 360.0;
                    }
                    init_val = val - 0.01;
                }
            }
            if (backward) {
                if (init_val <= val) {
                    val -= 360.0;
                }
            }
            else if (init_val >= val) {
                val += 360.0;
            }
            val -= degree;
            final double gap = Math.abs(val);
            if (!bisection) {
                if (val < 0.0) {
                    if (gap < l_gap) {
                        l_gap = gap;
                        l_ut = julian_day_ut;
                    }
                    else {
                        bisection = (l_ut > 0.0 && u_ut > 0.0);
                        if (bisection) {
                            julian_day_ut = l_ut;
                        }
                    }
                }
                else if (gap < u_gap) {
                    u_gap = gap;
                    u_ut = julian_day_ut;
                }
                else {
                    bisection = (l_ut > 0.0 && u_ut > 0.0);
                    if (bisection) {
                        julian_day_ut = u_ut;
                    }
                }
            }
            if (bisection) {
                if (val < 0.0) {
                    l_ut = julian_day_ut;
                }
                else {
                    u_ut = julian_day_ut;
                }
                julian_day_ut = 0.5 * (l_ut + u_ut);
            }
            else {
                julian_day_ut -= val / speed;
            }
            if (gap <= 0.01) {
                new_ut = julian_day_ut;
                break;
            }
        }
        return new_ut;
    }
    
    public double[] computeTransit(final int body, final double start_ut, final double end_ut, final double degree) {
        final LinkedList head = new LinkedList();
        double when = start_ut;
        while (true) {
            when = computeTransit(body, when, degree, true, false);
            if (when == Double.MIN_VALUE || when >= end_ut) {
                break;
            }
            double whend = Double.parseDouble(String.valueOf(when));
            head.addLast(whend);
            ++when;
        }
        if (head.isEmpty()) {
            return null;
        }
        final double[] data = new double[head.size()];
        try {
            final ListIterator iter = head.listIterator();
            for (int i = 0; i < data.length; ++i) {
                data[i] = (double) iter.next();
            }
        }
        catch (NoSuchElementException ex) {}
        return data;
    }
    
//    public LinkedList findStarByEquPos(final double[] pos) {
//        LinkedList head = new LinkedList();
//        StringBuilder error = new StringBuilder();
//        for (int i = 1;; i++) {
//            StringBuilder name_buf = new StringBuilder(Integer.toString(i));
//            int o_flag = eph.swe_fixstar_ut(name_buf, julian_day_ut, ephe_flag,
//                    computation, error);
//            if (o_flag == SweConst.ERR)
//                break;
//            eclToEqu(julian_day_ut);
//            double dx = getDegreeGap(pos[0], computation[0]);
//            double dy = pos[1] - computation[1];
//            head.add(new StarEntry(eph.swe_last_fixstar_entry(), dx * dx + dy
//                    * dy));
//        }
//        if (head.isEmpty())
//            return null;
//        Collections.sort(head);
//        return head;
//    }
    
    public String[] getStarEquPosData(final double[] pos, final Object obj) throws IOException {
        final StarEntry entry = (StarEntry)obj;
        String name = null;
        double mag = 9.9;
        final int index = entry.data.indexOf(44);
        if (index < 0) {
            return null;
        }
        final StringTokenizer st = new StringTokenizer(entry.data.substring(index + 1), ",");
        if (st.countTokens() == 15) {
            name = st.nextToken().trim();
            for (int i = 0; i < 11; ++i) {
                st.nextToken();
            }
            mag = FileIO.parseDouble(st.nextToken().trim(), 9.9, false);
        }
        final StringBuilder error = new StringBuilder();
        final StringBuilder name_buf = new StringBuilder("," + name);
        final int o_flag = eph.swe_fixstar_ut(name_buf, julian_day_ut, ephe_flag, computation, error);
        if (o_flag == -1) {
            return null;
        }
        eclToEqu(julian_day_ut);
        final String[] data = { name, String.valueOf(City.formatLongLatitude(computation[0], true, true, false)) + ", " + City.formatLongLatitude(computation[1], false, true, false), FileIO.formatDouble(mag, 1, 2, true, true), null, null };
        double d_x = computation[0] - pos[0];
        final double d_y = computation[1] - pos[1];
        if (d_x > 180.0) {
            d_x = 360.0 - d_x;
        }
        data[3] = String.valueOf(FileIO.formatDouble(d_x, 2, 2, false, false)) + ", " + FileIO.formatDouble(d_y, 2, 2, false, false);
        data[4] = FileIO.formatDouble(Math.sqrt(entry.error), 2, 2, true, false);
        return data;
    }
    
    public LinkedList computePlanetAzimuth(final int body, final double start_ut, final double end_ut, final double degree, final double max_speed, final double shift, final boolean quick_azimuth, final boolean add_last) {
        final LinkedList head = new LinkedList();
        final int[] date = new int[5];
        double ut = start_ut;
        while (ut < end_ut) {
            double n_ut = computePlanetAzimuthTransit(body, ut, degree, shift, QUARTER_DEGREE_PRECISION, quick_azimuth, true, false, true);
            if (n_ut != Double.MIN_VALUE) {
                getDateFromJulianDayUT(n_ut, date);
                ut = getJulianDayUT(date);
                final double speed = computePlanetAzimuthSpeed(body, ut, quick_azimuth);
                if (speed <= JUMP_SPEED) {
                    if (speed <= max_speed) {
                        final SearchRecord record = new SearchRecord(ut, SearchRecord.UNKNOWN);
                        if (add_last) {
                            head.addLast(record);
                        }
                        else {
                            head.addFirst(record);
                        }
                    }
                    ut += 0.9;
                    continue;
                }
            }
            double period = JUMP_PERIOD;
            double t_ut;
            do {
                t_ut = Math.min(end_ut, ut + period);
                n_ut = computePlanetAzimuthTransit(body, t_ut, degree, shift, DEGREE_PRECISION, quick_azimuth, false, false, true);
                if (n_ut == Double.MIN_VALUE) {
                    break;
                }
                if (computePlanetAzimuthSpeed(body, n_ut, quick_azimuth) > max_speed) {
                    break;
                }
                period *= 0.5;
            } while (period > TIME_PERIOD);
            ut = t_ut;
        }
        return head;
    }
    
    public boolean computeSolarEclipseLocation(final double ut, final double[] pos) throws IOException {
        final StringBuilder error = new StringBuilder();
        final double[] attr = new double[20];
        return eph.swe_sol_eclipse_where(ut, ephe_flag, pos, attr, error) != SweConst.ERR;
    }
    
    public LinkedList computeSolarEclipse( double start_ut,  double end_ut,  boolean win,  boolean add_last,  boolean anywhere) throws IOException {
        LinkedList head = new LinkedList();
        StringBuilder error = new StringBuilder();
        final double[] tret = new double[10];
        final double[] attr = new double[20];
        if (!win) {
            loadEphIfNeeded(start_ut, end_ut);
        }
        for (double ut = start_ut; ut < end_ut; ut = tret[0] + 1.0) {
            int type;
            if (anywhere) {
                type = eph.swe_sol_eclipse_when_glob(ut, ephe_flag, 0, tret, 0, error);
            }
            else {
                type = eph.swe_sol_eclipse_when_loc(ut, ephe_flag, location, tret, attr, 0, error);
            }
            if (type < 0 || tret[0] > end_ut) {
                return head;
            }
            final SearchRecord record = new SearchRecord(tret[0], ((type & SweConst.SE_ECL_TOTAL) == SweConst.SE_ECL_TOTAL) ? SearchRecord.TOTAL_ECLIPSE
                    : (((type & SweConst.SE_ECL_ANNULAR) == SweConst.SE_ECL_ANNULAR) ? SearchRecord.ANNULAR_ECLIPSE : SearchRecord.PARTIAL_ECLIPSE));
            if (add_last) {
                head.addLast(record);
            }
            else {
                head.addFirst(record);
            }
            if (win) {
                return head;
            }
        }
        return head;
    }
    
    public LinkedList computeLunarEclipse( double start_ut,  double end_ut,  boolean win,  boolean add_last) {
         LinkedList head = new LinkedList();
         StringBuilder error = new StringBuilder();
         double[] tret = new double[10];
         double[] attr = new double[20];
        if (!win) {
            loadEphIfNeeded(start_ut, end_ut);
        }
        for (double ut = start_ut; ut < end_ut; ut = tret[0] + 1.0) {
            int type = eph.swe_lun_eclipse_when(ut, ephe_flag, 0, tret, 0, error);
            if (type < 0 || tret[0] > end_ut) {
                return head;
            }
            type = eph.swe_lun_eclipse_how(tret[0], ephe_flag, location, attr, error);
            if (type <= 0) {
                return head;
            }
            final SearchRecord record = new SearchRecord(
                    tret[0],
                    ((type & SweConst.SE_ECL_TOTAL) == SweConst.SE_ECL_TOTAL) ? SearchRecord.TOTAL_ECLIPSE
                            : (((type & SweConst.SE_ECL_PENUMBRAL) == SweConst.SE_ECL_PENUMBRAL) ? SearchRecord.PENUMBRAL_ECLIPSE
                            : SearchRecord.PARTIAL_ECLIPSE));
            if (add_last) {
                head.addLast(record);
            }
            else {
                head.addFirst(record);
            }
            if (win) {
                return head;
            }
        }
        return head;
    }
    
    private void loadEphIfNeeded( double s_ut,  double e_ut) {
        if (getEphMode()) {
            return;
        }
        compute(s_ut, SweConst.SE_SUN);
        compute(e_ut, SweConst.SE_SUN);
    }
    
    private boolean loadEphIndex(String index) {
        if ((ephe_flag & SweConst.SEFLG_MOSEPH) != 0) {
            // already in Moseph mode and still failing, must be asteroids
            return false;
        }
        if (index.startsWith("ast")) {
//            int n = index.indexOf(92);
//            if (n < 0) {
//                n = index.indexOf(47);
//            }
            int n = index.indexOf('\\');
            if (n < 0)
                n = index.indexOf('/');
             String ast_dir_name = index.substring(0, n);
            index = index.substring(n + 1);
            if (load_table.get(index) != null) {
                return false;
            }
             int num = FileIO.parseInt(index.substring(2), 0, true);
            if (!Message.question(Resource.getString("dialog_need_data_3") + " " + num + " " + Resource.getString("dialog_need_data_4"))) {
                load_table.put(index, "t");
                return false;
            }
             String ast_name = index + ".se1";
             String url_base = Resource.getString("asteroid_url");
             File ast_dir = new File("ephe" + File.separator + ast_dir_name);
             File ast_file = new File("ephe" + File.separator + ast_dir_name + File.separator + ast_name);
             File ast_tmp = new File("ephe" + File.separator + ast_dir_name + File.separator + index + ".tmp");
            if (((ast_dir.isDirectory() || ast_dir.mkdir()) && ast_file.exists()) || copyFileFromURL(url_base + "/" + ast_dir_name + "/" + ast_name, ast_tmp.getPath(), true)) {
                if (ast_tmp.exists()) {
                    ast_tmp.renameTo(ast_file);
                }
                return true;
            }
            ast_tmp.deleteOnExit();
            Message.warn(Resource.getString("dialog_download_fail_1") +
                    " " + Resource.getString("asteroid_url") +
                    " " + Resource.getString("dialog_download_fail_3"));
            load_table.put(index, "t");
            return false;
        }
        else {
             boolean bc = index.startsWith("m");
             int year = FileIO.parseInt(index.substring(1), 0, true) * 100;
            String s_date;
            String e_date;
            if (bc) {
                s_date = year + 1 + " B.C.";
                e_date = year - 600 + 2 + " B.C.";
            }
            else {
                s_date = ((year == 0) ? "1 B.C." : (year + " A.D."));
                e_date = year + 600 + " A.D.";
            }
            if (!Message.question(Resource.getString("dialog_need_data_1") + " " + s_date + " " + Resource.getString("to") + " " + e_date + " " + Resource.getString("dialog_need_data_2"))) {
                Message.info(Resource.getString("dialog_download_switch"));
                setEphMode(true);
                return true;
            }
             String pl_name = "sepl" + index + ".se1";
             String mo_name = "semo" + index + ".se1";
             String as_name = "seas" + index + ".se1";
             String url_base2 = Resource.getString("ephe_url");
             File pl_file = new File("ephe" + File.separator + pl_name);
             File mo_file = new File("ephe" + File.separator + mo_name);
             File as_file = new File("ephe" + File.separator + as_name);
             File pl_tmp = new File("ephe" + File.separator + "sepl" + index + ".tmp");
             File mo_tmp = new File("ephe" + File.separator + "semo" + index + ".tmp");
             File as_tmp = new File("ephe" + File.separator + "seas" + index + ".tmp");
            if ((pl_file.exists() || copyFileFromURL(url_base2 + "/" + pl_name, pl_tmp.getPath(), true)) 
                    && (mo_file.exists() || copyFileFromURL(url_base2 + "/"
                    + mo_name, mo_tmp.getPath(), true)) && (as_file.exists() 
                    || copyFileFromURL(url_base2 + "/"
                    + as_name, as_tmp.getPath(), true)))
            {
                if (pl_tmp.exists()) {
                    pl_tmp.renameTo(pl_file);
                }
                if (mo_tmp.exists()) {
                    mo_tmp.renameTo(mo_file);
                }
                if (as_tmp.exists()) {
                    as_tmp.renameTo(as_file);
                }
                return true;
            }
            pl_tmp.deleteOnExit();
            mo_tmp.deleteOnExit();
            as_tmp.deleteOnExit();
            Message.warn(Resource.getString("dialog_download_fail_1") + " " 
                    + Resource.getString("ephe_url") + " " + Resource.getString("dialog_download_fail_2"));
            setEphMode(true);
            return true;
        }
    }
    
    private boolean copyFileFromURL( String url_name,  String out_name,  boolean again) {
        try {
             URL url = new URL(url_name);
             URLConnection conn = url.openConnection();
             BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(out_name));
             byte[] buffer = new byte[4096];
            int size;
            while ((size = in.read(buffer)) >= 0) {
                out.write(buffer, 0, size);
            }
            in.close();
            out.close();
        }
        catch (IOException e) {
            return again && copyFileFromURL(url_name, out_name, false);
        }
        return true;
    }
    
    private String getEphIndex(String message) {
         String missing_ast_file = "SwissEph file 'ast";
         String missing_pl_file = "SwissEph file 'sepl";
         String missing_mo_file = "SwissEph file 'semo";
         String missing_as_file = "SwissEph file 'seas";
        int index = message.indexOf(missing_ast_file);
        if (index >= 0) {
            message = message.substring(message.indexOf("'") + 1);
        }
        else {
            index = message.indexOf(missing_pl_file);
            if (index < 0) {
                index = message.indexOf(missing_mo_file);
                if (index < 0) {
                    index = message.indexOf(missing_as_file);
                    if (index < 0) {
                        return null;
                    }
                }
            }
            message = message.substring(index + missing_pl_file.length());
        }
        index = message.indexOf(".");
        return message.substring(0, index);
    }
    
    public static double BeijingTime( double ut) {
        return ut + BEIJING_TIME_OFFSET;
    }
    
    public double[] computeNewMoons( int[] birth_date,  double[] solar_terms) {
        double[] new_moons = new double[16];
        double when = solar_terms[0] + 2.0;
        boolean backward = true;
        setTopocentricMode(true, false);
        int size;
        for (size = 0; size < 16; ++size) {
            when = computeNewMoon(when, backward);
            new_moons[size] = BeijingTime(when); // map to Beijing time
            when = trimHour(new_moons[size]);
            if (backward) {
                if (when <= solar_terms[0]) {
                    backward = false;
                }
                else {
                    --size;
                }
            }
            else if (when > solar_terms[24]) {
                break;
            }
            when += (backward ? -25.0 : 25.0);
        }
        setTopocentricMode(false, false);
        double[] array = new double[size + 1];
        for (int j = 0; j <= size; ++j) {
            array[j] = new_moons[j];
        }
        new_moons = array;
        return new_moons;
    }
    /**
     * Find the new moon time from which the calculation starts
     * @author xiaxiaozheng
     * @date 16:42 1/2/2023
     * @param start_ut
     * @param backward
     * @return double
     **/
    private double computeNewMoon( double start_ut,  boolean backward) {
        try {
            int i_flag = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_TRANSIT_LONGITUDE;
            TransitCalculator tcal = new TCPlanetPlanet( eph,
                    SweConst.SE_MOON,
                    SweConst.SE_SUN,
                    i_flag, 0. );
//            double deltaT = SweDate.getDeltaT( start_ut );
            return eph.getTransitUT( tcal, start_ut, backward );
        }
        catch (Exception e) {
            return Double.MIN_VALUE;
        }
    }
    
    public int[] getSolarCalendar( int[] date,  double[] solar_terms,  boolean start_at_winter_solstice) {
         int[] solar_date = date.clone();
         double jd = getJulianDayUT(date);
        if (start_at_winter_solstice) {
            solar_date[1] = 1;
            if (jd >= solar_terms[24]) {
                int n = 0;
                ++solar_date[n];
            }
            else {
                for (int i = 4; i <= 24; i += 2) {
                    if (jd >= solar_terms[i - 2] && jd < solar_terms[i]) {
                        solar_date[1] = i / 2;
                    }
                }
            }
        }
        else {
            solar_date[1] = 11;
            if (jd < solar_terms[1]) {
                int n2 = 0;
                --solar_date[n2];
            }
            else {
                for (int i = 3; i <= 24; i += 2) {
                    if (jd >= solar_terms[i - 2] && jd < solar_terms[i]) {
                        if (i == 3) {
                            final int n3 = 0;
                            --solar_date[n3];
                            solar_date[1] = 12;
                        }
                        else {
                            solar_date[1] = (i - 3) / 2;
                        }
                    }
                }
            }
        }
        solar_date[0] = getChineseYear(solar_date[0]);
        return solar_date;
    }
    
    public int[] getLunarCalendar( int[] date,  double[] solar_terms,  double[] new_moons) {
         int[] lunar_date = date.clone();
         double jd = getJulianDayUT(date);
        lunar_date[0] = getChineseYear(date[0]);
        int index;
        for (index = 1; index < new_moons.length && (jd < trimHour(new_moons[index - 1]) || jd >= trimHour(new_moons[index])); ++index) {}
        int leap_index = getLeapMonthIndex(solar_terms, new_moons);
        leap_month = (index - 1 == leap_index);
        int month = index + 10 - ((index - 1 >= leap_index) ? 1 : 0);
        if (month <= 12) {
            int n = 0;
            --lunar_date[n];
        }
        else {
            month -= 12;
            if (month == 12) {
                 int n2 = 0;
                ++date[n2];
                 double[] s_terms = computeSolarTerms(date);
                 double[] n_moons = computeNewMoons(date, s_terms);
                leap_index = getLeapMonthIndex(s_terms, n_moons);
                if (leap_index == 1) {
                    leap_month = true;
                    --month;
                }
                int n3 = 0;
                --date[n3];
            }
        }
        lunar_date[1] = month;
        lunar_date[2] = (int)(trimHour(jd) - trimHour(new_moons[index - 1])) + 1;
        lunar_date[0] = fixYear(lunar_date[0]);
        return lunar_date;
    }
    
    public boolean isLeapMonth() {
        return leap_month;
    }
    
    public int[] getLunarDate( int[] date) {
         double[] solar_terms = computeSolarTerms(date);
        if (solar_terms == null) {
            return null;
        }
         double[] new_moons = computeNewMoons(date, solar_terms);
         int year = date[0];
         int lunar_year = getChineseYear(date[0]);
         int[] l_date = getLunarCalendar(date, solar_terms, new_moons);
        if (l_date != null) {
            l_date[0] = ((l_date[0] == lunar_year) ? year : (year - 1));
        }
        return l_date;
    }
    
    public int[] getDateFromLunarDate(final int[] lunar_date, final boolean leap) {
        final int[] s_date = lunar_date.clone();
        s_date[1] = 6;
        s_date[2] = 15;
        double[] solar_terms = computeSolarTerms(s_date);
        if (solar_terms == null) {
            return null;
        }
        double[] new_moons = computeNewMoons(s_date, solar_terms);
        final int[] date = lunar_date.clone();
        lunar_date[0] = getChineseYear(lunar_date[0]);
        date[1] = 12;
        date[2] = 31;
        int[] l_date = getLunarCalendar(date, solar_terms, new_moons);
        int val = compareLunarDate(lunar_date, leap, l_date, isLeapMonth());
        if (val == 0) {
            return date;
        }
        double e_ut;
        double s_ut;
        if (val < 0) {
            e_ut = getJulianDayUT(date);
            s_ut = e_ut - 365.0;
        }
        else {
            s_ut = getJulianDayUT(date);
            e_ut = s_ut + 180.0;
            final int n = 0;
            ++s_date[n];
            solar_terms = computeSolarTerms(s_date);
            if (solar_terms == null) {
                return null;
            }
            new_moons = computeNewMoons(s_date, solar_terms);
        }
        while (e_ut - s_ut > 0.5) {
            final double m_ut = 0.5 * (s_ut + e_ut);
            getDateFromJulianDayUT(m_ut, date);
            date[3] = lunar_date[3];
            date[4] = lunar_date[4];
            l_date = getLunarCalendar(date, solar_terms, new_moons);
            val = compareLunarDate(lunar_date, leap, l_date, isLeapMonth());
            if (val == 0) {
                return date;
            }
            if (val < 0) {
                e_ut = m_ut;
            }
            else {
                s_ut = m_ut;
            }
        }
        return null;
    }
    
    private int compareLunarDate(final int[] date_1, final boolean leap_1, final int[] date_2, final boolean leap_2) {
        if (date_1[0] != date_2[0]) {
            return (fixYear(date_1[0] + 1) == date_2[0]) ? -1 : 1;
        }
        if (date_1[1] != date_2[1]) {
            return date_1[1] - date_2[1];
        }
        if (leap_1 != leap_2) {
            return leap_1 ? 1 : -1;
        }
        return date_1[2] - date_2[2];
    }
    
    private double trimHour(final double val) {
        return (int)(val - 0.5) + 0.5;
    }
    
    public int getChineseYear(int year) {
        year = (year - Resource.getInt("birth_year_base") + 1) % 60;
        return fixYear(year);
    }
    
    private int fixYear(int year) {
        while (year < 0) {
            year += 60;
        }
        if (year == 0) {
            year = 60;
        }
        return year;
    }
    
    private int getLeapMonthIndex(final double[] solar_terms, final double[] new_moons) {
        if (new_moons.length == 14) {
            return 100;
        }
        for (int j = 1; j < new_moons.length; ++j) {
            final double start = trimHour(new_moons[j - 1]);
            final double end = trimHour(new_moons[j]);
            boolean mid_term = false;
            for (int i = 0; i < solar_terms.length; i += 2) {
                final double center = solar_terms[i];
                if (center >= start && center < end) {
                    mid_term = true;
                    break;
                }
            }
            if (!mid_term) {
                return j - 1;
            }
        }
        return 100;
    }
    
    public static boolean isValid(final double val) {
        return val != Double.MIN_VALUE;
    }
    
    public String getStarSign(final double degree, final double[] sign_pos, final String[] signs) {
        final int len = signs.length;
        double last_pos = sign_pos[len - 1];
        for (int i = 0; i < len; ++i) {
            double val = degree;
            double pos = sign_pos[i];
            if (pos < last_pos) {
                pos += 360.0;
                if (val < last_pos) {
                    val += 360.0;
                }
            }
            if (val >= last_pos && val < pos) {
                return signs[(i > 0) ? (i - 1) : (len - 1)];
            }
            last_pos = sign_pos[i];
        }
        return "?invalid?";
    }
    
    public String getZodiac(final double degree, final boolean full) {
        final int index = (int)(degree / 30.0);
        return full ? full_zodiac[index] : zodiac[index];
    }
    
    public int getZodiacShift(final String sign, final double degree) {
        final int index = (int)(degree / 30.0);
        for (int i = 0; i < zodiac.length; ++i) {
            if (zodiac[i].equals(sign)) {
                int gap = index - i;
                if (gap < 0) {
                    gap += 12;
                }
                return gap;
            }
        }
        return 0;
    }
    
    public String getMountain(final double degree) {
        double val = degree - 22.5 + mountain_offset;
        if (val < 0.0) {
            val += 360.0;
        }
        final int index = (int)(val / 15.0);
        val = 15.0 + index * 15.0 - val;
        return mountain_name[index];
    }
    
    public int getElementalIndex(final double degree) {
        return (int)(degree / 30.0) % 4;
    }
    
    public int getElementalStateIndex(final double degree) {
        return (int)(degree / 30.0) % 3;
    }
    
    public void setMountainOffset(final double val) {
        mountain_offset = val;
    }
    
    public String formatDegree(final double degree, final boolean use_sign, final boolean show_second) {
        if (!isValid(degree)) {
            return "?invalid?";
        }
        final DecimalFormat format = new DecimalFormat("00");
        double val = degree;
        String str;
        if (use_sign) {
            final int index = (int)(val / 30.0);
            val -= index * 30.0;
            str = format.format((int) val) + zodiac_name[index];
        }
        else {
            val -= 22.5 + mountain_offset;
            if (val < 0.0) {
                val += 360.0;
            }
            final int index = (int)(val / 15.0);
            val = 15.0 + index * 15.0 - val;
            str = format.format((int) val) + mountain_name[index];
        }
        val -= (int)val;
        return str + City.toMinuteSeconds((val < 0.0) ? (-val) : val, show_second);
    }
    
    public int getSignIndex(final double degree, final double[] sign_pos, final int start) {
        if (!isValid(degree)) {
            return -1;
        }
        final int len = sign_pos.length;
        double last_pos = sign_pos[len - 1];
        for (int i = start; i < len; ++i) {
            double val = degree;
            double pos = sign_pos[i];
            if (pos < last_pos) {
                pos += 360.0;
                if (val < last_pos) {
                    val += 360.0;
                }
            }
            if (val >= last_pos && val < pos) {
                return (i > start) ? (i - 1) : (len - 1);
            }
            last_pos = sign_pos[i];
        }
        return -1;
    }
    
    public String formatDegree(final double degree, final double[] sign_pos, final String[] signs, final boolean show_second) {
        if (!isValid(degree)) {
            return "?invalid?";
        }
        final DecimalFormat format = new DecimalFormat("00");
        final int len = signs.length;
        double last_pos = sign_pos[len - 1];
        for (int i = 0; i < len; ++i) {
            double val = degree;
            double pos = sign_pos[i];
            if (pos < last_pos) {
                pos += 360.0;
                if (val < last_pos) {
                    val += 360.0;
                }
            }
            if (val >= last_pos && val < pos) {
                val -= last_pos;
                final String str = format.format((int) val) + signs[(i > 0) ? (i - 1) : (len - 1)];
                val -= (int)val;
                return str + City.toMinuteSeconds((val < 0.0) ? (-val) : val, show_second);
            }
            last_pos = sign_pos[i];
        }
        return "?invalid?";
    }
    
    public String formatDegree(final double degree, final double[] sign_pos, final String[] signs, final String reverse, final boolean use_mountain, final boolean show_second) {
        final boolean half = sign_pos == null || ChartMode.isChartMode(3);
        String str = "";
        if (!half) {
            str = str + formatDegree(degree, sign_pos, signs, show_second) + reverse;
        }
        str = str + formatDegree(degree, !use_mountain, show_second);
        if (half) {
            str = str + reverse;
        }
        return str;
    }
    
    public String formatDegree(final double degree, final String space, final double[] sign_pos, final String[] signs, final boolean use_mountain, final boolean astrolog_coord) {
        final double val = astrolog_coord ? City.normalizeDegree(degree + 135.0) : City.normalizeDegree(-degree - 45.0);
        String str = formatDegree(degree, !use_mountain, true) + space;
        if (sign_pos != null) {
            str = str + formatDegree(degree, sign_pos, signs, true) + space;
        }
        str = str + FileIO.formatDouble(val, 3, 2, true, false);
        return str;
    }
    
    public String formatDegree(final double pos, final double alt, final String space) {
        final double val = City.normalizeDegree(-pos - 45.0);
        return FileIO.formatDouble(val, 3, 2, true, false) + space + FileIO.formatDouble(alt, 2, 1, true, true);
    }
    
    public String[] formatChineseDegree(final double degree, final double[] sign_pos, final String[] signs, final boolean extend) {
        if (!isValid(degree)) {
            return null;
        }
        final int len = signs.length;
        double last_pos = sign_pos[len - 1];
        for (int i = 0; i < len; ++i) {
            double val = degree;
            double pos = sign_pos[i];
            if (pos < last_pos) {
                pos += 360.0;
                if (val < last_pos) {
                    val += 360.0;
                }
            }
            if (val >= last_pos && val < pos) {
                val -= last_pos;
                final String[] array = { signs[(i > 0) ? (i - 1) : (len - 1)], BaseCalendar.chineseNumber((int)Math.round(val), false, false) };
                if (extend || array[1].length() < 3) {
                    final int n = 1;
                    array[n] = array[n] + Resource.getString("degree");
                }
                return array;
            }
            last_pos = sign_pos[i];
        }
        return null;
    }
    
    public static int getAsteroidData(final String[] name, final int[] number, final boolean[] show, final LinkedList head) {
        if (head == null) {
            Arrays.fill(name, "");
            Arrays.fill(number, 0);
            Arrays.fill(show, false);
        }
        if (!Resource.hasEitherKey("asteroids")) {
            return 0;
        }
        final String[] array = Resource.getPrefStringArray("asteroids");
        int size = 0;
        for (String s : array) {
            String str = s;
            int n = str.indexOf(58);
            String key = str.substring(0, n);
            str = str.substring(n + 1);
            n = str.indexOf(58);
            final int num = FileIO.parseInt(str.substring(0, n), 0, true);
            final boolean selected = FileIO.parseInt(str.substring(n + 1), 0, true) != 0;
            if (selected || head == null) {
                if (head != null) {
                    key = extractAsteroidKey(key);
                    if (key == null) {
                        continue;
                    }
                    head.addLast(key);
                    head.addLast(num);
                } else {
                    show[size] = selected;
                    name[size] = key;
                    number[size] = num;
                }
                ++size;
            }
        }
        return size;
    }
    
    public static String extractAsteroidKey(String key) {
        int n = key.indexOf(91);
        if (n >= 0) {
            key = key.substring(n + 1);
        }
        n = key.indexOf(93);
        if (n >= 0) {
            key = key.substring(0, n);
        }
        key = key.trim();
        return (key.length() == 0) ? null : key.substring(0, 1);
    }
    
    public void dispose() {
        eph.swe_close();
    }
    
    public boolean dumpPlanetData(final String dir, final int start_year, final int end_year, final boolean speed_group) {
        if (end_year <= start_year) {
            return false;
        }
        final double ut_sav = julian_day_ut;
        final int[] date = new int[5];
        date[0] = start_year;
        date[1] = (date[2] = 1);
        date[3] = (date[4] = 0);
        setJulianDay(date);
        final double period = 365.25 * (end_year - start_year);
        if (speed_group) {
            final FileIO sum = new FileIO(dir + "/" + "summary.txt", false, true);
            dumpPlanetSpeed(2, 4, period, dir, sum);
            dumpPlanetSpeed(3, 2, period, dir, sum);
            dumpPlanetSpeed(4, 5, period, dir, sum);
            dumpPlanetSpeed(5, 3, period, dir, sum);
            dumpPlanetSpeed(6, 6, period, dir, sum);
            sum.dispose();
        }
        else {
            dumpPlanetAttribute(2, period, dir);
            dumpPlanetAttribute(3, period, dir);
            dumpPlanetAttribute(4, period, dir);
            dumpPlanetAttribute(5, period, dir);
            dumpPlanetAttribute(6, period, dir);
            dumpPlanetAttribute(7, period, dir);
            dumpPlanetAttribute(8, period, dir);
            dumpPlanetAttribute(9, period, dir);
        }
        julian_day_ut = ut_sav;
        return true;
    }
    
    private void dumpPlanetAttribute(final int body, final double period, final String dir) {
        final StringBuilder error = new StringBuilder();
        final double ut = julian_day_ut;
        final FileIO out = new FileIO(dir + "/" + eph.swe_get_planet_name(body) + ".csv", false, false);
        double pos = compute(body);
        final double end_ut = julian_day_ut + period;
        final double delta_ut = (end_ut - julian_day_ut) / 999.99;
        final double[] xnasc = new double[6];
        final double[] xndsc = new double[6];
        final double[] xperi = new double[6];
        final double[] xaphe = new double[6];
        out.putLine("ut,speed,position,sun diff,xperi,xaphe,xperi diff,xaphe diff");
        while (julian_day_ut < end_ut) {
            final double s_pos = compute(0);
            pos = compute(body);
            final double speed = getSpeed();
            eph.swe_nod_aps_ut(julian_day_ut, body, ephe_flag, 1, xnasc, xndsc, xperi, xaphe, error);
            double diff = Math.abs(s_pos - pos);
            if (diff > 180.0) {
                diff = 360.0 - diff;
            }
            String str = julian_day_ut + "," + speed + "," + pos + "," + diff + "," + xperi[0] + "," + xaphe[0];
            diff = Math.abs(xperi[0] - pos);
            if (diff > 180.0) {
                diff = 360.0 - diff;
            }
            str = str + "," + diff;
            diff = Math.abs(xaphe[0] - pos);
            if (diff > 180.0) {
                diff = 360.0 - diff;
            }
            str = str + "," + diff;
            out.putLine(str);
            julian_day_ut += delta_ut;
        }
        out.dispose();
        julian_day_ut = ut;
    }
    
    private void dumpPlanetSpeed(final int body, int index, final double period, final String dir, final FileIO sum) {
        index -= 2;
        final double ut = julian_day_ut;
        final String stat = eph.swe_get_planet_name(body);
        final FileIO out = new FileIO(dir + "/" + eph.swe_get_planet_name(body) + ".txt", false, true);
        final double end_ut = julian_day_ut + period;
        final double delta_ut = 1.0;
        double p_ut = 0.0;
        double p_pos = 0.0;
        double l_pos = 0.0;
        double l_ut = 0.0;
        int p_state = -1;
        int state_count = 0;
        int total = 0;
        final int[] count = new int[speed_state.length];
        while (julian_day_ut < end_ut) {
            sun_pos = compute(0);
            final double pos = compute(body);
            final int state = getSpeedState(body, index);
            if (p_state == state) {
                ++state_count;
            }
            else {
                if (state_count > 0) {
                    out.putLine(speed_state[p_state] + ": " + state_count + " [" + p_ut + "-" + l_ut + ", " + p_pos + "-" + l_pos + "]");
                }
                state_count = 1;
                p_state = state;
                p_ut = julian_day_ut;
                p_pos = pos;
            }
            l_pos = pos;
            l_ut = julian_day_ut;
            ++count[state];
            ++total;
            julian_day_ut += delta_ut;
        }
        if (state_count > 0) {
            out.putLine(speed_state[p_state] + ": " + state_count + " [" + p_ut + "-" + l_ut + ", " + p_pos + "-" + l_pos + "]");
        }
        out.dispose();
        julian_day_ut = ut;
        sum.putLine(stat + ":");
        for (int i = 0; i < speed_state.length; ++i) {
            sum.putLine(speed_state[i] + ": " + count[i] + " (" + FileIO.formatDouble(100.0 * count[i] / total, 2, 1, false, false) + "%)");
        }
    }
    
    class StarEntry implements Comparable
    {
        String data;
        double error;
        
        public StarEntry(String str, double err) {
            data = str;
            error = err;
        }
        
        @Override
        public int compareTo(Object obj) {
            return Double.compare(error, ((StarEntry)obj).error);
        }
    }

}
