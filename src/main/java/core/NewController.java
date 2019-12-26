package core;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class NewController implements Initializable {

    private String importImagePath;
    private String[] permittedExtensions = {"png", "jpg", "jpeg"};
    @FXML ImageView imagePreview;
    @FXML Button zoomInButton, zoomOutButton;
    private DisplayedImage displayedImage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        zoomInButton.setOnAction(event -> {
            displayedImage.zoomIn();
        });

        zoomOutButton.setOnAction(event -> {
            displayedImage.zoomOut();
        });
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
                this.importImagePath = "file:///"+file;
                System.out.println(file + " chosen.");
                imagePreview.setImage(new Image(importImagePath));
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
