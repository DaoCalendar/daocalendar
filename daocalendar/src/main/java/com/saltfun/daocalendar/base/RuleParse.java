package com.saltfun.daocalendar.base;

import java.util.LinkedHashSet;
import java.util.LinkedList;

public class RuleParse
{
    boolean yydebug;
    int yynerrs;
    int yyerrflag;
    int yychar;
    static final int YYSTACKSIZE = 256;
    int[] statestk = new int[YYSTACKSIZE];  // state stack

    int stateptr;
    int stateptrmax;
    int statemax;
    String yytext;
    Object yyval;
    Object yylval;
    Object valstk[];
    int valptr;
    public static final short IDENTIFIER = 257;
    public static final short STRING = 258;
    public static final short EQ = 259;
    public static final short NE = 260;
    public static final short LE = 261;
    public static final short GE = 262;
    public static final short DEF = 263;
    public static final short AGN = 264;
    public static final short APN = 265;
    public static final short ADE = 266;
    public static final short SUE = 267;
    public static final short MUE = 268;
    public static final short DIE = 269;
    public static final short MOE = 270;
    public static final short YYERRCODE = 256;
    static final short[] yylhs;
    static final short[] yylen;
    static final short[] yydefred;
    static final short[] yydgoto;
    static final short[] yysindex;
    static final short[] yyrindex;
    static final short[] yygindex;
    static final int YYTABLESIZE = 521;
    static short[] yytable;
    static short[] yycheck;
    static final short YYFINAL = 14;
    static final short YYMAXTOKEN = 270;
    static final String[] yyname;
    static final String[] yyrule;
    public int index;
    public int max;
    public String expr;
    public boolean now;
    private Object result;
    private LinkedList arg_level_list, arg_list;
    private RuleParse me;
    int yyn;
    int yym;
    int yystate;
    String yys;
    
