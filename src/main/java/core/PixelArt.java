package core;

import com.dajudge.colordiff.RgbColor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PixelArt {

    private final BufferedImage image;
    private final List<RgbColor> drednotColorPalette;
    private final ColorDistanceCalculator calculator;
    private final String colorPalette;


    public PixelArt(BufferedImage image, ColorSpace colorSpace, double chromaOffset, String colorPalette) throws IOException {
        this.image = image;
        this.colorPalette = colorPalette;
        this.drednotColorPalette = initialiseDrednotColors();
        this.calculator = new ColorDistanceCalculator(colorSpace, chromaOffset);
    }

    public PixelArt(BufferedImage image, ColorSpace colorSpace, double chromaOffset, List<RgbColor> customPalette) throws IOException {
        this.image = image;
        this.colorPalette = "Imported";
        this.drednotColorPalette = customPalette;
        this.calculator = new ColorDistanceCalculator(colorSpace, chromaOffset);
    }

    private List<RgbColor> initialiseDrednotColors() {
        BufferedReader bufferedReader = null;
        try {
            // i am reading in the file as stream and writing it to a file because of this:
            // https://stackoverflow.com/questions/43811764/java-getclass-getclassloader-getresourcepath-fails-inside-maven-shaded-ja
            InputStream is = getClass().getClassLoader().getResourceAsStream(this.colorPalette);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            File tempFile = File.createTempFile("aaa", "aaa", null);
            OutputStream fos = new FileOutputStream(tempFile);
            fos.write(buffer);

            bufferedReader = new BufferedReader(new FileReader(tempFile));
            String jsonData = bufferedReader.lines().collect(Collectors.joining());

            // deserialize the data
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(jsonData);
            JsonArray colorsJson = tradeElement.getAsJsonArray();

            Gson gson = new Gson();
            List<RgbColor> colors = new ArrayList<>();
            for(JsonElement el : colorsJson) {
                int id = el.getAsJsonObject().get("ID").getAsInt();
                int r = el.getAsJsonObject().get("R").getAsInt();
                int g = el.getAsJsonObject().get("G").getAsInt();
                int b = el.getAsJsonObject().get("B").getAsInt();
                colors.add(new DrednotColor(r,g,b,id));
            }
            return colors;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Color[][] getOriginalColors() {
        Color[][] rgbColors = new Color[image.getWidth()][image.getHeight()];
        for(int j = 0; j < image.getHeight(); j++) {
            for(int i = 0; i < image.getWidth(); i++) {
                int color = image.getRGB(i,j);
                int blue = color & 0xff;
                int green = (color & 0xff00) >> 8;
                int red = (color & 0xff0000) >> 16;
                //System.out.println("Pixel x"+i+" y"+j+":");
                //System.out.println("R:"+red+" G:"+green+" B:"+blue);
                rgbColors[i][j] = new Color(red, green, blue);
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
                        -1
                );

                drednotColors[j][i] = calculator.getClosestColor(color, drednotColorPalette);
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
