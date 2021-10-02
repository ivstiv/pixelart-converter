package core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class PixelArt {

    private final BufferedImage image;
    private final ColorPalette colorPalette;
    private final ColorDistanceCalculator calculator;

    public PixelArt(BufferedImage image, ColorSpace colorSpace, double chromaOffset, ColorPalette palette) throws IOException {
        this.image = image;
        //this.colorPalette = "Imported";
        this.colorPalette = palette;
        //this.drednotColorPalette = customPalette;
        this.calculator = new ColorDistanceCalculator(colorSpace, chromaOffset);
    }


    public Color[][] getOriginalColors() {
        Color[][] rgbColors = new Color[image.getWidth()][image.getHeight()];
        for(int j = 0; j < image.getHeight(); j++) {
            for(int i = 0; i < image.getWidth(); i++) {

                Color pixel = new Color(image.getRGB(i, j), true);
                int red = pixel.getRed();
                int blue = pixel.getBlue();
                int green = pixel.getGreen();
                System.out.println("alpha" + pixel.getAlpha());

                rgbColors[i][j] = new Color(red, green, blue, pixel.getAlpha());
            }
        }
        return rgbColors;
    }

    public DrednotColor[][] getDrednotColors() {
        Color[][] originalColors = getOriginalColors();
        DrednotColor[][] drednotColors = new DrednotColor[originalColors.length][originalColors[0].length];

        for(int j = 0; j < originalColors.length; j++) {
            for (int i = 0; i < originalColors[j].length; i++) {
                DrednotColor color = new DrednotColor(
                        originalColors[j][i].getRed(),
                        originalColors[j][i].getGreen(),
                        originalColors[j][i].getBlue(),
                        originalColors[j][i].getAlpha(),
                        "-1"
                );

                drednotColors[j][i] = calculator.getClosestColor(color, colorPalette);
            }
        }
        return drednotColors;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void resize(String outputImagePath, int scaledWidth, int scaledHeight)
            throws IOException {
        BufferedImage inputImage = this.image;

        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath
                .lastIndexOf(".") + 1);

        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }
}
