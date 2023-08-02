package com.saltfun.daocalendar.base;

import java.util.*;

public class EvalRule
{
    private final ChartData data;
    private BaseTab out;
    private int year_offset_start;
    private int year_offset_end;
    private boolean list_variable;
    private boolean show_all_failure;
    private boolean show_failure;
    private Hashtable birth_table;
    private Hashtable now_table;
    private LinkedList good;
    private LinkedList bad;
    private String[] full_zodiac;
    private String[] full_stellar_signs;
    
    public EvalRule(final ChartData t_data) {
        data = t_data;
    }
    
    public void initSign( String[] zodiac,  String[] stellar_signs,  boolean sex) {
        birth_table = new Hashtable();
        good = new LinkedList();
        bad = new LinkedList();
        now_table = null;
        full_zodiac = zodiac;
        full_stellar_signs = stellar_signs;
        RuleEntry.initBirth(birth_table, full_zodiac, full_stellar_signs, sex);
        String space = Resource.getString("non_white_space");
        birth_table.put("$s", space);
        space = space + space;
        birth_table.put("$s2", space);
        space = space + space;
        birth_table.put("$s4", space);
        space = space + space;
        birth_table.put("$s8", space);
        birth_table.put("$n", "\n");
        birth_table.put("$t", "\t");
        birth_table.put("?t", "t");
        birth_table.put("?" + Resource.getString(sex ? "male" : "female"), "t");
        birth_table.put("$e", new LinkedHashSet<>());
    }
    
    public void initNow() {
        RuleEntry.initNow(now_table = new Hashtable());
    }
    
