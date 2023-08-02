package com.saltfun.daocalendar.base;

public abstract class BaseMessage
{
    public void info(final String message) {
    }
    
    public void warn(final String message) {
    }
    
    public boolean question(final String message) {
        return false;
    }
    
    public void error(final String message) {
    }
}