    static {
        yylhs = new short[] { -1, 0, 1, 1, 2, 2, 3, 3, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 6, 6, 6, 6, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 12, 8, 11, 11, 9, 9, 13, 13, 14, 14, 14, 14, 14, 14, 14, 14, 10, 10, 10, 15, 15 };
        yylen = new short[] { 2, 1, 1, 3, 1, 3, 1, 3, 1, 3, 3, 3, 3, 3, 3, 1, 3, 3, 1, 3, 3, 3, 1, 4, 1, 2, 2, 2, 2, 2, 2, 2, 2, 0, 6, 1, 3, 1, 3, 1, 3, 1, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 1, 2 };
        yydefred = new short[] { 0, 49, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 24, 37, 32, 0, 31, 29, 26, 25, 27, 28, 30, 0, 0, 0, 39, 52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 0, 38, 0, 51, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19, 20, 21, 0, 0, 42, 43, 44, 45, 46, 47, 48, 40, 23, 0, 0, 34, 0, 0 };
        yydgoto = new short[] { 14, 34, 16, 17, 18, 19, 20, 21, 22, 23, 24, 93, 82, 36, 37, 39 };
        yysindex = new short[] { 143, 0, 0, 143, -107, 143, 143, 143, 143, 143, 143, 143, 143, 143, 0, -118, -24, -46, -57, 12, -35, 0, -63, 0, 0, 0, -8, 0, 0, 0, 0, 0, 0, 0, -118, 23, -33, 0, 0, 241, 143, 143, 143, 143, 143, 143, 143, 143, 143, 143, 143, 143, 143, 143, 143, 0, 143, 143, 143, 143, 143, 143, 143, 0, 143, 0, 0, -24, -46, -57, 12, 12, 12, 12, 12, 12, -35, -35, 0, 0, 0, -93, 143, 0, 0, 0, 0, 0, 0, 0, 0, 0, -118, -31, 0, 143, -118 };
        yyrindex = new short[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 36, 72, 300, 302, 49, 9, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -17, 192, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 337, 301, 312, 74, 100, 108, 148, 156, 182, 35, 60, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -15, 0, 0, 0, -11 };
        yygindex = new short[] { 0, 4, 11, -1, 14, 280, 17, 78, 457, 0, 55, 0, 0, 0, -39, 0 };
        yytable();
        yycheck();
        yyname = new String[] { "end-of-file", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "'!'", null, null, "'$'", "'%'", "'&'", null, "'('", "')'", "'*'", "'+'", "','", "'-'", null, "'/'", null, null, null, null, null, null, null, null, null, null, null, null, "'<'", "'='", "'>'", "'?'", "'@'", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "'['", null, "']'", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "'{'", "'|'", "'}'", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "IDENTIFIER", "STRING", "EQ", "NE", "LE", "GE", "DEF", "AGN", "APN", "ADE", "SUE", "MUE", "DIE", "MOE" };
        yyrule = new String[] { "$accept : top", "top : logical_or", "logical_or : logical_and", "logical_or : logical_or '|' logical_and", "logical_and : equality", "logical_and : logical_and '&' equality", "equality : relational_expr", "equality : equality '=' relational_expr", "relational_expr : additive_expr", "relational_expr : relational_expr '<' additive_expr", "relational_expr : relational_expr '>' additive_expr", "relational_expr : relational_expr LE additive_expr", "relational_expr : relational_expr GE additive_expr", "relational_expr : relational_expr EQ additive_expr", "relational_expr : relational_expr NE additive_expr", "additive_expr : multiplicative_expr", "additive_expr : additive_expr '+' multiplicative_expr", "additive_expr : additive_expr '-' multiplicative_expr", "multiplicative_expr : postfix", "multiplicative_expr : multiplicative_expr '*' postfix", "multiplicative_expr : multiplicative_expr '/' postfix", "multiplicative_expr : multiplicative_expr '%' postfix", "postfix : unary", "postfix : unary '[' logical_or ']'", "unary : primary", "unary : '@' unary", "unary : '%' unary", "unary : '$' unary", "unary : '?' unary", "unary : '*' unary", "unary : '!' unary", "unary : '-' unary", "unary : DEF unary", "$$1 :", "unary : '&' identifier '(' $$1 argument ')'", "argument : logical_or", "argument : argument ',' logical_or", "primary : identifier", "primary : '(' expression ')'", "expression : assignment", "expression : expression ',' assignment", "assignment : logical_or", "assignment : unary AGN assignment", "assignment : unary APN assignment", "assignment : unary ADE assignment", "assignment : unary SUE assignment", "assignment : unary MUE assignment", "assignment : unary DIE assignment", "assignment : unary MOE assignment", "identifier : IDENTIFIER", "identifier : STRING", "identifier : '{' name '}'", "name : postfix", "name : name postfix" };
    }
    
    void debug(final String msg) {
        if (yydebug) {
            System.out.println(msg);
        }
    }
    
    final void state_push(final int state) {
        try {
            ++stateptr;
            statestk[stateptr] = state;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            final int oldsize = statestk.length;
            final int newsize = oldsize * 2;
            final int[] newstack = new int[newsize];
            System.arraycopy(statestk, 0, newstack, 0, oldsize);
            (statestk = newstack)[stateptr] = state;
        }
    }
    
    final int state_pop() {
        return statestk[stateptr--];
    }
    
    final void state_drop(final int cnt) {
        stateptr -= cnt;
    }
    
    final int state_peek(final int relative) {
        return statestk[stateptr - relative];
    }
    
    final boolean init_stacks() {
        stateptr = -1;
        val_init();
        return true;
    }
    
    void dump_stacks(final int count) {
        System.out.println("=index==state====value=     s:" + stateptr + "  v:" + valptr);
        for (int i = 0; i < count; ++i) {
            System.out.println(" " + i + "    " + statestk[i] + "      " + valstk[i]);
        }
        System.out.println("======================");
    }
    
    final void val_init() {
        yyval = new Object();
        yylval = new Object();
        valptr = -1;
    }
    
    final void val_push(final Object val) {
        try {
            ++valptr;
            valstk[valptr] = val;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            final int oldsize = valstk.length;
            final int newsize = oldsize * 2;
            final Object[] newstack = new Object[newsize];
            System.arraycopy(valstk, 0, newstack, 0, oldsize);
            (valstk = newstack)[valptr] = val;
        }
    }
    
    final Object val_pop() {
        return valstk[valptr--];
    }
    
    final void val_drop(final int cnt) {
        valptr -= cnt;
    }
    
    final Object val_peek(final int relative) {
        return valstk[valptr - relative];
    }
    
