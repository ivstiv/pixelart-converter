package core;

import com.dajudge.colordiff.RgbColor;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.management.Attribute;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class Controller {

    private String importImage, exportImage = null;
    @FXML Label selectedImage;
    @FXML ToggleGroup colorSpaceGroup;

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

    public void showPreview() throws IOException {
        if(getImportImagePath() != null) {

/*
            int scale = 20;
            BufferedImage displayImage = new BufferedImage(colors.length*scale, colors[0].length*scale, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = displayImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(pixelart.getImage(), 0, 0, displayImage.getWidth(), displayImage.getHeight(), null);
            g2.dispose();
*/


            BufferedImage displayImage = getConvertedImage();
            Image image = SwingFXUtils.toFXImage(displayImage, null);
            ImageView imageView = new ImageView(image);
            Stage stage = new Stage();
            Group root = new Group(imageView);
            Scene scene = new Scene(root, displayImage.getWidth(), displayImage.getHeight());
            stage.setTitle("Image preview");
            stage.setScene(scene);
            stage.show();
/*
            BufferedImage newImage = new BufferedImage(colors.length*scale, colors[0].length*scale, BufferedImage.TYPE_INT_RGB);
            for (int x = 0, offsetX = 0; x < colors.length; x++) {
                for (int y = 0, offsetY = 0; y < colors[x].length; y++) {
                    newImage.setRGB(x+offsetX, y+offsetY, colors[x][y].getRGB());

                    //for(int s = 1; s < scale; s++, offsetY++) {
                        //System.out.println("wut"+s);
                    //    newImage.setRGB(x, y+s+offsetY, colors[x][y].getRGB());
                   // }
                }
            }
*/
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

        int scale = 25;
        BufferedImage displayImage = new BufferedImage(colors.length*scale, colors[0].length*scale, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = displayImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(drednotImage, 0, 0, displayImage.getWidth(), displayImage.getHeight(), null);
        if(scale > 24) {
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        }

        // i need a calculator here as well to put white text for black tiles
        ColorDistanceCalculator calculator = new ColorDistanceCalculator(ColorSpace.RGB);
        for (int x = 0; x < colors.length; x++) {
            for (int y = 0; y < colors[x].length; y++) {
                double distance = calculator.euclideanDistanceBetween(colors[x][y], new RgbColor(0,0,0));
                if(distance < 50) {
                    g2.setColor(Color.lightGray);
                }else{
                    g2.setColor(Color.black);
                }
                g2.drawString(colors[x][y].getId()+"", (float) (x*scale), (float) (y*scale+0.7*scale));
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
/*
    public String getExportImagePath() {
        if(exportImage == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setHeaderText("You need to select an image first!");
            a.show();
            return null;
        }else{
            return this.exportImage;
        }
    }*/
}
