package pl.mwrobel.todolist;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pl.mwrobel.todolist.datamodel.TaskListData;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainwindow.fxml"));
        primaryStage.setTitle("toDoList");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        try{
            TaskListData.getInstance().loadTasks();
        }catch(IOException e){
            System.out.println( e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        try{
            TaskListData.getInstance().saveTasks();
        }catch(IOException e){
            System.out.println( e.getMessage());
        }
    }
}
