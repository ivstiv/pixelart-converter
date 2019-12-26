package core;

import com.dajudge.colordiff.RgbColor;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.management.Attribute;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    private String importImage, exportImage = null;
    @FXML Label selectedImage;
    @FXML ToggleGroup colorSpaceGroup;
    @FXML CheckBox showColorID;
    @FXML Slider fontSize, scaleRatio;
    @FXML Hyperlink github;
    @FXML TextField chromaOffset;
    private static Controller instance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;
        github.setOnAction((event) -> Main.getInstance().getHostServices().showDocument("https://github.com/Ivstiv/drednot-pixelart-converter"));
    }

    public static Controller getInstance() {
        return instance;
    }

    public void selectImportImage() {
        FileDialog chooser = new FileDialog((Frame)null, "Select to import an image");
        chooser.setMode(FileDialog.LOAD);
        chooser.setVisible(true);
        String file = chooser.getDirectory()+chooser.getFile();
        String extension = file.substring(file.length()-3, file.length());
        if(!file.equals("nullnull")) {
            if(!extension.equalsIgnoreCase("png")) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Selected file:\n"+file);
                a.setHeaderText("You can only choose PNG images!");
                a.show();
            }else{
                this.importImage = file;
                selectedImage.setText("Selected Image: "+file);
            }
            System.out.println(file + " chosen.");
        }
    }

    public void showOriginal() throws IOException {
        if(getImportImagePath() != null) {
            // here colorspace is not used at all so no need to link it to the GUI
            PixelArt pixelart = new PixelArt(getImportImagePath(), ColorSpace.CIEDE2000);
            Color[][] colors = pixelart.getOriginalColors();
            BufferedImage drednotImage = new BufferedImage(colors.length, colors[0].length, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < colors.length; x++) {
                for (int y = 0; y < colors[x].length; y++) {
                    drednotImage.setRGB(x, y, colors[x][y].getRGB());
                }
            }

            int scale = (int) scaleRatio.getValue();
            BufferedImage displayImage = new BufferedImage(colors.length*scale, colors[0].length*scale, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = displayImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(drednotImage, 0, 0, displayImage.getWidth(), displayImage.getHeight(), null);

            WritableImage wr = null;
            if (displayImage != null) {
                wr = new WritableImage(displayImage.getWidth(), displayImage.getHeight());
                PixelWriter pw = wr.getPixelWriter();
                for (int x = 0; x < displayImage.getWidth(); x++) {
                    for (int y = 0; y < displayImage.getHeight(); y++) {
                        pw.setArgb(x, y, displayImage.getRGB(x, y));
                    }
                }
            }
            ImageView imageView = new ImageView(wr);
            Stage stage = new Stage();
            Group root = new Group(imageView);
            Scene scene = new Scene(root, displayImage.getWidth()-12, displayImage.getHeight()-12);
            stage.setTitle("Image preview (Original)");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        }
    }

    public void showPreview() throws IOException {
        if(getImportImagePath() != null) {

            RadioButton selectedRadioButton = (RadioButton) colorSpaceGroup.getSelectedToggle();
            String colorSpace = selectedRadioButton.getText();

            BufferedImage displayImage = getConvertedImage();
            // I am writing it myself instead of using SwingFXUtils.toFXImage() because
            // the library has been moved between java 8 and java 11 so the path is different
            WritableImage wr = null;
            if (displayImage != null) {
                wr = new WritableImage(displayImage.getWidth(), displayImage.getHeight());
                PixelWriter pw = wr.getPixelWriter();
                for (int x = 0; x < displayImage.getWidth(); x++) {
                    for (int y = 0; y < displayImage.getHeight(); y++) {
                        pw.setArgb(x, y, displayImage.getRGB(x, y));
                    }
                }
            }
            //Image image = new ImageView(wr);
            ImageView imageView = new ImageView(wr);
            Stage stage = new Stage();
            Group root = new Group(imageView);
            Scene scene = new Scene(root, displayImage.getWidth()-12, displayImage.getHeight()-12);
            stage.setTitle("Image preview ("+colorSpace+")");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        }
    }

    public void exportImage() throws IOException {
        if(getImportImagePath() != null) {
            String exportPath = selectExportImage();
            if(exportPath != null) {
                File outputfile = new File(exportPath);
                ImageIO.write(getConvertedImage(), "png", outputfile);
            }
        }
    }

    private BufferedImage getConvertedImage() throws IOException {
        RadioButton selectedRadioButton = (RadioButton) colorSpaceGroup.getSelectedToggle();
        String colorSpace = selectedRadioButton.getText();

        PixelArt pixelart = new PixelArt(getImportImagePath(), ColorSpace.valueOf(colorSpace));
        DrednotColor[][] colors = pixelart.getDrednotColors();
        BufferedImage drednotImage = new BufferedImage(colors.length, colors[0].length, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[x].length; y++) {
                drednotImage.setRGB(x, y, colors[x][y].getRGBValue());
            }
        }

        int scale = (int) scaleRatio.getValue();
        BufferedImage displayImage = new BufferedImage(colors.length*scale, colors[0].length*scale, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = displayImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(drednotImage, 0, 0, displayImage.getWidth(), displayImage.getHeight(), null);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int) fontSize.getValue()));

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

    public String selectExportImage() {
        FileDialog chooser = new FileDialog((Frame)null, "Select to export an image");
        chooser.setMode(FileDialog.LOAD);
        chooser.setVisible(true);
        String file = chooser.getDirectory()+chooser.getFile();
        if(file.equals("nullnull")) {
            return null;
        }
        System.out.println(file + " chosen.");
        return file;
    }

    public String getImportImagePath() {
        if(importImage == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setHeaderText("You need to select an image first!");
            a.show();
            return null;
        }else{
            return this.importImage;
        }
    }

    public void resizeImage() throws IOException {
        RadioButton selectedRadioButton = (RadioButton) colorSpaceGroup.getSelectedToggle();
        String colorSpace = selectedRadioButton.getText();

        PixelArt pixelart = new PixelArt(getImportImagePath(), ColorSpace.valueOf(colorSpace));
        pixelart.resize("test.png", 43, 43
        );
    }

    public void openHelp() throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("new-view.fxml"));
        fxmlLoader.setController(new NewController());
        Parent root = fxmlLoader.load();
        stage.setTitle("Drednot Pixel Art Converter");
        stage.setScene(new Scene(root, 1280, 720));
        stage.show();
    }
}