    static void yytable() {
        RuleParse.yytable = new short[] { 91, 22, 53, 47, 15, 48, 40, 51, 63, 15, 94, 64, 52, 95, 41, 42, 13, 83, 84, 85, 86, 87, 88, 89, 41, 90, 35, 41, 54, 35, 36, 40, 55, 36, 22, 16, 1, 22, 22, 22, 68, 22, 22, 22, 22, 22, 22, 15, 22, 8, 15, 67, 15, 15, 15, 49, 69, 50, 81, 26, 17, 22, 22, 22, 22, 22, 76, 77, 0, 15, 15, 15, 2, 16, 13, 0, 16, 0, 16, 16, 16, 0, 0, 0, 0, 0, 92, 8, 0, 0, 8, 38, 0, 8, 22, 16, 16, 16, 17, 96, 14, 17, 15, 17, 17, 17, 0, 0, 11, 8, 8, 8, 13, 2, 54, 13, 2, 66, 13, 0, 17, 17, 17, 0, 22, 22, 22, 0, 16, 78, 79, 80, 0, 15, 13, 13, 13, 0, 14, 0, 0, 14, 8, 0, 14, 0, 11, 0, 12, 11, 1, 2, 11, 17, 0, 0, 9, 0, 0, 16, 14, 14, 14, 0, 0, 2, 0, 13, 11, 11, 11, 0, 0, 8, 0, 0, 11, 0, 0, 9, 7, 4, 10, 12, 17, 6, 12, 0, 5, 12, 0, 0, 12, 14, 9, 0, 2, 9, 13, 0, 9, 11, 43, 44, 45, 46, 10, 8, 12, 12, 12, 0, 0, 0, 0, 0, 9, 9, 9, 0, 10, 0, 0, 10, 14, 0, 10, 0, 0, 22, 22, 0, 11, 22, 22, 22, 22, 22, 0, 22, 0, 12, 10, 10, 10, 0, 0, 0, 0, 9, 0, 0, 22, 22, 22, 0, 0, 0, 22, 22, 22, 22, 22, 22, 22, 0, 13, 0, 15, 15, 15, 15, 12, 0, 11, 10, 0, 9, 7, 4, 9, 12, 0, 6, 0, 0, 5, 56, 57, 58, 59, 60, 61, 62, 16, 16, 16, 16, 0, 0, 4, 5, 6, 0, 10, 8, 10, 0, 8, 8, 8, 8, 7, 0, 0, 0, 22, 0, 0, 17, 17, 17, 17, 70, 71, 72, 73, 74, 75, 0, 0, 0, 0, 13, 13, 13, 13, 3, 4, 5, 6, 4, 5, 6, 4, 5, 6, 0, 0, 0, 7, 0, 0, 7, 0, 0, 7, 0, 0, 14, 14, 14, 14, 6, 13, 0, 65, 11, 11, 11, 11, 0, 0, 7, 0, 0, 0, 0, 3, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 5, 6, 0, 0, 0, 0, 1, 2, 0, 0, 0, 7, 3, 12, 12, 12, 12, 0, 0, 0, 0, 9, 9, 9, 9, 0, 0, 0, 0, 0, 4, 5, 6, 0, 0, 0, 3, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 10, 10, 10, 10, 0, 0, 0, 0, 0, 0, 22, 22, 22, 22, 0, 0, 0, 0, 0, 25, 3, 27, 28, 29, 30, 31, 32, 33, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 35, 35, 35, 35, 35, 35, 35, 0, 35 };
    }
    
