package pl.mwrobel.todolist;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import pl.mwrobel.todolist.datamodel.Task;
import pl.mwrobel.todolist.datamodel.TaskListData;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    @FXML
    private ListView<Task> toDoListView;
    @FXML
    private TextArea taskDescriptionTextArea;
    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<Task> filteredList;

    private Predicate<Task> allTasks;
    private Predicate<Task> todayTasks;

    public void initialize(){

        listContextMenu = new ContextMenu();

        //adding edit menu item
        MenuItem editMenuItem = new MenuItem("Edit task");
        editMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showEditTaskDialog();
            }
        });

        //adding delete menu item
        MenuItem deleteMenuItem = new MenuItem("Delete task");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Task task = toDoListView.getSelectionModel().getSelectedItem();
                deleteTask(task);
            }
        });

        //adding items to listContextMenu
        listContextMenu.getItems().addAll(editMenuItem, deleteMenuItem);

        //ANONYMOUS CLASS APPROACH
        toDoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Task>() {
            @Override
            public void changed(ObservableValue<? extends Task> observable, Task oldValue, Task newValue) {
                if(newValue != null){
                    Task task = toDoListView.getSelectionModel().getSelectedItem();
                    taskDescriptionTextArea.setText(task.getDescription());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("d MMMM yyyy");
                    deadlineLabel.setText(df.format(task.getDeadline()));
                }
            }
        });
        //LAMBDA APPROACH
//        toDoListView.getSelectionModel().selectedItemProperty().addListener( (observable, newValue, oldValue) -> {
//                 if(newValue != null){
//                    Task task = toDoListView.getSelectionModel().getSelectedItem();
//                    taskDescriptionTextArea.setText(task.getDescription());
//                    deadlineLabel.setText(task.getDeadline().toString());
//                }
//            });

        //FILTERING
        allTasks = new Predicate<Task>() {
            @Override
            public boolean test(Task task) {
                return true;
            }
        };
        todayTasks = new Predicate<Task>() {
            @Override
            public boolean test(Task task) {
                return task.getDeadline().equals(LocalDate.now());
            }
        };
        filteredList = new FilteredList<Task>(TaskListData.getInstance().getTasks(), allTasks);

        //SORTING
        SortedList<Task> sortedList = new SortedList<>(filteredList,
                new Comparator<Task>() {
                    @Override
                    public int compare(Task o1, Task o2) {
                            return o1.getDeadline().compareTo(o2.getDeadline());
                    }
                });

        //toDoListView.setItems(TaskListData.getInstance().getTasks());
        toDoListView.setItems(sortedList);
        toDoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        toDoListView.getSelectionModel().selectFirst();

        toDoListView.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>() {
            @Override
            public ListCell<Task> call(ListView<Task> param) {
                ListCell<Task> cell = new ListCell<Task>(){
                    @Override
                    protected void updateItem(Task task, boolean empty) {
                        super.updateItem(task, empty);
                        if(empty){
                            setText(null);
                        } else{
                            setText(task.getName() + " ");
                            // set color text to RED if deadline is today
                            if(task.getDeadline().equals(LocalDate.now())){
                                setTextFill(Color.RED);
                            }
                            // set color text to ORANGE if deadline in <=3 days
                            else if(ChronoUnit.DAYS.between(LocalDate.now(),task.getDeadline()) <= 3 &&
                                    ChronoUnit.DAYS.between(LocalDate.now(),task.getDeadline()) >= 1){
                                setTextFill(Color.ORANGE);
                            }
                            // set color text to GRAY if deadline passed
                            else if(ChronoUnit.DAYS.between(LocalDate.now(),task.getDeadline()) < 0){
                                    setTextFill(Color.GRAY);
                            }
                            setText(task.getName().trim());
                        }
                    }
                };
                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            } else{
                                cell.setContextMenu(listContextMenu);
                            }
                        }
                );

                return cell;
            }


        });
        TaskListData.getInstance().getTasks().addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> c) {
                while(c.next()){
                    if(c.wasUpdated()){
                        System.out.println("List was Updated!");
                    }
                }
            }
        });

    }

    @FXML
    public void showNewTaskDialog(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new task");
        dialog.setHeaderText("Use this dialog to create a new task");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("newTaskDialog.fxml"));

        try{
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e){
            System.out.println("Could not load the dialog.");
            e.printStackTrace();
            return;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            DialogController controller = fxmlLoader.getController();
            Task newTask = controller.processData();
            toDoListView.getSelectionModel().select(newTask);
        }
    }
    @FXML
    public void showEditTaskDialog(){
        Task selectedTask = toDoListView.getSelectionModel().getSelectedItem();
        if(selectedTask != null) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(mainBorderPane.getScene().getWindow());
            dialog.setTitle("Edit task");
            dialog.setHeaderText("Use this dialog to edit selected task");
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("editTaskDialog.fxml"));
            DialogController controller;
            try {
                Parent dialogContent = fxmlLoader.load();
                controller = fxmlLoader.getController(); // need to create the controller after the fxmlLoader.load() as the controller is specified in the fxml file
                controller.loadData(selectedTask);
                dialog.getDialogPane().setContent(dialogContent);

            } catch (IOException e) {
                System.out.println("Could not load the dialog.");
                e.printStackTrace();
                return;
            }

            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {

                    controller.saveEditedTask(selectedTask);
                    toDoListView.getSelectionModel().select(selectedTask);
                    toDoListView.refresh(); // change in name is caught but color is not changed
                    deadlineLabel.setText(selectedTask.getDeadline().format(TaskListData.getInstance().getFormatter()));
                    taskDescriptionTextArea.setText(selectedTask.getDescription());
                    toDoListView.getCellFactory().call(toDoListView);
            }

        }
    }

    @FXML
    public void handleClickListView(){
        Task task = toDoListView.getSelectionModel().getSelectedItem();

        taskDescriptionTextArea.setText(task.getDescription());
        deadlineLabel.setText(task.getDeadline().toString());

    }
    @FXML
    public void deleteTask(){
        Task selectedTask = toDoListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null){
            deleteTask(selectedTask);
        }
    }

    public void deleteTask(Task task){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete task");
        alert.setHeaderText("Delete task: " + task.getName());
        alert.setContentText("Please, press OK to confirm or Cancel to back out.");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)){
            TaskListData.getInstance().deleteTask(task);
        }

    }


    @FXML
    public void handleKeyPressed(KeyEvent keyEvent){
        Task selectedTask = toDoListView.getSelectionModel().getSelectedItem();
        if (selectedTask != null){
            if(keyEvent.getCode().equals(KeyCode.DELETE)){
                deleteTask(selectedTask);
            }
        }
    }

    @FXML
    public void handleFilterButton(){
        Task selectedTask = toDoListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()){
            filteredList.setPredicate(todayTasks);
            if (filteredList.isEmpty()){
                taskDescriptionTextArea.clear();
                deadlineLabel.setText("");
            } else if(filteredList.contains(selectedTask)){
                toDoListView.getSelectionModel().select(selectedTask);
            }else{
                toDoListView.getSelectionModel().selectFirst();
            }
        } else {
            filteredList.setPredicate(allTasks);
            toDoListView.getSelectionModel().select(selectedTask);
        }
    }
    @FXML
    public void handleExit(){
        Platform.exit();
    }

}
