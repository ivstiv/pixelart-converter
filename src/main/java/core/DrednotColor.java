package core;

import com.dajudge.colordiff.RgbColor;

import java.awt.*;

public class DrednotColor extends RgbColor{

    private int id, rgbValue;

    public DrednotColor(int r, int g, int b, int id) {
        super(r,g,b);
        this.id = id;
        rgbValue = (r << 16) | (g << 8) | b;
    }

    // to make it compatible with BufferedImage and the library for color distance
    public DrednotColor(int rgbValue, int id) {
        super(
                rgbValue & 0xff,
                (rgbValue & 0xff00) >> 8,
                (rgbValue & 0xff0000) >> 16
        );
        this.rgbValue = rgbValue;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getRGBValue() {
        return rgbValue;
    }
}