    static void yycheck() {
        RuleParse.yycheck = new short[] { 93, 0, 37, 60, 0, 62, 124, 42, 41, 0, 41, 44, 47, 44, 38, 61, 123, 56, 57, 58, 59, 60, 61, 62, 41, 64, 41, 44, 91, 44, 41, 124, 40, 44, 33, 0, 0, 36, 37, 38, 41, 40, 41, 42, 43, 44, 45, 38, 47, 0, 41, 40, 43, 44, 45, 43, 42, 45, 54, 4, 0, 60, 61, 62, 63, 64, 49, 50, -1, 60, 61, 62, 0, 38, 0, -1, 41, -1, 43, 44, 45, -1, -1, -1, -1, -1, 82, 38, -1, -1, 41, 13, -1, 44, 93, 60, 61, 62, 38, 95, 0, 41, 93, 43, 44, 45, -1, -1, 0, 60, 61, 62, 38, 41, 91, 41, 44, 39, 44, -1, 60, 61, 62, -1, 123, 124, 125, -1, 93, 51, 52, 53, -1, 124, 60, 61, 62, -1, 38, -1, -1, 41, 93, -1, 44, -1, 38, -1, 0, 41, 257, 258, 44, 93, -1, -1, 0, -1, -1, 124, 60, 61, 62, -1, -1, 93, -1, 93, 60, 61, 62, -1, -1, 124, -1, -1, 33, -1, -1, 36, 37, 38, 0, 40, 124, 42, 38, -1, 45, 41, -1, -1, 44, 93, 38, -1, 124, 41, 124, -1, 44, 93, 259, 260, 261, 262, 63, 64, 60, 61, 62, -1, -1, -1, -1, -1, 60, 61, 62, -1, 38, -1, -1, 41, 124, -1, 44, -1, -1, 37, 38, -1, 124, 41, 42, 43, 44, 45, -1, 47, -1, 93, 60, 61, 62, -1, -1, -1, -1, 93, -1, -1, 60, 61, 62, -1, -1, -1, 257, 258, 259, 260, 261, 262, 263, -1, 123, -1, 259, 260, 261, 262, 124, -1, 33, 93, -1, 36, 37, 38, 124, 40, -1, 42, -1, -1, 45, 264, 265, 266, 267, 268, 269, 270, 259, 260, 261, 262, -1, -1, 0, 0, 0, -1, 63, 64, 124, -1, 259, 260, 261, 262, 0, -1, -1, -1, 124, -1, -1, 259, 260, 261, 262, 43, 44, 45, 46, 47, 48, -1, -1, -1, -1, 259, 260, 261, 262, 0, 38, 38, 38, 41, 41, 41, 44, 44, 44, -1, -1, -1, 38, -1, -1, 41, -1, -1, 44, -1, -1, 259, 260, 261, 262, 61, 123, -1, 125, 259, 260, 261, 262, -1, -1, 61, -1, -1, -1, -1, 41, -1, -1, 44, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 93, 93, 93, -1, -1, -1, -1, 257, 258, -1, -1, -1, 93, 263, 259, 260, 261, 262, -1, -1, -1, -1, 259, 260, 261, 262, -1, -1, -1, -1, -1, 124, 124, 124, -1, -1, -1, 93, -1, -1, -1, -1, -1, 124, -1, -1, -1, -1, 259, 260, 261, 262, -1, -1, -1, -1, -1, -1, 259, 260, 261, 262, -1, -1, -1, -1, -1, 3, 124, 5, 6, 7, 8, 9, 10, 11, 12, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 257, 258, -1, -1, -1, -1, 263, -1, -1, -1, -1, -1, -1, -1, -1, 56, 57, 58, 59, 60, 61, 62, -1, 64 };
    }
    
    public void init(final String str, final boolean multi, final boolean trace) {
        now = multi;
        expr = str;
        index = 0;
        max = expr.length();
        me = (trace ? this : null);
        arg_level_list = new LinkedList();
        result = null;
        if (yydebug) {
            debug("--- init parser ---");
        }
    }
    
    public Object parse() {
        yyparse();
        return result;
    }
    
    private void yyerror(final String mesg) {
        throw new ArrayIndexOutOfBoundsException(mesg);
    }
    
