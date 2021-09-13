package core;

import com.dajudge.colordiff.RgbColor;

import java.awt.*;

public class DrednotColor extends RgbColor{

    private int rgbValue;
    private String id;
    private int[] lab;
    private float[] hsv;

    public DrednotColor(int r, int g, int b, String id) {
        super(r,g,b);
        this.id = id;
        rgbValue = (r << 16) | (g << 8) | b;
    }

    // to make it compatible with BufferedImage and the library for color distance
    public DrednotColor(int rgbValue, String id) {
        super(
                rgbValue & 0xff,
                (rgbValue & 0xff00) >> 8,
                (rgbValue & 0xff0000) >> 16
        );
        this.rgbValue = rgbValue;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public int getRGBValue() {
        return rgbValue;
    }

    public float getH() {
        if(hsv == null)
            hsv = rgb2hsv();
        return hsv[0];
    }

    public float getS() {
        if(hsv == null)
            hsv = rgb2hsv();
        return hsv[1];
    }

    public float getV() {
        if(hsv == null)
            hsv = rgb2hsv();
        return hsv[2];
    }

    private float[] rgb2hsv(){
        float[] hsv = new float[3];
        float r = (float) super.r / 255f;
        float g = (float) super.g / 255f;
        float b = (float) super.b / 255f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        // Hue
        if (max == min){
            hsv[0] = 0;
        }
        else if (max == r){
            hsv[0] = ((g - b) / delta) * 60f;
        }
        else if (max == g){
            hsv[0] = ((b - r) / delta + 2f) * 60f;
        }
        else if (max == b){
            hsv[0] = ((r - g) / delta + 4f) * 60f;
        }

        // Saturation
        if (delta == 0)
            hsv[1] = 0;
        else
            hsv[1] = delta / max;

        //Value
        hsv[2] = max;

        return hsv;
    }

    public float getL() {
        if(lab == null)
            lab = rgb2lab();
        return lab[0];
    }

    public float getA() {
        if(lab == null)
            lab = rgb2lab();
        return lab[1];
    }

    public float getB() {
        if(lab == null)
            lab = rgb2lab();
        return lab[2];
    }

    // I have no idea how this works :D
    // copied from here: https://stackoverflow.com/questions/4593469/java-how-to-convert-rgb-color-to-cie-lab
    public int[] rgb2lab() {
        //http://www.brucelindbloom.com

        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;
        float eps = 216.f/24389.f;
        float k = 24389.f/27.f;

        float Xr = 0.964221f;  // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        r = (float) super.r/255.f; //R 0..1
        g = (float) super.g/255.f; //G 0..1
        b = (float) super.b/255.f; //B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r/12;
        else
            r = (float) Math.pow((r+0.055)/1.055,2.4);

        if (g <= 0.04045)
            g = g/12;
        else
            g = (float) Math.pow((g+0.055)/1.055,2.4);

        if (b <= 0.04045)
            b = b/12;
        else
            b = (float) Math.pow((b+0.055)/1.055,2.4);


        X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
        Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
        Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;

        // XYZ to Lab
        xr = X/Xr;
        yr = Y/Yr;
        zr = Z/Zr;

        if ( xr > eps )
            fx =  (float) Math.pow(xr, 1/3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if ( yr > eps )
            fy =  (float) Math.pow(yr, 1/3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if ( zr > eps )
            fz =  (float) Math.pow(zr, 1/3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        Ls = ( 116 * fy ) - 16;
        as = 500*(fx-fy);
        bs = 200*(fy-fz);

        int[] lab = new int[3];
        lab[0] = (int) (2.55*Ls + .5);
        lab[1] = (int) (as + .5);
        lab[2] = (int) (bs + .5);
        return lab;
    }
}
