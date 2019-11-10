package core;

import com.dajudge.colordiff.ColorDiff;
import com.dajudge.colordiff.RgbColor;

import java.util.List;

public class ColorDistanceCalculator {

    private final ColorSpace colorSpace;

    public ColorDistanceCalculator(ColorSpace colorSpace) {
        this.colorSpace = colorSpace;
    }

    public DrednotColor getClosestColor(RgbColor color, List<RgbColor> palette) {
        switch (colorSpace) {
            case CIEDE2000:
                return (DrednotColor) ColorDiff.closest(color, palette);
            case RGB:
                return euclideanDistanceRGB(color, palette);
            case HSB:
                return euclideanDistanceHSB(color, palette);
            default:
                throw new RuntimeException("Invalid color space!");
        }
    }

    private DrednotColor euclideanDistanceRGB(RgbColor color, List<RgbColor> palette) {
        double shortestDistance = -1;
        RgbColor closestColor = null;
        for(RgbColor paletteColor : palette) {
            if(shortestDistance == -1) {
                shortestDistance = euclideanDistanceBetween(color, paletteColor);
                closestColor = paletteColor;
            }else{
                double newDistance = euclideanDistanceBetween(color, paletteColor);
                if(newDistance < shortestDistance) {
                    shortestDistance = newDistance;
                    closestColor = paletteColor;
                }
            }
        }
        return (DrednotColor) closestColor;
    }

    private DrednotColor euclideanDistanceHSB(RgbColor color, List<RgbColor> palette) {
        return null;
    }

    public double euclideanDistanceBetween(RgbColor c1, RgbColor c2) {
        return Math.sqrt(Math.pow(c1.r-c2.r, 2)+Math.pow(c1.g-c2.g, 2)+Math.pow(c1.b-c2.b, 2));
    }
}
