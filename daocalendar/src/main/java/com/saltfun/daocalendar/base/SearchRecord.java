package com.saltfun.daocalendar.base;

public class SearchRecord
{
    public static final int UNKNOWN = 0;
    public static final int PARTIAL_ECLIPSE = 1;
    public static final int ANNULAR_ECLIPSE = 2;
    public static final int PENUMBRAL_ECLIPSE = 3;
    public static final int TOTAL_ECLIPSE = 4;
    private int type;
    private final double jd_ut;
    private String data;
    
    public SearchRecord(double val, int kind) {
        jd_ut = val;
        type = kind;
    }
    
    public SearchRecord(double val, String mesg) {
        jd_ut = val;
        data = mesg;
    }
    
    public double getTime() {
        return jd_ut;
    }
    
    public String getData() {
        return data;
    }
    
    public boolean isType(int kind) {
        return type == kind;
    }
    /**
     * total_eclipse=全蚀
     * partial_eclipse=偏蚀
     * annular_eclipse=环蚀
     * penumbral_eclipse=半影蚀
     * @author xiaxiaozheng
     * @date 18:26 1/17/2023
     * @return java.lang.String
     **/
    public String getType() {
        switch (type) {
            case ANNULAR_ECLIPSE: {
                return Resource.getString("annular_eclipse");
            }
            case PENUMBRAL_ECLIPSE: {
                return Resource.getString("penumbral_eclipse");
            }
            case TOTAL_ECLIPSE: {
                return Resource.getString("total_eclipse");
            }
            case PARTIAL_ECLIPSE: {
                return Resource.getString("partial_eclipse");
            }
            default: {
                return "";
            }
        }
    }
}
