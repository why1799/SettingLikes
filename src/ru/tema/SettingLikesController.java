package ru.tema;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javax.swing.text.StyledEditorKit;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Vector;

public class SettingLikesController implements Initializable {

    @FXML
    public AnchorPane anchorpane;

    @FXML
    public TextField textfield;

    @FXML
    public MenuBar menuBar;

    @FXML
    public ProgressBar progressbar;

    @FXML
    public Label groupnamelabel, progresslabel, likeschangerlabel;

    private Menu menustart, menustop, menulogout, menuupdate, menuabout;

    private String lastpost, groupid;

    private Boolean stopprog, update;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Label menustartlabel = new Label("Начать");
        menustart = new Menu();
        menustart.setGraphic(menustartlabel);

        Label menustoplabel = new Label("Остановить");
        menustop = new Menu();
        menustop.setGraphic(menustoplabel);
        menustop.setDisable(true);

        Label menuupdatelabel = new Label("Обновить");
        menuupdate = new Menu();
        menuupdate.setGraphic(menuupdatelabel);
        menuupdate.setDisable(true);

        Label menulogoutlabel = new Label("Разлогиниться");
        menulogout = new Menu();
        menulogout.setGraphic(menulogoutlabel);

        Label menuaboutlabel = new Label("О программе");
        menuabout = new Menu();
        menuabout.setGraphic(menuaboutlabel);


        menustartlabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                start();
                System.out.println("Start button was pressed");
            }
        });

        menuupdatelabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                update();
                System.out.println("Update button was pressed");
            }
        });

        menustoplabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stop();
                System.out.println("Stop button was pressed");
            }
        });

        menulogoutlabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                logout();
                System.out.println("Logout button was pressed");
            }
        });

        menuaboutlabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                allertShow("О программе", "Программа Setting Likes ставит лайки на новые записи в указанной группе.\n\n© 2018 SettingLikes\nАвтор: Тёма Харченко\nhttps://vk.com/kharchenko_tema");
                System.out.println("About button was pressed");
            }
        });

        menuBar.getMenus().addAll(menustart);
        menuBar.getMenus().addAll(menustop);
        menuBar.getMenus().addAll(menuupdate);
        menuBar.getMenus().addAll(menulogout);
        menuBar.getMenus().addAll(menuabout);

        //Reloading textField text
        reload();

        stopprog = false;
        update = false;
        lastpost = "";

        groupnamelabel.setText("");
        progresslabel.setText("");
        likeschangerlabel.setText("");
    }

    private void start(){
        stopprog = false;
        menustop.setDisable(false);
        menuupdate.setDisable(false);
        textfield.setDisable(true);
        menustart.setDisable(true);

        System.out.println("Start button was pressed");
        save();

        groupid = getGroupID();

        if(groupid == "-1"){
            allertShow("Ошибка", "Вы не ввели ссылку на группу");
            stop();
            return;
        }

        groupid = getGroupID(groupid);

        System.out.println(groupid);

        String code = "https://api.vk.com/method/wall.get.xml?owner_id=-" + groupid + "&count=2&filter=owner&access_token=" + Pass.token + Pass.version;
        String data = Get.get(code);

        updatelastpost(data);

        if(stopprog){
            return;
        }

        System.out.println(lastpost);

        waitForTheNextCheck();
    }

    private void stop(){
        menustop.setDisable(true);
        menuupdate.setDisable(true);
        textfield.setDisable(false);
        menustart.setDisable(false);
        stopprog = true;
    }

    private void update(){
        update = true;
    }

    private void logout(){
        stop();
        try {
            FileOutputStream fos = new FileOutputStream("account.cfg");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            Save ts = new Save();

            ts.token = "";
            ts.user_id = "";

            oos.writeObject(ts);
            oos.flush();
            oos.close();
            System.out.println("File was sussesfully saved");
        }
        catch (Exception ex){
            System.out.println("Couldn't save the file");
        }

        Stage currentstage = (Stage) anchorpane.getScene().getWindow();
        Parent root;
        try {
            Stage stage = new Stage();
            root = FXMLLoader.load(getClass().getResource("Auth.fxml"));
            stage.setTitle("Vk Test");
            stage.setScene(new Scene(root, 650, 430));
            stage.setMaxWidth(650);
            stage.setMinWidth(650);
            stage.setMaxHeight(430);
            stage.setMinHeight(430);
            stage.show();
            System.out.println("Auth window started");
            currentstage.hide();
        }
        catch (Exception e) {
            System.out.println("Auth window didn't start");
            e.printStackTrace();
        }
        currentstage.close();
    }

    private void reload(){
        //Reloading textField text
        try {
            FileInputStream fis = new FileInputStream("save");
            ObjectInputStream oin = new ObjectInputStream(fis);
            TextSave ts = (TextSave) oin.readObject();
            fis.close();

            textfield.setText(ts.text);
        }
        catch(Exception ex) {}
    }

    private void save() {
        //Saving textField text
        try {
            FileOutputStream fos = new FileOutputStream("save");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            TextSave ts = new TextSave();

            ts.text = textfield.getText();

            oos.writeObject(ts);
            oos.flush();
            oos.close();
            System.out.println("File was sussesfully saved");
        }
        catch (Exception ex){
            System.out.println("Couldn't save the text");
        }
    }

    private String getFromKey(String find, String data){
        return getFromKey(find, data, 0);
    }

    private String getFromKey(String find, String data, int start){
        String find1 = "<" + find + ">", find2 = "</" + find + ">";
        int st = data.indexOf(find1, start);
        if(st == -1)
            return "";

        return data.substring(st + find1.length(), data.indexOf(find2, st));
    }

    private void allertShow(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private String getGroupID(String id){
        String code = "https://api.vk.com/method/groups.getById.xml?group_id=" + id + "&access_token=" + Pass.token + Pass.version;
        String data = Get.get(code);

        groupnamelabel.setText("Название группы: " + getFromKey("name", data));

        return getFromKey("id", data);
    }

    private String getGroupID() {
        String groupurl = textfield.getText();
        String find = "vk.com/";
        int st = groupurl.indexOf(find);
        if(st == -1)
            return "-1";

        groupurl = groupurl.substring(st + find.length());

        find = "public";
        st = groupurl.indexOf(find);
        if(st == 0){
            return groupurl.substring(st + find.length());
        }

        find = "club";
        st = groupurl.indexOf(find);
        if(st == 0){
            return groupurl.substring(st + find.length());
        }

        return groupurl;
    }

    private void updatelastpost(String data) {
        if(data.indexOf("<error_code>") != -1){
            allertShow("Ошибка", getFromKey("error_msg", data));
            stop();
            return;
        }

        if(getFromKey("count", data).compareTo("0") == 0){
            lastpost = "0";
        }
        else
        {
            int st = data.indexOf("id");
            lastpost = getFromKey("id", data);

            if(getFromKey("is_pinned", data).compareTo("1") == 0 && getFromKey("count", data).compareTo("1") != 0){
                String newpost = getFromKey("id", data, st);
                if(Integer.parseInt(newpost) > Integer.parseInt(lastpost)){
                    lastpost = newpost;
                }
            }
        }
    }

    private String newLikes(int count)
    {
        if(count  == 1){
            return " новый лайк";
        }

        if(count % 10 >= 2 && count % 10 <= 4 && (count < 12 || count > 14)){
            return " новых лайка";
        }

        return " новых лайков";
    }

    private String wasSet(int count)
    {
        if(count  == 1){
            return "Был поставлен ";
        }
        return "Было поставлено ";
    }

    private void setNewLikes() {
        Task task = new Task<Void>() {
            @Override public Void call() {
                menuupdate.setDisable(true);
                updateProgress(0, 100);

                String code = "https://api.vk.com/method/wall.get.xml?owner_id=-" + groupid + "&count=1000&filter=owner&access_token=" + Pass.token + Pass.version;
                String data = Get.get(code);

                String oldlastpost = lastpost;

                updatelastpost(data);

                Vector<String> idstolike = new Vector<String>();

                if(getFromKey("count", data).compareTo("0") != 0){
                    int st = 0;

                    while(st != -3)
                    {
                        String newid = getFromKey("id", data, st);

                        if(st == 0){
                            st = data.indexOf("<id>", st) - 2;
                        }

                        if (Integer.parseInt(newid) <= Integer.parseInt(oldlastpost) && st == 0){
                            st = data.indexOf("<id>", st + 3) - 2;
                            continue;
                        }

                        if(Integer.parseInt(newid) <= Integer.parseInt(oldlastpost)){
                            break;
                        }

                        idstolike.add(newid);
                        st = data.indexOf("<id>", st + 3) - 2;

                    }
                }

                if(idstolike.size() != 0){
                    updateMessage("Ставятся лайки");
                }

                for(int i = 0; i < idstolike.size() && !stopprog; ++i) {
                    updateTitle((i + 1) + " из " + idstolike.size());
                    updateProgress(i + 1, idstolike.size());

                    code = "https://api.vk.com/method/likes.add.xml?type=post&owner_id=-" + groupid + "&item_id=" + idstolike.elementAt(i) + "&filter=owner&access_token=" + Pass.token + Pass.version;
                    data = Get.get(code);

                    if (data.indexOf("<error_code>") != -1) {
                        try {
                            i--;
                            Thread.sleep(500);
                        } catch (Exception ex) {
                        }
                    }
                }

                updateMessage(wasSet(idstolike.size()) + idstolike.size() + newLikes(idstolike.size()) + " после обновления!");
                menuupdate.setDisable(false);
                return null;
            }
        };

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                if(!stopprog){
                    waitForTheNextCheck();
                }
                else{
                    menuupdate.setDisable(true);
                }
            }
        });

        likeschangerlabel.textProperty().bind(task.messageProperty());
        progresslabel.textProperty().bind(task.titleProperty());
        progressbar.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void waitForTheNextCheck() {

        //long sec = 10;

        long min = 30;
        long sec = min * 60;

        Task task = new Task<Void>() {
            @Override public Void call() throws InterruptedException {
                for(long i = 0 ; i <= sec && !stopprog; i++){
                    if(i != 0){
                        Thread.sleep(1000);
                    }
                    if(update){
                        i = sec;
                        update = false;
                    }
                    long diff = sec - i;
                    long diffSeconds = diff % 60;
                    long diffMinutes = (diff / 60) % 60;
                    updateProgress((double) i, (double)sec);
                    updateMessage("До следующей проверки: " + diffMinutes + ":" + (diffSeconds / 10) % 10 + "" + diffSeconds % 10);
                }
                return null;
            }
        };

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                if(!stopprog){
                    setNewLikes();
                }
            }
        });

        progresslabel.textProperty().bind(task.messageProperty());
        progressbar.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