    public void setBirthInfo( Calculate cal,  String[] birth_poles,  boolean day_birth,  String[] year_signs,
                              double life_sign_pos,  double self_sign_pos,  double[] stellar_sign_pos,
                              int[] lunar_date,  boolean leap_month)
    {
        String pole_char = Resource.getString("pole_char");
        String year_char = Resource.getString("year_char");
        String month_char = Resource.getString("month_char");
        String day_char = Resource.getString("day_char");
        String hour_char = Resource.getString("hour_char");
        String zodiac_house = Resource.getString("zodiac_house");
        birth_table.put("$" + year_char + pole_char, birth_poles[ChartData.YEAR_POLE]);
        birth_table.put("$" + month_char + pole_char, birth_poles[ChartData.MONTH_POLE]);
        birth_table.put("$" + day_char + pole_char, birth_poles[ChartData.DAY_POLE]);
        birth_table.put("$" + hour_char + pole_char, birth_poles[ChartData.HOUR_POLE]);
         String month = birth_poles[ChartData.MONTH_POLE].substring(1);
        String[] array = Resource.getStringArray("earth_pole_names");
        int val = FileIO.getArrayIndex(month, array) - 2;
        if (val < 0) {
            val += 12;
        }
        val /= 3;
        array = Resource.getStringArray("four_seasons");
        birth_table.put("$" + Resource.getString("season"), Integer.toString(val));
        birth_table.put("?" + array[val], "t");
        birth_table.put("?" + Resource.getString(day_birth ? "daytime" : "nighttime"), "t");
        val = (int)(life_sign_pos / 30.0);
        for (int i = 0; i < year_signs.length; ++i) {
            int n = val - i;
            if (n < 0) {
                n += 12;
            }
            birth_table.put("@" + year_signs[i], full_zodiac[n]);
            birth_table.put("$" + full_zodiac[n].charAt(0) + zodiac_house, year_signs[i]);
            final String key = full_zodiac[i].substring(0, 1);
            String value = data.getWeakHouse(key);
            if (!value.equals("")) {
                birth_table.put("?" + value + key, "t");
            }
            value = data.getSolidHouse(key);
            if (!value.equals("")) {
                birth_table.put("?" + value + key, "t");
            }
        }
        LinkedHashSet set = new LinkedHashSet();
        Collections.addAll(set, full_zodiac);
        birth_table.put("$" + Resource.getString("zodiac_group"), set);
        String lunar = Resource.getString("lunar_calendar").substring(0, 1);
        birth_table.put("$" + lunar + year_char, birth_poles[0]);
        if (leap_month) {
            birth_table.put("?" + Resource.getString("leap") + month_char, "t");
        }
        birth_table.put("$" + lunar + month_char, Integer.toString(lunar_date[1]));
        int[] range = Resource.getIntArray("lunar_face_1_range");
        boolean b_val;
        if (range[0] > range[1]) {
            b_val = (lunar_date[2] >= range[0] || lunar_date[2] <= range[1]);
        }
        else {
            b_val = (lunar_date[2] >= range[0] && lunar_date[2] <= range[1]);
        }
        if (b_val) {
            birth_table.put("?" + Resource.getString("lunar_face_1"), "t");
        }
        range = Resource.getIntArray("lunar_face_2_range");
        if (range[0] > range[1]) {
            b_val = (lunar_date[2] >= range[0] || lunar_date[2] <= range[1]);
        }
        else {
            b_val = (lunar_date[2] >= range[0] && lunar_date[2] <= range[1]);
        }
        if (b_val) {
            birth_table.put("?" + Resource.getString("lunar_face_2"), "t");
        }
        String key2 = Resource.getString("life_master").substring(0, 1);
        birth_table.put("@" + key2, cal.getZodiac(life_sign_pos, true));
        birth_table.put("%" + key2, cal.getStarSign(life_sign_pos, stellar_sign_pos, full_stellar_signs));
        final double boundary_range = Resource.getDouble("boundary_range");
        double d_val = life_sign_pos % 30.0;
        if (d_val < boundary_range || d_val > 30.0 - boundary_range) {
            birth_table.put("?" + key2 + Resource.getString("zodiac_boundary"), "t");
        }
        if (withinSignBoundary(life_sign_pos, stellar_sign_pos, boundary_range)) {
            birth_table.put("?" + key2 + Resource.getString("stellar_boundary"), "t");
        }
        key2 = Resource.getString("self_master").substring(0, 1);
        birth_table.put("@" + key2, cal.getZodiac(self_sign_pos, true));
        birth_table.put("%" + key2, cal.getStarSign(self_sign_pos, stellar_sign_pos, full_stellar_signs));
        d_val = self_sign_pos % 30.0;
        if (d_val < boundary_range || d_val > 30.0 - boundary_range) {
            birth_table.put("?" + key2 + Resource.getString("zodiac_boundary"), "t");
        }
        if (withinSignBoundary(self_sign_pos, stellar_sign_pos, boundary_range)) {
            birth_table.put("?" + key2 + Resource.getString("stellar_boundary"), "t");
        }
        key2 = Resource.getString("life_helper_key");
        for (int iter = 0; iter < 2; ++iter) {
            String sign;
            String type;
            switch (iter) {
                case 0: {
                    sign = cal.getStarSign(life_sign_pos, stellar_sign_pos, full_stellar_signs);
                    type = Resource.getString("degree");
                    break;
                }
                default: {
                    sign = cal.getZodiac(life_sign_pos, true);
                    type = zodiac_house;
                    break;
                }
            }
            final String[] helper = Resource.getStringArray(key2 + sign.substring(1));
            for (int k = 0; k < helper.length; k++) {
                saveStringSetToTable(birth_table, "$" + key2.charAt(k) + type, helper[k], 1);
            }
        }
    }
    
