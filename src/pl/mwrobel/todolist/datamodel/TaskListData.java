package pl.mwrobel.todolist.datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

public class TaskListData {

    private static TaskListData instance = new TaskListData();
    private static String filename = "TaskListData.txt";

    private ObservableList<Task> tasks;
    private DateTimeFormatter formatter;

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public static TaskListData getInstance(){
        return instance;
    }

    private TaskListData(){
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public ObservableList<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public Task getTask(Task selectedTask){
        return tasks.get(tasks.indexOf(selectedTask));
    }

    public void loadTasks() throws IOException {
        tasks = FXCollections.observableArrayList();
        Path path = Paths.get(filename);
        BufferedReader bf = Files.newBufferedReader(path);

        String input;
        try{
            while((input=bf.readLine()) != null){
                String[] taskElements = input.split("\t");
                String name = taskElements[0];
                String description = taskElements[1];
                LocalDate deadline = LocalDate.parse(taskElements[2], formatter);

                Task task = new Task(name, description, deadline);
                tasks.add(task);
            }

        }finally {
            if(bf != null){
                bf.close();
            }
        }

    }
    public void saveTasks() throws IOException{
        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);
        try{
            Iterator<Task> iterator = tasks.iterator();
            while(iterator.hasNext()){
                Task task = iterator.next();
                bw.write(String.format("%s\t%s\t%s",
                        task.getName(),
                        task.getDescription(),
                        task.getDeadline().format(formatter)));
                bw.newLine();
            }
        }finally {
            if(bw != null){
                bw.close();
            }
        }

    }

    public void deleteTask(Task task){
        tasks.remove(task);
    }

}
