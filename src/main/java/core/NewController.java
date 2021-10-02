package core;

import com.dajudge.colordiff.RgbColor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NewController implements Initializable {

    private String importImagePath = null;
    @FXML ImageView imagePreview;
    @FXML Button zoomInButton, zoomOutButton, convertButton, showOriginalButton;
    // you will not find these groups in SceneBuilder.. check the fxml file :D
    @FXML ToggleGroup colorSpaceGroup, colorPaletteGroup;
    @FXML RadioMenuItem importedOption;
    @FXML CheckBox showColorID;
    @FXML TextField scaleRatio, fontSize, chromaOffset;
    @FXML Label statusLabel;
    private DisplayedImage displayedImage;
    private String importedPalettePath;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        zoomInButton.setOnAction(event -> displayedImage.zoomIn());

        zoomOutButton.setOnAction(event -> displayedImage.zoomOut());

        showOriginalButton.setOnAction(event -> this.displayedImage.showOriginalImage());

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
                } catch (OutOfMemoryError e) {
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

    private String getPaletteInJson () {
        String path = getPalettePath();
        if (path.startsWith("palettes")) {
            return readFromBundledFiles(path);
        } else {
            return readFromFileSystem(path);
        }
    }


    private String getPalettePath () {
        RadioMenuItem selectedRadioButton = (RadioMenuItem) colorPaletteGroup.getSelectedToggle();
        String label = selectedRadioButton.getText();

        switch(label) {
            case "Dredark":
                return "palettes/DrednotNew.json";
            case "Drednot OLD":
                return "palettes/Drednot.json";
            case "Faber Castell 36":
                return "palettes/Faber_Castell_36.json";
            case "Black and White":
                return "palettes/BlackWhite.json";
            case "RGB":
                return "palettes/RGB.json";
            case "Imported":
                if (importedPalettePath == null) {
                    showWarning("You haven't imported a palette. The default one will be used.");
                    return "palettes/DrednotNew.json";
                }
                return importedPalettePath;
            default:
                return "palettes/DrednotNew.json";
        }
    }


    private String readFromBundledFiles (String path) {
        try {
            // I am reading in the file as stream and writing it to a file because of this:
            // https://stackoverflow.com/questions/43811764/java-getclass-getclassloader-getresourcepath-fails-inside-maven-shaded-ja
            InputStream is = getClass().getClassLoader().getResourceAsStream(path);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            File tempFile = File.createTempFile("aaa", "aaa", null);
            OutputStream fos = new FileOutputStream(tempFile);
            fos.write(buffer);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile));

            return bufferedReader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog(e);
            return null;
        }
    }


    private String readFromFileSystem (String path) {
        try {
            File tempFile = new File(path);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile));
            return bufferedReader.lines().collect(Collectors.joining());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showErrorDialog(e);
        }
        return null;
    }

    private BufferedImage getConvertedImage() {
        RadioMenuItem selectedRadioButton = (RadioMenuItem) colorSpaceGroup.getSelectedToggle();
        String colorSpace = selectedRadioButton.getText();

        double chromaOffsetValue = 0.2;
        if(isNumeric(chromaOffset.getText())) {
            chromaOffsetValue = Double.parseDouble(chromaOffset.getText());
        }

        PixelArt pixelart = null;
        try {
            ColorPalette palette = ColorPalette.fromJson(getPaletteInJson());
            pixelart = new PixelArt(displayedImage.getOriginalImage(), ColorSpace.valueOf(colorSpace), chromaOffsetValue, palette);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog(e);
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

        // I need a calculator here as well to put white text for black tiles
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


    public void importPalette() {
        String path = promptImportPalettePath();

        if(path != null) {
            importedPalettePath = path;
            importedOption.setDisable(false);
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

    public void resize() {
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

        String[] widthXheight = newSize.split("x");
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

    private BufferedImage getResizedImage(String imagePath, int scaledWidth, int scaledHeight) {
        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog(e);
        }

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
    public void importImage() {
        this.importImagePath = promptImportImagePath();
        if(this.importImagePath != null) {
            System.out.println("Imported image:"+importImagePath);
            setStatus("Status: Ready to convert.");
            // set up the preview
            //imagePreview.setImage(new Image("file:///"+importImagePath));
            displayedImage = new DisplayedImage(imagePreview);
            //displayedImage.setOriginalImage(new Image("file:///"+importImagePath));
            try {
                displayedImage.setOriginalImage(ImageIO.read(new File(importImagePath)));
            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog(e);
            }
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
    public void exportImage() {
        // export only if there is imported image
        if(this.importImagePath == null) {
            showWarning("You need to import an image first!");
            return;
        }

        String exportPath = promptExportImagePath();
        if(exportPath != null) {
            File outputFile = new File(exportPath);
            try {
                ImageIO.write(getConvertedImage(), "png", outputFile);
            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog(e);
            }
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
    public void exportCSV() {
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

            PixelArt pixelart = null;
            try {
                ColorPalette palette = ColorPalette.fromJson(getPaletteInJson());
                pixelart = new PixelArt(displayedImage.getOriginalImage(), ColorSpace.valueOf(colorSpace), chromaOffsetValue, palette);
            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog(e);
            }
            DrednotColor[][] colors = pixelart.getDrednotColors();

            FileWriter myWriter = null;
            try {
                myWriter = new FileWriter(exportPath);
            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog(e);
            }
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
                try {
                    myWriter.write(finishedLine);
                } catch (IOException e) {
                    e.printStackTrace();
                    showErrorDialog(e);
                }
                line.setLength(0);
            }
            try {
                myWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                showErrorDialog(e);
            }
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

    public void showAbout() {
        Alert a = new Alert(Alert.AlertType.NONE);
        a.setTitle("About");
        a.setHeaderText("If you have any issues with the program do not hesitate to contact me!\nDiscord tag: SKDown#4341");
        a.setContentText("Github: github.com/ivstiv/pixelart-converter");
        a.getDialogPane().getButtonTypes().add(ButtonType.OK);
        a.show();
    }

    /* Utility methods */

    private void showWarning(String text) {
        Alert a = new Alert(Alert.AlertType.NONE);
        a.setHeaderText(text);
        a.getDialogPane().getButtonTypes().add(ButtonType.OK);
        a.show();
    }

    public void showErrorDialog(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText("Oops, something really bad happened!");
        alert.setContentText("Please make my day a bit worse by sending me the stacktrace from below. :D\n" +
                "Contact details in the About page!");

        Label label = new Label("The exception stacktrace was:");

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
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
