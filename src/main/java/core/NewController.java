package core;

import com.dajudge.colordiff.RgbColor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NewController implements Initializable {

    private String importImagePath;
    private String[] permittedExtensions = {"png", "jpg", "jpeg"};
    @FXML ImageView imagePreview;
    @FXML Button zoomInButton, zoomOutButton, convertButton;
    @FXML ToggleGroup colorSpaceGroup;
    @FXML CheckBox showColorID;
    @FXML TextField scaleRatio, fontSize, chromaOffset;
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
            if(getImportImagePath() != null) {
                try {
                    displayedImage.setConvertedImage(getConvertedImage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private BufferedImage getConvertedImage() throws IOException {
        RadioMenuItem selectedRadioButton = (RadioMenuItem) colorSpaceGroup.getSelectedToggle();
        String colorSpace = selectedRadioButton.getText();

        double chromaOffsetValue = 0.2;
        if(isNumeric(chromaOffset.getText())) {
            chromaOffsetValue = Double.parseDouble(chromaOffset.getText());
        }

        PixelArt pixelart = new PixelArt(getImportImagePath(), ColorSpace.valueOf(colorSpace), chromaOffsetValue);
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

    public String getImportImagePath() {
        if(importImagePath == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setHeaderText("You need to select an image first.");
            a.show();
            return null;
        }else{
            return this.importImagePath;
        }
    }

    public void selectImportImage() {
        FileDialog chooser = new FileDialog((Frame)null, "Import an image");
        chooser.setMode(FileDialog.LOAD);
        chooser.setVisible(true);
        String file = chooser.getDirectory()+chooser.getFile();
        String extension = file.substring(file.length()-3, file.length());
        if(!file.equals("nullnull")) {
            if(isPermittedExtension(extension)) {
                this.importImagePath = file;
                System.out.println(file + " chosen.");
                imagePreview.setImage(new Image("file:///"+importImagePath));
                displayedImage = new DisplayedImage(imagePreview);
                displayedImage.setup();
            }else{
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Selected file:\n"+file);
                a.setHeaderText("You can only choose PNG and JPEG images.");
                a.show();
            }
        }
    }

    boolean isPermittedExtension(String extension) {
        for(String ext : permittedExtensions)
            if(ext.equalsIgnoreCase(extension))
                return true;
            return false;
    }
}
