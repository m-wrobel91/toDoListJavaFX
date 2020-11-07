package pl.mwrobel.todolist;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pl.mwrobel.todolist.datamodel.Task;
import pl.mwrobel.todolist.datamodel.TaskListData;


public class DialogController {

    @FXML
    private TextField nameField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private DatePicker deadlinePicker;

    public Task processData(){
        Task newTask = new Task(nameField.getText().trim(),
                descriptionArea.getText().trim(),
                deadlinePicker.getValue());
        TaskListData.getInstance().addTask(newTask);
        return newTask;
    }

    public Task loadData(Task selectedTask){


        nameField.setText(selectedTask.getName());
        descriptionArea.setText(selectedTask.getDescription());
        deadlinePicker.setValue(selectedTask.getDeadline());

        return selectedTask;
    }

    public void saveEditedTask(Task selectedTask){

        selectedTask.setDeadline(deadlinePicker.getValue());
        selectedTask.setDescription(descriptionArea.getText().trim());
        selectedTask.setName(nameField.getText().trim());
}
}
