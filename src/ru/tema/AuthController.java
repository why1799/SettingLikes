package ru.tema;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.event.ActionEvent;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthController implements Initializable{

    @FXML
    public AnchorPane anchorPane;

    @FXML
    public WebView webView;

    WebEngine engine;
    Boolean authloaded;

    private void webViewLoaded() {
        System.out.println("Page loaded");
        if(authloaded)
        {
            authloaded = false;
            if(doesLoggedIn())
                close();
            else
                engine.load("https://oauth.vk.com/authorize?client_id=5155001&redirect_uri=https://oauth.vk.com/blank.html&display=popup&scope=wall&response_type=token&revoke=1");
        }
        else {
            String t = engine.getDocument().getDocumentURI();
            int ti = t.indexOf("https://oauth.vk.com/blank.html#access_token=");

            int ti2 = t.indexOf("https://oauth.vk.com/blank.html#error=access_denied");
            if(ti2 == 0)
                close();

            if (ti == 0)
            {
                ti += "https://oauth.vk.com/blank.html#access_token=".length();
                Pass.token = "";
                for (; t.charAt(ti) != '&'; ti++)
                {
                    Pass.token += t.charAt(ti);
                }
                ti = t.indexOf("user_id=") + "user_id=".length();
                Pass.user_id = "";
                for (; ti < t.length(); ti++)
                {
                    Pass.user_id += t.charAt(ti);
                }

                try {
                    FileOutputStream fos = new FileOutputStream("account.cfg");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    Save ts = new Save();

                    ts.token = Pass.token;
                    ts.user_id = Pass.user_id;

                    oos.writeObject(ts);
                    oos.flush();
                    oos.close();
                    System.out.println("File was sussesfully saved");
                }
                catch (Exception ex){
                    System.out.println("Couldn't save the file");
                }

                close();
            }
        }
    }

    private void close() {
        Stage currentstage = (Stage) anchorPane.getScene().getWindow();
        if (Pass.token != "")
        {
            Parent root;
            try {
                root = FXMLLoader.load(getClass().getResource("SettingLikes.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Setting Likes");
                stage.setScene(new Scene(root, 490, 205));
                stage.setMaxWidth(490);
                stage.setMinWidth(490);
                stage.setMaxHeight(250);
                stage.setMinHeight(250);
                stage.show();
                System.out.println("Setting Likes window started");
                currentstage.hide();
            }
            catch (Exception e) {
                System.out.println("Setting Likes window didn't start");
                e.printStackTrace();
            }
        }
        currentstage.close();
    }

    private Boolean doesLoggedIn() {
        try {
            FileInputStream fis = new FileInputStream("account.cfg");
            ObjectInputStream oin = new ObjectInputStream(fis);
            Save ts = (Save) oin.readObject();
            fis.close();
            Pass.token = ts.token;
            Pass.user_id = ts.user_id;
        }
        catch(Exception ex) {
            return false;
        }

        String got = Get.get("https://api.vk.com/method/groups.get?user_id=" + Pass.user_id + "&extended=0&access_token=" + Pass.token + Pass.version).toString();
        int ind = got.indexOf("error_code");
        if (ind == -1)
            return true;
        else
            return false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        engine = webView.getEngine();

        engine.getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable,
                 Worker.State oldValue,
                 Worker.State newValue) -> {
                    if( newValue != Worker.State.SUCCEEDED ) {
                        return;
                    }
                    webViewLoaded();
                } );

        authloaded = true;
        engine.loadContent("<html><body><h1>VK auth for setting likes</h1><body></html>");
    }


}
