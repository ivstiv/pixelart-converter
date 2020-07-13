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

        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("new-view.fxml"));
        fxmlLoader.setController(new NewController());
        Parent root = fxmlLoader.load();
        stage.setTitle("Pixel Art Converter");
        stage.setScene(new Scene(root, 1280, 720));
        stage.show();
    }


    public static void main(String[] args) {
        if(args.length > 0) {
            System.out.println("to be implemented");
            System.exit(0);
        }else{
            launch(args);
        }
    }

    public static Main getInstance() {
        return instance;
    }
}
