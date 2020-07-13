package core;

import com.dajudge.colordiff.RgbColor;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NewController implements Initializable {

    private String importImagePath = null;
    @FXML ImageView imagePreview;
    @FXML Button zoomInButton, zoomOutButton, convertButton, showOriginalButton;
    @FXML ToggleGroup colorSpaceGroup;
    @FXML CheckBox showColorID;
    @FXML TextField scaleRatio, fontSize, chromaOffset;
    @FXML Label statusLabel;
    private DisplayedImage displayedImage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        zoomInButton.setOnAction(event -> {
            displayedImage.zoomIn();
        });

        zoomOutButton.setOnAction(event -> {
            displayedImage.zoomOut();
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
                    if(convertedImage != null)
                        displayedImage.setConvertedImage(getConvertedImage());
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

        PixelArt pixelart = new PixelArt(this.importImagePath, ColorSpace.valueOf(colorSpace), chromaOffsetValue);
        DrednotColor[][] colors = pixelart.getDrednotColors();
        BufferedImage drednotImage = new BufferedImage(colors.length, colors[0].length, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[x].length; y++) {
                drednotImage.setRGB(x, y, colors[x][y].getRGBValue());
            }
        }

/*
        // Pepa test export numbers
        try {
            FileWriter myWriter = new FileWriter("data.csv");
            StringBuilder line = new StringBuilder();

            DrednotColor[][] transposed = new DrednotColor[colors[0].length][colors.length];
            for (int i = 0; i < colors.length; i++) {
                for (int j = 0; j < colors[i].length; j++) {
                    System.out.println(i+" | "+j);
                    transposed[j][i] = colors[i][j];
                }
            }

            for (int x = 0; x < transposed.length; x++) {
                for (int y = 0; y < transposed[x].length; y++) {
                    line.append(transposed[x][y].getId()).append(",");
                }
                //line.replace(line.toString().lastIndexOf(","), line.toString().lastIndexOf(","),"\n");
                String finishedLine = line.toString().replaceAll(",$", "\n");
                myWriter.write(finishedLine);
                line.setLength(0);
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
*/


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

    public static boolean isNumeric(String s) {
        try {
            double v = Double.parseDouble(s);
            return true;
        } catch (NumberFormatException nfe) {}
        return false;
    }

    // this is being called from the UI Import menu
    public void importImage() {
        this.importImagePath = promptImportImagePath();
        if(this.importImagePath != null) {
            System.out.println("Imported image:"+importImagePath);
            setStatus("Status: Ready to convert.");
            // setup the preview
            imagePreview.setImage(new Image("file:///"+importImagePath));
            displayedImage = new DisplayedImage(imagePreview);
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

    private void showWarning(String text) {
        Alert a = new Alert(Alert.AlertType.NONE);
        a.setHeaderText(text);
        a.getDialogPane().getButtonTypes().add(ButtonType.OK);
        a.show();
    }

    public void setStatus(String text) {
        Platform.runLater(() -> statusLabel.setText(text));
    }
}
