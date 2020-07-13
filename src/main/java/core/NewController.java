package core;

import com.dajudge.colordiff.RgbColor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NewController implements Initializable {

    private String importImagePath = null;
    @FXML ImageView imagePreview;
    @FXML Button zoomInButton, zoomOutButton, convertButton, showOriginalButton;
    // you will not find these groups in SceneBuilder.. check the fxml file :D
    @FXML ToggleGroup colorSpaceGroup, colorPaletteGroup;
    @FXML CheckBox showColorID;
    @FXML TextField scaleRatio, fontSize, chromaOffset;
    @FXML Label statusLabel;
    private DisplayedImage displayedImage;
    private List<RgbColor> customPalette = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        zoomInButton.setOnAction(event -> {
            displayedImage.zoomIn();
        });

        zoomOutButton.setOnAction(event -> {
            displayedImage.zoomOut();
        });

        showOriginalButton.setOnAction(event -> {
            this.displayedImage.showOriginalImage();
        });

        convertButton.setOnAction(event -> {
            if(this.importImagePath == null) {
                showWarning("You need to import an image first!");
                return;
            }

            setStatus("Status: Converting. . . (The program might seem unresponsive.)");
            convertButton.setDisable(true);
            showOriginalButton.setDisable(true);

            Thread convertingThread = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                try {
                    BufferedImage convertedImage = getConvertedImage();
                    // if the image is too big the heap fills up and ugly things happen..
                    // so this null check is required
                    if(convertedImage != null)
                        displayedImage.setConvertedImage(convertedImage);
                } catch (OutOfMemoryError | IOException e) {
                    e.printStackTrace();
                    setStatus("Error: Couldn't convert the image. May be it is too big?");
                    convertButton.setDisable(false);
                    showOriginalButton.setDisable(false);
                    return;
                }
                long endTime = System.currentTimeMillis();
                long elapsedSeconds = (endTime-startTime)/1000;
                setStatus("Status: Converted in "+elapsedSeconds+"s.");
                convertButton.setDisable(false);
                showOriginalButton.setDisable(false);
            });
            convertingThread.start();
        });
    }

    private BufferedImage getConvertedImage() throws IOException {
        RadioMenuItem selectedRadioButton = (RadioMenuItem) colorSpaceGroup.getSelectedToggle();
        String colorSpace = selectedRadioButton.getText();

        double chromaOffsetValue = 0.2;
        if(isNumeric(chromaOffset.getText())) {
            chromaOffsetValue = Double.parseDouble(chromaOffset.getText());
        }

        String colorPalettePath = getColorPalettePath();
        PixelArt pixelart;
        if(colorPalettePath.equalsIgnoreCase("Imported")) {
            pixelart = new PixelArt(displayedImage.getOriginalImage(), ColorSpace.valueOf(colorSpace), chromaOffsetValue, customPalette);
        }else{
            pixelart = new PixelArt(displayedImage.getOriginalImage(), ColorSpace.valueOf(colorSpace), chromaOffsetValue, getColorPalettePath());
        }

        DrednotColor[][] colors = pixelart.getDrednotColors();
        BufferedImage drednotImage = new BufferedImage(colors.length, colors[0].length, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[x].length; y++) {
                drednotImage.setRGB(x, y, colors[x][y].getRGBValue());
            }
        }

        int scale = 1;
        int fontSizeValue = 1;
        if(isNumeric(scaleRatio.getText())) {
            scale = Integer.parseInt(scaleRatio.getText());
        }
        if(isNumeric(fontSize.getText())) {
            fontSizeValue = Integer.parseInt(fontSize.getText());
        }
        BufferedImage displayImage = new BufferedImage(colors.length*scale, colors[0].length*scale, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = displayImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(drednotImage, 0, 0, displayImage.getWidth(), displayImage.getHeight(), null);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSizeValue));

        // i need a calculator here as well to put white text for black tiles
        if(showColorID.isSelected()) {
            ColorDistanceCalculator calculator = new ColorDistanceCalculator(ColorSpace.RGB);
            for (int x = 0; x < colors.length; x++) {
                for (int y = 0; y < colors[x].length; y++) {
                    double distance = calculator.euclideanDistanceRGB(colors[x][y], new RgbColor(0,0,0));
                    if(distance < 50) {
                        g2.setColor(Color.lightGray);
                    }else{
                        g2.setColor(Color.black);
                    }
                    g2.drawString(colors[x][y].getId()+"", (float) (x*scale), (float) (y*scale+0.7*scale));
                }
            }
        }
        g2.dispose();

        return displayImage;
    }

    private String getColorPalettePath() {
        RadioMenuItem selectedRadioButton = (RadioMenuItem) colorPaletteGroup.getSelectedToggle();
        String colorPalette = selectedRadioButton.getText();

        switch(colorPalette) {
            case "Drednot":
                return "palettes/Drednot.json";
            case "Faber Castell 36":
                return "palettes/Faber_Castell_36.json";
            case "Black and White":
                return "palettes/BlackWhite.json";
            case "RGB":
                return "palettes/RGB.json";
            case "Imported":
                return "Imported";
            default:
                return "palettes/Drednot.json";
        }
    }

    public void importPalette() throws FileNotFoundException {
        String path = promptImportPalettePath();

        if(path != null) {
            File tempFile = new File(path);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile));
            String jsonData = bufferedReader.lines().collect(Collectors.joining());

            // deserialize the data
            JsonParser parser = new JsonParser();
            JsonElement tradeElement = parser.parse(jsonData);
            JsonArray colorsJson = tradeElement.getAsJsonArray();

            Gson gson = new Gson();
            for(JsonElement el : colorsJson) {
                int id = el.getAsJsonObject().get("ID").getAsInt();
                int r = el.getAsJsonObject().get("R").getAsInt();
                int g = el.getAsJsonObject().get("G").getAsInt();
                int b = el.getAsJsonObject().get("B").getAsInt();
                customPalette.add(new DrednotColor(r, g, b, id));
            }
            showWarning("Custom palette has been imported!\nChange the palette from the Modify menu to \"Imported\" in order for it to be applied.");
        }
    }

    private String promptImportPalettePath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select to import a palette");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Data files", "*.json")
        );
        File file = chooser.showOpenDialog(new Stage());
        // file is null when user cancels the selection
        return file == null ? null : file.toPath().toString();
    }

    public void resize() throws IOException {
        // resize only if there is imported image
        if(this.importImagePath == null) {
            showWarning("You need to import an image first!");
            return;
        }

        String newSize = promptResizeImage();
        if(newSize.isEmpty()) return;
        // regex matches any positive "intxint"
        if(!newSize.matches("\\d+x\\d+")) {
            showWarning("Invalid size format!");
            return;
        }

        String widthXheight[] = newSize.split("x");
        BufferedImage newImage = getResizedImage(
                this.importImagePath,
                Integer.parseInt(widthXheight[0]),
                Integer.parseInt(widthXheight[1])
        );

        displayedImage.setOriginalImage(newImage);
        setStatus("Status: The original image has been resized to "+newSize+".");
    }

    private String promptResizeImage() {
        TextInputDialog resizePrompt = new TextInputDialog();
        resizePrompt.setContentText("New size (Width)x(Height):");
        resizePrompt.setHeaderText("Change the size of the original image.\nExample: 80x80");
        resizePrompt.setTitle("Resize Image");
        Optional<String> result = resizePrompt.showAndWait();
        if(result.isPresent()) {
            return result.get().trim();
        }
        return "";
    }

    private BufferedImage getResizedImage(String imagePath, int scaledWidth, int scaledHeight) throws IOException {
        BufferedImage inputImage = ImageIO.read(new File(imagePath));

        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        return outputImage;

        // extracts extension of output file
        //String formatName = outputImagePath.substring(outputImagePath.lastIndexOf(".") + 1);

        // writes to output file
        //ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }

    // this is being called from the UI Import menu
    public void importImage() throws IOException {
        this.importImagePath = promptImportImagePath();
        if(this.importImagePath != null) {
            System.out.println("Imported image:"+importImagePath);
            setStatus("Status: Ready to convert.");
            // setup the preview
            //imagePreview.setImage(new Image("file:///"+importImagePath));
            displayedImage = new DisplayedImage(imagePreview);
            //displayedImage.setOriginalImage(new Image("file:///"+importImagePath));
            displayedImage.setOriginalImage(ImageIO.read(new File(importImagePath)));
            displayedImage.setup();
        }
    }


    private String promptImportImagePath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select to import an image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png")
        );
        File file = chooser.showOpenDialog(new Stage());
        // file is null when user cancels the selection
        return file == null ? null : file.toPath().toString();
    }


    // this is being called from the UI Export menu
    public void exportImage() throws IOException {
        // export only if there is imported image
        if(this.importImagePath == null) {
            showWarning("You need to import an image first!");
            return;
        }

        String exportPath = promptExportImagePath();
        if(exportPath != null) {
            File outputfile = new File(exportPath);
            ImageIO.write(getConvertedImage(), "png", outputfile);
        }
    }

    private String promptExportImagePath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save exported image to");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png")
        );
        File file = chooser.showSaveDialog(new Stage());
        // file is null when user cancels the selection
        return file == null ? null : file.toPath().toString();
    }

    // this is being called from the UI Export menu
    public void exportCSV() throws IOException {
        // export only if there is imported image
        if(this.importImagePath == null) {
            showWarning("You need to import an image first!");
            return;
        }

        String exportPath = promptExportCSVPath();
        if(exportPath != null) {

            RadioMenuItem selectedRadioButton = (RadioMenuItem) colorSpaceGroup.getSelectedToggle();
            String colorSpace = selectedRadioButton.getText();

            double chromaOffsetValue = 0.2;
            if(isNumeric(chromaOffset.getText())) {
                chromaOffsetValue = Double.parseDouble(chromaOffset.getText());
            }

            String colorPalettePath = getColorPalettePath();
            PixelArt pixelart;
            if(colorPalettePath.equalsIgnoreCase("Imported")) {
                pixelart = new PixelArt(displayedImage.getOriginalImage(), ColorSpace.valueOf(colorSpace), chromaOffsetValue, customPalette);
            }else{
                pixelart = new PixelArt(displayedImage.getOriginalImage(), ColorSpace.valueOf(colorSpace), chromaOffsetValue, getColorPalettePath());
            }

            DrednotColor[][] colors = pixelart.getDrednotColors();

            FileWriter myWriter = new FileWriter(exportPath);
            StringBuilder line = new StringBuilder();

            DrednotColor[][] transposed = new DrednotColor[colors[0].length][colors.length];
            for (int i = 0; i < colors.length; i++) {
                for (int j = 0; j < colors[i].length; j++) {
                    //System.out.println(i+" | "+j);
                    transposed[j][i] = colors[i][j];
                }
            }

            for (int x = 0; x < transposed.length; x++) {
                for (int y = 0; y < transposed[x].length; y++) {
                    line.append(transposed[x][y].getId()).append(",");
                }

                String finishedLine = line.toString().replaceAll(",$", "\n");
                myWriter.write(finishedLine);
                line.setLength(0);
            }
            myWriter.close();
            System.out.println("Successfully exported as CSV file!");
            setStatus("Status: Successfully exported as CSV file!");
        }
    }

    private String promptExportCSVPath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export converted colours to CSV");
        File file = chooser.showSaveDialog(new Stage());
        // file is null when user cancels the selection
        return file == null ? null : file.toPath().toString();
    }

    /* Utility methods */

    private void showWarning(String text) {
        Alert a = new Alert(Alert.AlertType.NONE);
        a.setHeaderText(text);
        a.getDialogPane().getButtonTypes().add(ButtonType.OK);
        a.show();
    }

    public void setStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }

    private boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException ignored) {}
        return false;
    }
}