    public boolean setNowInfo( Calculate cal,  String[] now_poles,  int[] now_date, int age,  double[] now_pos,
                               double[] stellar_sign_pos,  int[] now_lunar_date,  boolean now_leap_month) {
        if (now_poles == null || now_date == null) {
            return false;
        }
        if (now_pos == null) {
            return false;
        }
        String str = getNowStellarPos(cal, now_pos, stellar_sign_pos);
        if (str == null) {
            return false;
        }
        saveStringSetToTable(now_table, "$" + Resource.getString("limit"), str, 2);
        str = getNowZodiacPos(cal, now_pos);
        saveStringSetToTable(now_table, "$" + Resource.getString("zodiac_limit"), str, 1);
        double pos;
        double gap;
        if (now_pos[1] > now_pos[0]) {
            pos = City.normalizeDegree(0.5 * (now_pos[0] + now_pos[1] + 360.0));
            gap = now_pos[0] - now_pos[1] + 360.0;
        }
        else {
            pos = 0.5 * (now_pos[0] + now_pos[1]);
            gap = now_pos[0] - now_pos[1];
        }
        now_table.put("$" + Resource.getString("limit") + Resource.getString("degree"),
                FileIO.formatDouble(pos, 1, 2, false, false));
        now_table.put("$" + Resource.getString("limit") + Resource.getString("aspects_orb_name"),
                FileIO.formatDouble(gap, 1, 2, false, false));
         String pole_char = Resource.getString("pole_char");
         String year_char = Resource.getString("year_char");
         String current_date = Resource.getString("current_date");
         String now = current_date.substring(0, 1);
        now_table.put("$" + now + year_char + pole_char, now_poles[2]);
        if (now_lunar_date != null) {
             String month_char = Resource.getString("month_char");
             String day_char = Resource.getString("day_char");
             String hour_char = Resource.getString("hour_char");
            now_table.put("$" + now + month_char + pole_char, now_poles[3]);
            now_table.put("$" + now + day_char + pole_char, now_poles[4]);
            now_table.put("$" + now + hour_char + pole_char, now_poles[5]);
             String lunar = Resource.getString("lunar_calendar").substring(0, 1);
            now_table.put("$" + now + lunar + year_char, now_poles[0]);
            if (now_leap_month) {
                now_table.put("?" + now + Resource.getString("leap") + month_char, "t");
            }
            now_table.put("$" + now + lunar + month_char, Integer.toString(now_lunar_date[1]));
        }
        if (age < 1 || age > 99) {
            age = 1;
        }
        now_table.put("$" + now + Resource.getString("age"), Integer.toString(age));
        now_table.put("$" + current_date, Integer.toString(now_date[0]));
        final int child_age_limit = data.getChildLimit(true);
        String key = (age - 1 < child_age_limit) ? data.getChildLimit(age - 1, null) : null;
        if (key != null) {
            now_table.put("$" + Resource.getString("child_limit"), key);
        }
        key = data.getSmallLimit(age, ":");
        now_table.put("$" + key.substring(0, 2), key.substring(3));
        if (now_lunar_date != null) {
            key = data.getMonthLimit(age, ":");
            now_table.put("$" + key.substring(0, 2), key.substring(3));
        }
        key = data.getFlyLimit(age - 1, ":", child_age_limit);
        if (key != null) {
            String f_key = key.substring(0, 2);
            String f_val = key.substring(3);
            if (f_val.length() > 1) {
                f_val = f_val.substring(0, 2);
            }
            saveStringSetToTable(now_table, "$" + f_key, f_val, 1);
        }
        return true;
    }
    
    private String getNowStellarPos(final Calculate cal,  double[] pos,  double[] sign_pos) {
        int s_index = cal.getSignIndex(pos[0], sign_pos, 0);
        int e_index = cal.getSignIndex(pos[1], sign_pos, 0);
        if (s_index < 0 || e_index < 0) {
            return null;
        }
        if (e_index > s_index) {
            s_index += full_stellar_signs.length;
        }
        StringBuilder str = new StringBuilder();
        for (int i = s_index; i >= e_index; --i) {
            str.append(full_stellar_signs[i % full_stellar_signs.length]);
        }
        return str.toString();
    }
    
    private String getNowZodiacPos( Calculate cal,  double[] pos) {
         String str = cal.getZodiac(pos[0], false);
         String e_str = cal.getZodiac(pos[1], false);
        return str.equals(e_str) ? str : (str + e_str);
    }
    
    private boolean withinSignBoundary(double pos, double[] sign_pos, double range) {
        for (double signPo : sign_pos) {
             double l_val = signPo - range;
             double u_val = signPo + range;
            if (l_val < 0.0) {
                if (pos < u_val || pos > l_val + 360.0) {
                    return true;
                }
            } else if (u_val > 360.0) {
                if (pos < u_val - 360.0 || pos > l_val) {
                    return true;
                }
            } else if (pos > l_val && pos < u_val) {
                return true;
            }
        }
        return false;
    }
    
