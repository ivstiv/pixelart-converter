package core;

import com.dajudge.colordiff.ColorDiff;
import com.dajudge.colordiff.RgbColor;

import java.util.List;

public class ColorDistanceCalculator {

    private final ColorSpace colorSpace;
    private double chromaOffset = 0;

    public ColorDistanceCalculator(ColorSpace colorSpace) {
        this.colorSpace = colorSpace;
    }
    public ColorDistanceCalculator(ColorSpace colorSpace, double chromaOffset) {
        this.chromaOffset = chromaOffset;
        this.colorSpace = colorSpace;
    }

    public DrednotColor getClosestColor(RgbColor color, List<RgbColor> palette) {
        switch (colorSpace) {
            case CIEDE2000:
                return (DrednotColor) ColorDiff.closest(color, palette);
            case RGB:
                return shortestDistanceRGB(color, palette);
            case HSV:
                return shortestDistanceHSV(color, palette);
            case CIELAB_76:
                return shortestDistanceCIE76(color, palette);
            case CIELAB_94:
                return shortestDistanceCIE94(color, palette);
            default:
                throw new RuntimeException("Invalid color space!");
        }
    }

    private DrednotColor shortestDistanceRGB(RgbColor color, List<RgbColor> palette) {
        double shortestDistance = -1;
        RgbColor closestColor = null;
        for(RgbColor paletteColor : palette) {
            if(shortestDistance == -1) {
                shortestDistance = euclideanDistanceRGB(color, paletteColor);
                closestColor = paletteColor;
            }else{
                double newDistance = euclideanDistanceRGB(color, paletteColor);
                if(newDistance < shortestDistance) {
                    shortestDistance = newDistance;
                    closestColor = paletteColor;
                }
            }
        }
        return (DrednotColor) closestColor;
    }

    private DrednotColor shortestDistanceHSV(RgbColor color, List<RgbColor> palette) {
        double shortestDistance = -1;
        RgbColor closestColor = null;
        for(RgbColor paletteColor : palette) {
            if(shortestDistance == -1) {
                shortestDistance = euclideanDistanceHSV(color, paletteColor);
                closestColor = paletteColor;
            }else{
                double newDistance = euclideanDistanceHSV(color, paletteColor);
                if(newDistance < shortestDistance) {
                    shortestDistance = newDistance;
                    closestColor = paletteColor;
                }
            }
        }
        return (DrednotColor) closestColor;
    }

    private DrednotColor shortestDistanceCIE76(RgbColor color, List<RgbColor> palette) {
        double shortestDistance = -1;
        RgbColor closestColor = null;
        for(RgbColor paletteColor : palette) {
            if(shortestDistance == -1) {
                shortestDistance = euclideanDistanceCIE76(color, paletteColor);
                closestColor = paletteColor;
            }else{
                double newDistance = euclideanDistanceCIE76(color, paletteColor);
                if(newDistance < shortestDistance) {
                    shortestDistance = newDistance;
                    closestColor = paletteColor;
                }
            }
        }
        return (DrednotColor) closestColor;
    }

    private DrednotColor shortestDistanceCIE94(RgbColor color, List<RgbColor> palette) {
        double shortestDistance = -1;
        RgbColor closestColor = null;
        for(RgbColor paletteColor : palette) {
            if(shortestDistance == -1) {
                shortestDistance = euclideanDistanceCIE94(color, paletteColor);
                closestColor = paletteColor;
            }else{
                double newDistance = euclideanDistanceCIE94(color, paletteColor);
                if(newDistance < shortestDistance) {
                    shortestDistance = newDistance;
                    closestColor = paletteColor;
                }
            }
        }
        return (DrednotColor) closestColor;
    }

    public double euclideanDistanceRGB(RgbColor c1, RgbColor c2) {
        return Math.sqrt(Math.pow(c1.r-c2.r, 2)+Math.pow(c1.g-c2.g, 2)+Math.pow(c1.b-c2.b, 2));
    }

    public double euclideanDistanceHSV(RgbColor color1, RgbColor color2) {
        DrednotColor c1 = (DrednotColor) color1;
        DrednotColor c2 = (DrednotColor) color2;
        double dh = Math.min(Math.abs(c1.getH()-c2.getH()), 360-Math.abs(c1.getH()-c2.getH())) / 180.0;
        double ds = Math.abs(c1.getS()-c2.getS());
        double db = Math.abs(c1.getV()-c2.getV()) / 255.0;
        return Math.sqrt(dh*dh+ds*ds+db*db);
    }

    // formula from here: https://en.wikipedia.org/wiki/Color_difference
    public double euclideanDistanceCIE76(RgbColor color1, RgbColor color2) {
        DrednotColor c1 = (DrednotColor) color1;
        DrednotColor c2 = (DrednotColor) color2;
        return Math.sqrt(Math.pow(c1.getL()-c2.getL(), 2)+Math.pow(c1.getA()-c2.getA(), 2)+Math.pow(c1.getB()-c2.getB(), 2));
    }

    // formula from here: https://en.wikipedia.org/wiki/Color_difference
    public double euclideanDistanceCIE94(RgbColor color1, RgbColor color2) {
        DrednotColor c1 = (DrednotColor) color1;
        DrednotColor c2 = (DrednotColor) color2;

        double dL = c1.getL() - c2.getL();
        double C1 = Math.sqrt(Math.pow(c1.getA(), 2)+Math.pow(c1.getB(), 2));
        double C2 = Math.sqrt(Math.pow(c2.getA(), 2)+Math.pow(c2.getB(), 2));
        /*
            da, db, dC got changed to Sqrt(pow()) because they were giving negative values
            which were causing NaN error int Sqrt method
         */
        double dC = Math.sqrt(Math.pow(C1-C2, 2))*this.chromaOffset;
        double da = Math.sqrt(Math.pow(c1.getA()-c2.getA(), 2));
        double db = Math.sqrt(Math.pow(c1.getB()-c2.getB(), 2));
        double dH = Math.sqrt(da+db-dC);
        double Sc = 1 + 0.045*C1;
        double Sh = 1 + 0.015*C1;

        return Math.sqrt(Math.pow(dL/1*1, 2) + Math.pow(dC/1*Sc, 2) + Math.pow(dH/1*Sh, 2))+1;
    }
}
