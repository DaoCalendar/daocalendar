package com.saltfun.daocalendar.marsroam.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DaoAstrofont {

    protected static Font astroFont;

    static {
        InputStream s = null;
        try {
            s = DaoAstrofont.class.getResourceAsStream("/AMATERU.ttf");
            astroFont = Font.createFont(Font.TRUETYPE_FONT, s );
            System.out.println("导入字体"); //Load astrology fonts
        } catch (IOException e) {
            Logger.getLogger(DaoAstrofont.class.getName()).log(Level.SEVERE, null, e);
        } catch ( FontFormatException e ) {
            Logger.getLogger(DaoAstrofont.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try { s.close(); } catch(Exception ex) { }
        }
    }

    public static Font getFont(float size) {
        return astroFont.deriveFont(size);
    }


    public static Font getFont(int style, float size) {
        return astroFont.deriveFont(style, size);
    }


    public static boolean registEnvironment() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .registerFont(astroFont);
    }

    private DaoAstrofont() {
    }
}