    public void setBirthSign( Calculate cal,  String[] signs,  double[] sign_pos,  double[] stellar_sign_pos,
                              double mountain_pos) {
        if (Calculate.isValid(mountain_pos)) {
            birth_table.put("@" + Resource.getString("mountain_name"), cal.getZodiac(mountain_pos, true));
        }
         String against = Resource.getString("against");
         String[] enemies = Resource.getStringArray("enemies");
         String strengthen = Resource.getString("strengthen");
         String[] helpers = Resource.getStringArray("helpers");
        for (String sign : signs) {
            for (String enemy : enemies) {
                if (sign.equals(enemy.substring(0, 1))) {
                    birth_table.put("$" + against + sign, enemy.substring(2, 3));
                }
            }
            for (String helper : helpers) {
                if (sign.equals(helper.substring(0, 1))) {
                    birth_table.put("$" + strengthen + sign, helper.substring(2, 3));
                }
            }
        }
        final Position[] positions = setSign(cal, birth_table, "", signs, sign_pos, stellar_sign_pos);
        if (positions == null || positions.length != 11) {
            return;
        }
        int lower = Position.getPositionIndex(positions, positions.length, 2);
        while (true) {
            final Position pos = Position.getPosition(positions, positions.length, lower - 1);
            final int val = pos.getIndex();
            if (val != 0 && (val < 2 || val > 6)) {
                break;
            }
            --lower;
        }
        int upper = lower + 5;
        boolean around_sun = false;
        while (lower < upper) {
            final Position pos2 = Position.getPosition(positions, positions.length, lower);
            final int val2 = pos2.getIndex();
            if (val2 < 2 || val2 > 6) {
                if (val2 != 0) {
                    break;
                }
                around_sun = true;
                ++upper;
            }
            ++lower;
        }
        if (lower == upper) {
            birth_table.put("?__sp" + (around_sun ? 5 : 4), "t");
            if (!around_sun) {
                final Position pos2 = Position.getPosition(positions, positions.length, lower - 1);
                final int val2 = pos2.getIndex();
                if (val2 == 0) {
                    birth_table.put("?__sp2", "t");
                }
                else if (val2 == 1) {
                    birth_table.put("?__sp3", "t");
                }
            }
        }
        lower = Position.getPositionIndex(positions, positions.length, 0);
        while (true) {
            final Position pos2 = Position.getPosition(positions, positions.length, lower - 1);
            final int val2 = pos2.getIndex();
            if (val2 > 6) {
                break;
            }
            --lower;
        }
        for (upper = lower + 7; lower < upper; ++lower) {
            final Position pos2 = Position.getPosition(positions, positions.length, lower);
            final int val2 = pos2.getIndex();
            if (val2 > 6) {
                break;
            }
        }
        if (lower == upper) {
            birth_table.put("?__sp1", "t");
        }
        lower = Position.getPositionIndex(positions, positions.length, 10);
        while (true) {
            final Position pos2 = Position.getPosition(positions, positions.length, lower - 1);
            final int val2 = pos2.getIndex();
            if (val2 != 1 && (val2 < 10 || val2 > 13)) {
                break;
            }
            --lower;
        }
        for (upper = lower + 5; lower < upper; ++lower) {
            final Position pos2 = Position.getPosition(positions, positions.length, lower);
            final int val2 = pos2.getIndex();
            if (val2 != 1) {
                if (val2 < 10) {
                    break;
                }
                if (val2 > 13) {
                    break;
                }
            }
        }
        if (lower == upper) {
            birth_table.put("?__sp6", "t");
        }
    }
    
    public void setNowSign(final Calculate cal, final String[] signs, final double[] sign_pos, final double[] stellar_sign_pos) {
        setSign(cal, now_table, Resource.getString("current_date").substring(0, 1), signs, sign_pos, stellar_sign_pos);
    }
    
