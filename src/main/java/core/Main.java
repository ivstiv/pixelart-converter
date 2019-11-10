package core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    static Main instance;

    @Override
    public void start(Stage primaryStage) throws Exception{
        instance = this;

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("view.fxml"));
        fxmlLoader.setController(new Controller());
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Drednot Pixel Art Converter");
        primaryStage.setScene(new Scene(root, 650, 400));
        primaryStage.show();
    }


    public static void main(String[] args) {
        if(args.length > 0) {
            System.out.println("export option");
            System.exit(0);
        }else{
            launch(args);
        }
    }

    public static Main getInstance() {
        return instance;
    }
}