    private int yylex() {
        if (index >= max) {
            return 0;
        }
        char c = expr.charAt(index++);
        yytext = Character.toString(c);
        if (c == '\"') {
            while (index < max) {
                c = expr.charAt(index++);
                if (c == '\"') {
                    break;
                }
                yytext = yytext + c;
            }
            yylval = yytext;
            return 258;
        }
        if (c == '>') {
            yylval = yytext;
            if (index < max && expr.charAt(index) == '=') {
                ++index;
                return GE;
            }
            return c;
        }
        else if (c == '<') {
            yylval = yytext;
            if (index >= max) {
                return c;
            }
            final char v = expr.charAt(index);
            if (v == '=' || v == '<') {
                ++index;
                return (v == '=') ? LE : AGN;
            }
            return c;
        }
        else if (c == '+') {
            yylval = yytext;
            if (index >= max) {
                return c;
            }
            final char v = expr.charAt(index);
            if (v == '=' || v == '<') {
                ++index;
                return (v == '=') ? ADE : APN;
            }
            return c;
        }
        else if (c == '-') {
            yylval = yytext;
            if (index < max && expr.charAt(index) == '=') {
                ++index;
                return SUE;
            }
            return c;
        }
        else if (c == '*') {
            yylval = yytext;
            if (index < max && expr.charAt(index) == '=') {
                ++index;
                return MUE;
            }
            return c;
        }
        else if (c == '/') {
            yylval = yytext;
            if (index < max && expr.charAt(index) == '=') {
                ++index;
                return DIE;  //269 死
            }
            return c;
        }
        else if (c == '%') {
            yylval = yytext;
            if (index < max && expr.charAt(index) == '=') {
                ++index;
                return MOE;
            }
            return c;
        }
        else if (c == '!') {
            yylval = yytext;
            if (index < max && expr.charAt(index) == '=') {
                ++index;
                return NE;
            }
            return c;
        }
        else if (c == '=') {
            yylval = yytext;
            if (index < max && expr.charAt(index) == '=') {
                ++index;
                return EQ;
            }
            return c;
        }
        else if (c == '?') {
            yylval = yytext;
            if (index < max && expr.charAt(index) == '?') {
                ++index;
                return DEF;
            }
            return c;
        }
        else {
            if (c == '|' || c == '&' || c == '@' || c == '$' || c == ',' || c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}') {
                yylval = yytext;
                return c;
            }
            if (isIdentifier(c)) {
                while (index < max) {
                    c = expr.charAt(index);
                    if (!isIdentifier(c)) {
                        break;
                    }
                    yytext = String.valueOf(yytext) + c;
                    ++index;
                }
                yylval = yytext;
                return IDENTIFIER;
            }
            yyerror("illegal character");
            return -1;
        }
    }
    
    private boolean isIdentifier(final char c) {
        return c == '_' || c == '.' || Character.isDigit(c) || (Character.isLowerCase(c)
                | Character.isUpperCase(c)) || c > 'ÿ';
    }
    
    void yylexdebug(final int state, int ch) {
        String s = null;
        if (ch < 0) {
            ch = 0;
        }
        if (ch <= YYMAXTOKEN) {
            s = RuleParse.yyname[ch];
        }
        if (s == null) {
            s = "illegal-symbol";
        }
        debug("state " + state + ", reading " + ch + " (" + s + ")");
    }
    
    int yyparse() {
        init_stacks();
        yynerrs = 0;
        yyerrflag = 0;
        yychar = -1;
        state_push(yystate = 0);
        val_push(yylval);
        while (true) {
            boolean doaction = true;
            yyn = RuleParse.yydefred[yystate];
            while (yyn == 0) {
                if (yychar < 0) {
                    yychar = yylex();
                    if (yychar < 0) {
                        yychar = 0;
                    }
                }
                yyn = RuleParse.yysindex[yystate];
                if (yyn != 0 && (yyn += yychar) >= 0 && yyn <= 521 && RuleParse.yycheck[yyn] == yychar) {
                    state_push(yystate = RuleParse.yytable[yyn]);
                    val_push(yylval);
                    yychar = -1;
                    if (yyerrflag > 0) {
                        --yyerrflag;
                    }
                    doaction = false;
                    break;
                }
                yyn = RuleParse.yyrindex[yystate];
                if (yyn != 0 && (yyn += yychar) >= 0 && yyn <= 521 && RuleParse.yycheck[yyn] == yychar) {
                    yyn = RuleParse.yytable[yyn];
                    doaction = true;
                    break;
                }
                if (yyerrflag == 0) {
                    yyerror("syntax error");
                    ++yynerrs;
                }
                Label_0468: {
                    if (yyerrflag < 3) {
                        yyerrflag = 3;
                        while (stateptr >= 0) {
                            yyn = RuleParse.yysindex[state_peek(0)];
                            if (yyn != 0 && (yyn += 256) >= 0 && yyn <= 521 && RuleParse.yycheck[yyn] == 256) {
                                state_push(yystate = RuleParse.yytable[yyn]);
                                val_push(yylval);
                                doaction = false;
                                break Label_0468;
                            }
                            if (stateptr < 0) {
                                yyerror("Stack underflow. aborting...");
                                return 1;
                            }
                            state_pop();
                            val_pop();
                        }
                        yyerror("stack underflow. aborting...");
                        return 1;
                    }
                    if (yychar == 0) {
                        return 1;
                    }
                    yychar = -1;
                }
                yyn = RuleParse.yydefred[yystate];
            }
            if (!doaction) {
                continue;
            }
            yym = RuleParse.yylen[yyn];
            if (yym > 0) {
                yyval = val_peek(yym - 1);
            }
            switch (yyn) {
                case 1: {
                    result = val_peek(0);
                    break;
                }
                case 3: {
                    yyval = RuleEntry.evalOr(me, val_peek(2), val_peek(0));
                    break;
                }
                case 5: {
                    yyval = RuleEntry.evalAnd(me, val_peek(2), val_peek(0));
                    break;
                }
                case 7: {
                    yyval = RuleEntry.setContainment(me, val_peek(2), val_peek(0));
                    break;
                }
                case 9: {
                    yyval = RuleEntry.evalRel(me, val_peek(2), val_peek(0), RuleEntry.OP_LT);
                    break;
                }
                case 10: {
                    yyval = RuleEntry.evalRel(me, val_peek(2), val_peek(0), RuleEntry.OP_GT);
                    break;
                }
                case 11: {
                    yyval = RuleEntry.evalRel(me, val_peek(2), val_peek(0), RuleEntry.OP_LE);
                    break;
                }
                case 12: {
                    yyval = RuleEntry.evalRel(me, val_peek(2), val_peek(0), RuleEntry.OP_GE);
                    break;
                }
                case 13: {
                    yyval = RuleEntry.evalRel(me, val_peek(2), val_peek(0), RuleEntry.OP_EQ);
                    break;
                }
                case 14: {
                    yyval = RuleEntry.evalRel(me, val_peek(2), val_peek(0), RuleEntry.OP_NE);
                    break;
                }
                case 16: {
                    yyval = RuleEntry.evalExpr(me, val_peek(2), val_peek(0), RuleEntry.OP_ADD);
                    break;
                }
                case 17: {
                    yyval = RuleEntry.evalExpr(me, val_peek(2), val_peek(0), RuleEntry.OP_SUB);
                    break;
                }
                case 19: {
                    yyval = RuleEntry.evalExpr(me, val_peek(2), val_peek(0), RuleEntry.OP_MUL);
                    break;
                }
                case 20: {
                    yyval = RuleEntry.evalExpr(me, val_peek(2), val_peek(0), RuleEntry.OP_DIV);
                    break;
                }
                case 21: {
                    yyval = RuleEntry.evalExpr(me, val_peek(2), val_peek(0), RuleEntry.OP_MOD);
                    break;
                }
                case 23: {
                    yyval = RuleEntry.indexValue(me, val_peek(3), val_peek(1));
                    break;
                }
                case 25: {
                    yyval = RuleEntry.evalVariable(me, '@', val_peek(0), false);
                    break;
                }
                case 26: {
                    yyval = RuleEntry.evalVariable(me, '%', val_peek(0), false);
                    break;
                }
                case 27: {
                    yyval = RuleEntry.evalVariable(me, '$', val_peek(0), false);
                    break;
                }
                case 28: {
                    yyval = RuleEntry.evalBoolean(me, val_peek(0));
                    break;
                }
                case 29: {
                    yyval = RuleEntry.evalHasEntry(me, val_peek(0), now);
                    break;
                }
                case 30: {
                    yyval = RuleEntry.evalNot(me, val_peek(0));
                    break;
                }
                case 31: {
                    yyval = RuleEntry.evalExpr(me, "0", val_peek(0), RuleEntry.OP_SUB);
                    break;
                }
                case 32: {
                    yyval = RuleEntry.evalDefined(me, val_peek(0));
                    break;
                }
                case 33: {
                    arg_list = new LinkedList();
                    arg_level_list.addFirst(arg_list);
                    break;
                }
                case 34: {
                    yyval = RuleEntry.evalFunction(me, val_peek(4), arg_list);
                    arg_level_list.removeFirst();
                    arg_list = (arg_level_list.isEmpty() ? null : (LinkedList) arg_level_list.getFirst());
                    break;
                }
                case 35: {
                    arg_list.addLast(val_peek(0));
                    break;
                }
                case 36: {
                    arg_list.addLast(val_peek(0));
                    break;
                }
                case 38: {
                    yyval = val_peek(1);
                    break;
                }
                case 40: {
                    yyval = val_peek(0);
                    break;
                }
                case 42: {
                    yyval = RuleEntry.evalAssign(me, val_peek(2), val_peek(0), now);
                    break;
                }
                case 43: {
                    Object obj = RuleEntry.evalVariable(me, '$', val_peek(2), true);
                    if (obj == null) {
                        obj = new LinkedHashSet();
                    }
                    yyval = RuleEntry.evalAssign(me, val_peek(2), RuleEntry.setUnion(obj, val_peek(0)), now);
                    break;
                }
                case 44: {
                    Object obj = RuleEntry.evalVariable(me, '$', val_peek(2), true);
                    if (obj == null) {
                        obj = "0";
                    }
                    yyval = RuleEntry.evalAssign(me, val_peek(2),
                            RuleEntry.evalExpr(me, obj, val_peek(0), RuleEntry.OP_ADD), now);
                    break;
                }
                case 45: {
                    Object obj = RuleEntry.evalVariable(me, '$', val_peek(2), true);
                    if (obj == null) {
                        obj = "0";
                    }
                    yyval = RuleEntry.evalAssign(me, val_peek(2),
                            RuleEntry.evalExpr(me, obj, val_peek(0), RuleEntry.OP_SUB), now);
                    break;
                }
                case 46: {
                    Object obj = RuleEntry.evalVariable(me, '$', val_peek(2), true);
                    if (obj == null) {
                        obj = "0";
                    }
                    yyval = RuleEntry.evalAssign(me, val_peek(2),
                            RuleEntry.evalExpr(me, obj, val_peek(0), RuleEntry.OP_MUL), now);
                    break;
                }
                case 47: {
                    Object obj = RuleEntry.evalVariable(me, '$', val_peek(2), true);
                    if (obj == null) {
                        obj = "0";
                    }
                    yyval = RuleEntry.evalAssign(me, val_peek(2),
                            RuleEntry.evalExpr(me, obj, val_peek(0), RuleEntry.OP_DIV), now);
                    break;
                }
                case 48: {
                    Object obj = RuleEntry.evalVariable(me, '$', val_peek(2), true);
                    if (obj == null) {
                        obj = "0";
                    }
                    yyval = RuleEntry.evalAssign(me, val_peek(2),
                            RuleEntry.evalExpr(me, obj, val_peek(0), RuleEntry.OP_MOD), now);
                    break;
                }
                case 51: {
                    yyval = val_peek(1);
                    break;
                }
                case 53: {
                    yyval = RuleEntry.concatString(me, val_peek(1), val_peek(0));
                    break;
                }
            }
            state_drop(yym);
            yystate = state_peek(0);
            val_drop(yym);
            yym = RuleParse.yylhs[yyn];
            if (yystate == 0 && yym == 0) {
                state_push(yystate = 14);
                val_push(yyval);
                if (yychar < 0) {
                    yychar = yylex();
                    if (yychar < 0) {
                        yychar = 0;
                    }
                }
                if (yychar == 0) {
                    return 0;
                }
            }
            else {
                yyn = RuleParse.yygindex[yym];
                if (yyn != 0 && (yyn += yystate) >= 0 && yyn <= 521 && RuleParse.yycheck[yyn] == yystate) {
                    yystate = RuleParse.yytable[yyn];
                }
                else {
                    yystate = RuleParse.yydgoto[yym];
                }
                state_push(yystate);
                val_push(yyval);
            }
        }
    }
    
    public void run() {
        yyparse();
    }
    
    public RuleParse() {
        statestk = new int[256];
        valstk = new Object[256];
    }
    
    public RuleParse(final boolean debugMe) {
        statestk = new int[256];
        valstk = new Object[256];
        yydebug = debugMe;
    }
}