    public Position[] setSign(final Calculate cal, Hashtable table, final String prefix, final String[] signs, final double[] sign_pos, final double[] stellar_sign_pos) {
        final String[] directions = Resource.getStringArray("directions");
        final String alone = Resource.getString("alone");
        final String before = Resource.getString("before");
        final String after = Resource.getString("after");
        final String none = Resource.getString("none");
        final String year_sign_key = Resource.getString("year_sign_key");
        final String zodiac_house = Resource.getString("zodiac_house");
        final String degree = Resource.getString("degree");
        final double boundary_range = Resource.getDouble("boundary_range");
        final LinkedList<Position> head = new LinkedList<>();
        for (int i = 0; i < signs.length; ++i) {
            final double pos = sign_pos[i];
            if (Calculate.isValid(pos)) {
                table.put("$" + prefix + signs[i] + degree, FileIO.formatDouble(sign_pos[i], 1, 2, false, false));
                final double d_val = pos % 30.0;
                if (d_val < boundary_range || d_val > 30.0 - boundary_range) {
                    birth_table.put("?" + prefix + signs[i] + Resource.getString("zodiac_boundary"), "t");
                }
                if (withinSignBoundary(pos, stellar_sign_pos, boundary_range)) {
                    birth_table.put("?" + prefix + signs[i] + Resource.getString("stellar_boundary"), "t");
                }
                final String h_val = cal.getZodiac(pos, true);
                final String s_val = cal.getStarSign(pos, stellar_sign_pos, full_stellar_signs);
                table.put("@" + prefix + signs[i], h_val);
                table.put("%" + prefix + signs[i], s_val);
                if (i <= 13 && i != 7 && i != 8 && i != 9) {
                    final int n = (int)(pos / 30.0);
                    table.put("?" + prefix + signs[i] + directions[n / 3], "t");
                    head.add(new Position(signs[i], i, pos, 0, false));
                    LinkedHashSet set = (LinkedHashSet) table.get("$" + prefix + h_val.charAt(0) + zodiac_house + year_sign_key);
                    set.add(signs[i]);
                    set = (LinkedHashSet) table.get("$" + prefix + s_val.charAt(0) + degree + year_sign_key);
                    set.add(signs[i]);
                }
            }
        }
        if (head.isEmpty()) {
            return null;
        }
        final Position[] positions = head.toArray(new Position[1]);
        if (positions.length > 1) {
            Arrays.sort(positions, 0, positions.length, (a, b) -> {
                final double p_a = a.getLocation();
                final double p_b = b.getLocation();
                return Double.compare(p_a, p_b);
            });
        }
        for (int j = 0; j < positions.length; ++j) {
            final Position pos2 = positions[j];
            final Position p_pos = Position.getPosition(positions, positions.length, j - 1);
            final Position n_pos = Position.getPosition(positions, positions.length, j + 1);
            final int i_pos = (int)(pos2.getLocation() / 30.0);
            int gap = i_pos - (int)(p_pos.getLocation() / 30.0);
            if (gap < 0) {
                gap += 12;
            }
            table.put("$" + prefix + pos2.getName() + after, (gap < 2) ? p_pos.getName() : none);
            gap = (int)(n_pos.getLocation() / 30.0) - i_pos;
            if (gap < 0) {
                gap += 12;
            }
            table.put("$" + prefix + pos2.getName() + before, (gap < 2) ? n_pos.getName() : none);
            final String pos_sign = (String)table.get("@" + prefix + pos2.getName());
            if (p_pos == pos2 || !pos_sign.equals(table.get("@" + prefix + p_pos.getName()))) {
                if (n_pos == pos2 || !pos_sign.equals(table.get("@" + prefix + n_pos.getName()))) {
                    table.put("?" + prefix + alone + pos2.getName(), "t");
                }
            }
        }
        return positions;
    }
    
    public void setBirthStarSign(Hashtable t_table, Hashtable master_table, LinkedList<String> ten_god_list, String[] signs, String[] star_equ_map, String year_info) {
        setStarSign(t_table, birth_table, master_table, ten_god_list, signs, star_equ_map, year_info, false);
    }
    
    public void setNowStarSign(Hashtable t_table, Hashtable master_table, LinkedList<String> ten_god_list, String[] signs, String[] star_equ_map, String year_info) {
        setStarSign(t_table, now_table, master_table, ten_god_list, signs, star_equ_map, year_info, true);
    }
    
