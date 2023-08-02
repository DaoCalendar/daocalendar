package com.saltfun.daocalendar.base;

public class Message
{
    private static BaseMessage mesg;
    
    public static void setMessage(final BaseMessage msg) {
        Message.mesg = msg;
    }
    
    public static void info(final String message) {
        Message.mesg.info(message);
    }
    
    public static void warn(final String message) {
        Message.mesg.warn(message);
    }
    
    public static boolean question(final String message) {
        return Message.mesg.question(message);
    }
    
    public static void error(final String message) {
        Message.mesg.error(message);
    }
}
