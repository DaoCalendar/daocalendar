package com.saltfun.daocalendar.base;

import java.util.Arrays;
import java.util.Comparator;

public class Transit
{
    public double jd_ut;
    public double pos;
    public int from_index;
    public int to_index;
    public int aspect_index;
    
    public Transit(final double time, final double degree, final int f_index, final int a_index, final int t_index) {
        this.jd_ut = time;
        this.pos = degree;
        this.from_index = f_index;
        this.to_index = t_index;
        this.aspect_index = a_index;
    }
    
    public static void sort(final Transit[] array) {
        Arrays.sort(array, 0, array.length, new Comparator() {
            @Override
            public int compare(final Object a, final Object b) {
                final double p_a = ((Transit)a).jd_ut;
                final double p_b = ((Transit)b).jd_ut;
                if (p_a < p_b) {
                    return -1;
                }
                if (p_a > p_b) {
                    return 1;
                }
                return 0;
            }
        });
    }
}