    public void setStarSign(Hashtable t_table, Hashtable table, Hashtable master_table, LinkedList<String> ten_god_list, String[] signs, String[] star_equ_map, String year_info, boolean now) {
         String sep = Resource.getString("year_info_sep");
         String star_sign_key = Resource.getString("star_sign_key");
         String year_sign_key = Resource.getString("year_sign_key");
         String zodiac_house = Resource.getString("zodiac_house");
         String degree = Resource.getString("degree");
         String prefix = now ? Resource.getString("current_date").substring(0, 1) : "";
        for (int i = 0; i < 28; ++i) {
             String key = full_stellar_signs[i].substring(0, 1);
            table.put("$" + prefix + key + degree + year_sign_key, new LinkedHashSet());
        }
        for (int i = 0; i < 12; ++i) {
             String key = full_zodiac[i].substring(0, 1);
            table.put("$" + prefix + key + zodiac_house + year_sign_key, new LinkedHashSet());
             LinkedList head = (LinkedList) t_table.get(key);
            if (head != null) {
                 LinkedList star_list = new LinkedList();
                for (Object o : head) {
                    final String star = (String) o;
                    if (data.inMasterTable(star, now)) {
                        table.put("@" + prefix + star, full_zodiac[i]);
                        star_list.add(star);
                    }
                }
                setListSetToTable(table, "$" + prefix + key + zodiac_house + star_sign_key, star_list);
            }
        }
        for (int i = 0; i < star_equ_map.length; i += 2) {
            String value = (String)table.get("@" + prefix + star_equ_map[i]);
            if (value != null) {
                table.put("@" + prefix + star_equ_map[i + 1], value);
            }
            else {
                value = (String)table.get("@" + prefix + star_equ_map[i + 1]);
                if (value != null) {
                    table.put("@" + prefix + star_equ_map[i], value);
                }
            }
        }
        if (year_info == null) {
            return;
        }
        year_info = year_info.substring(year_info.indexOf("|")).replaceAll(" ", "");
         String str = sep.charAt(1) + Resource.getString("none") + sep.charAt(2);
        year_info = year_info.replaceAll(sep.substring(1), str);
         StringTokenizer st = new StringTokenizer(year_info, sep);
        while (st.hasMoreTokens()) {
             String key2 = st.nextToken();
            String value2 = st.nextToken();
            if (value2.length() == 3) {
                value2 = value2.charAt(0) + value2.substring(2);
            }
            saveStringSetToTable(table, "$" + prefix + key2, value2, 1);
        }
         String change_to = Resource.getString("change_to");
        for (String sign : signs) {
            final LinkedList list = (LinkedList) t_table.get(sign + star_sign_key);
            if (list != null) {
                for (int k = 0; k < 2 && !list.isEmpty(); ++k) {
                    final String val = (String) list.getFirst();
                    if (ten_god_list.contains(val)) {
                        list.removeFirst();
                        table.put("$" + prefix + sign + change_to, val);
                    }
                }
                setListSetToTable(table, "$" + prefix + sign + star_sign_key, list);
            }
        }
    }
    
    private void saveStringSetToTable(Hashtable t_table, String key, String value, int width) {
        if (value.length() / width > 1) {
            final LinkedHashSet set = new LinkedHashSet();
            for (int j = 0; j < value.length(); j += width) {
                set.add(value.substring(j, j + width));
            }
            t_table.put(key, set);
        }
        else {
            t_table.put(key, value);
        }
    }
    
    private void setListSetToTable(Hashtable t_table, String key, LinkedList list) {
        LinkedHashSet set = new LinkedHashSet(list);
        t_table.put(key, set);
    }
    
