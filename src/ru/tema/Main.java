package ru.tema;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Pass.token = "";

        //Parent root = FXMLLoader.load(getClass().getResource("Test.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("Auth.fxml"));
        primaryStage.setTitle("Vk Test");
        primaryStage.setScene(new Scene(root, 650, 430));
        primaryStage.setMaxWidth(650);
        primaryStage.setMinWidth(650);
        primaryStage.setMaxHeight(430);
        primaryStage.setMinHeight(430);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