    public void initOptions(BaseTab tab) {
        out = tab;
        if (out == null || !RuleEntry.hasRuleEntry(true)) {
            return;
        }
        out.clear();
        RuleEntry.setOutputTab(out);
        RuleEntry.restoreTable(true);
        int n = 0;
        year_offset_end = n;
        year_offset_start = n;
        String str = RuleEntry.getOutput("option");
        if (str != null) {
            final StringTokenizer st = new StringTokenizer(str.replaceAll("[ \t]", ""), Resource.getString(","));
            while (st.hasMoreTokens()) {
                final StringTokenizer n_st = new StringTokenizer(st.nextToken(), "=");
                if (n_st.countTokens() != 2) {
                    continue;
                }
                final String key = n_st.nextToken();
                final String value = n_st.nextToken();
                if (key.equals("year_offset")) {
                    final StringTokenizer v_st = new StringTokenizer(value, ":");
                    if (v_st.countTokens() != 2) {
                        continue;
                    }
                    year_offset_start = FileIO.parseInt(v_st.nextToken(), 0, false);
                    year_offset_end = FileIO.parseInt(v_st.nextToken(), 0, false);
                }
                else {
                    if (!key.equals("wrap")) {
                        continue;
                    }
                    out.setWrapMode(!value.equals("0"));
                }
            }
        }
        list_variable = show_all_failure = show_failure = false;
        str = RuleEntry.getOutput("debug");
        if (str == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(str.replaceAll("[ \t]", ""), ",");
        while (st.hasMoreTokens()) {
            final StringTokenizer n_st = new StringTokenizer(st.nextToken(), "=");
            if (n_st.countTokens() != 2) {
                continue;
            }
            final String key = n_st.nextToken();
            final String value = n_st.nextToken();
            switch (key) {
                case "list_variable":
                    list_variable = !value.equals("0");
                    break;
                case "show_all_failure":
                    show_all_failure = !value.equals("0");
                    break;
                case "show_failure":
                    show_failure = !value.equals("0");
                    break;
                case "trace_rule":
                    RuleEntry.setTrace(value);
                    break;
                case "trace_variable":
                    RuleEntry.setTraceVariable(value);
                    break;
                default:
                    if (!key.equals("trace_rank")) {
                        continue;
                    }
                    RuleEntry.setTraceLevelRank(RuleEntry.getLevelRank(value));
                    break;
            }
        }
    }
    
    private void listStateTable(Hashtable table, String name) {
        if (out == null || !RuleEntry.hasRuleEntry(true)) {
            return;
        }
        out.appendLine("--- begin predefined " + name + " variable listing ---");
        Vector vector = new Vector(table.keySet());
        Collections.sort((List<Comparable>)vector);
        StringBuilder str = new StringBuilder("  ");
        int n = 0;
        String space = Resource.getString("non_white_space");
        for (Object key : vector) {
            final Object object = table.get(key);
            if (object instanceof String) {
                String val = (String)object;
                if (val.equals("\t")) {
                    val = "tab";
                }
                else if (val.equals("\n")) {
                    val = "newline";
                }
                else if (val.startsWith(space)) {
                    val = val.length() + " space";
                }
                final String value = FileIO.formatString(key + "=" + val, 7);
                str.append(value);
                if (++n < 6) {
                    continue;
                }
                out.appendLine(str.toString());
                str = new StringBuilder("  ");
                n = 0;
            }
        }
        if (n > 0) {
            out.appendLine(str.toString());
        }
        for (final Object key : Collections.unmodifiableList(vector)) {
            final Object object = table.get(key);
            if (object instanceof LinkedHashSet) {
                RuleEntry.showSetContent("  " + key + "=", object, 12);
            }
        }
        out.appendLine("--- end predefined " + name + " variable listing ---");
        out.appendLine();
    }
    
    public int getYearOffsetStart() {
        return year_offset_start;
    }
    
    public int getYearOffsetEnd() {
        return year_offset_end;
    }
    
    public void computeStyles() {
        RuleEntry.setDebugOption(show_all_failure);
        RuleEntry.restoreTable(false);
        final int[] style_range = Resource.getIntArray("style_range");
        final int level = style_range[1] - Resource.getPrefInt("style_level");
        RuleEntry.setRuleLevel(level, Resource.getPrefInt("fill_max_styles"));
        if (list_variable) {
            listStateTable(birth_table, Resource.getString("birth_date"));
        }
        RuleEntry.computeRules(good, bad);
        //五行合格 五星者即木火土金水也:白虎从驾：申酉月生金，日同宫。金为白虎星
        //政馀合格 政者日月五星馀者气孛罗计:金水从阳：金水掌吉，神居垣殿，昼生者奇。
    }
    
    public void ruleHeader() {
        RuleEntry.restoreTable(true);
        final String str = RuleEntry.getOutput("header");
        if (str != null) {
            out.appendLine(str);
        }
    }
    
    public void ruleFooter() {
        final String str = RuleEntry.getOutput("footer");
        if (str != null) {
            out.appendLine(str);
        }
    }
    
    public void computeRules() {
        RuleEntry.setDebugOption(show_all_failure || show_failure);
        LinkedList<String> r_good = new LinkedList<>();
        LinkedList<String> r_bad = new LinkedList<>();
        RuleEntry.setRuleLevel(0, Integer.MAX_VALUE);
        if (list_variable && now_table != null) {
            listStateTable(now_table, Resource.getString("current_date"));
        }
        RuleEntry.computeRules(r_good, r_bad);
        ListIterator<String> iter = r_good.listIterator();
        while (iter.hasNext()) {
            out.appendLine(iter.next());
        }
        iter = r_bad.listIterator();
        while (iter.hasNext()) {
            out.appendLine(iter.next());
        }
    }
    
    LinkedList getGoodStyles() {
        return good;
    }
    
    LinkedList getBadStyles() {
        return bad;
    }
}
