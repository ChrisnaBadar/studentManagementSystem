import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class App extends Application {

    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private TableView<Student> tableView = new TableView<>();
    private ComboBox<String> courseComboBox = new ComboBox<>();

    // File path for the CSV file
    private static final String FILE_PATH = "students.csv";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        studentList = readDataFromFile();
        primaryStage.setTitle("Student Management System");

        // Create a table to display student records
        TableColumn<Student, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<Student, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());

        TableColumn<Student, String> courseColumn = new TableColumn<>("Course");
        courseColumn.setCellValueFactory(cellData -> cellData.getValue().courseProperty());

        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(idColumn);
        tableView.getColumns().add(courseColumn);
        tableView.setItems(studentList);

        // Column for Grades
        TableColumn<Student, Number> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellValueFactory(cellData -> cellData.getValue().gradeProperty());
        tableView.getColumns().add(gradeColumn);

        // Adding a button to each row in the table to view details
        TableColumn<Student, Void> detailsColumn = new TableColumn<>("Details");
        detailsColumn.setCellFactory(param -> new TableCell<Student, Void>() {
            private final Button detailsButton = new Button("View Details");

            {
                detailsButton.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    showStudentDetails(student);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });
        tableView.getColumns().add(detailsColumn);

        // Add Student Button
        Button addStudentButton = new Button("Add Student");
        addStudentButton.setOnAction(e -> openAddStudentForm());

                // Save Data Button
        Button saveButton = new Button("Save Data");
        saveButton.setOnAction(e -> {
            // Create a popup for the progress indicator
            Stage progressStage = new Stage();
            progressStage.initModality(Modality.APPLICATION_MODAL);
            progressStage.setTitle("Saving Data");

            ProgressIndicator progressIndicator = new ProgressIndicator();
            VBox progressBox = new VBox(10, new Label("Saving..."), progressIndicator);
            progressBox.setAlignment(Pos.CENTER);
            progressBox.setPadding(new Insets(20));

            Scene progressScene = new Scene(progressBox);
            progressStage.setScene(progressScene);
            progressStage.show();

            // Run the save operation in a separate thread
            new Thread(() -> {
                try {
                    writeDataToFile(studentList);
                    Platform.runLater(() -> {
                        progressStage.close(); // Close the progress indicator popup

                        // Show the confirmation alert
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Save Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("Data saved to students.csv");
                        alert.showAndWait();
                    });
                } catch (IOException ex) {
                    Platform.runLater(() -> {
                        progressStage.close(); // Close the progress indicator popup

                        // Show error alert
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Save Failed");
                        errorAlert.setContentText("An error occurred while saving data: " + ex.getMessage());
                        errorAlert.showAndWait();
                    });
                }
            }).start();
        });



        // Button Layout
        HBox buttonLayout = new HBox(10);
        buttonLayout.setPadding(new Insets(10));
        buttonLayout.getChildren().addAll(addStudentButton, saveButton);


        // Populate the course ComboBox
        courseComboBox.getItems().addAll("Math", "Science", "History", "English");

        // Layout for input fields and buttons
        GridPane inputGrid = new GridPane();
        inputGrid.setPadding(new Insets(10, 10, 10, 10));
        inputGrid.setVgap(10);
        inputGrid.setHgap(10);

        // Main layout
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tableView);
        borderPane.setBottom(buttonLayout);
    
        // Create a layout that combines inputGrid and buttonLayout
        VBox bottomLayout = new VBox();
        bottomLayout.getChildren().addAll(inputGrid, buttonLayout);
        borderPane.setBottom(bottomLayout);

        primaryStage.setOnCloseRequest(event -> {
            if (isDataDifferent()) {
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Unsaved Changes");
                confirmDialog.setHeaderText("Save Changes");
                confirmDialog.setContentText("Would you like to save your last update?");
    
                Optional<ButtonType> result = confirmDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        writeDataToFile(studentList);
                    } catch (IOException ex) {
                        ex.printStackTrace(); // Handle save error
                    }
                }
            }
        });

        Scene scene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean isDataDifferent() {
        try {
            ObservableList<Student> fileData = readDataFromFile();
            // Using equals method for comparison
            return !studentList.equals(fileData);
        } catch (IOException e) {
            e.printStackTrace();
            return true; // Assume data is different if there's an error reading the file
        }
    }
    

    private void openAddStudentForm() {
        Stage addStudentStage = new Stage();
        addStudentStage.setTitle("Add New Student");
    
        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField idField = new TextField();
        idField.setPromptText("ID");
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.getItems().addAll("Math", "Science", "History", "English");
        courseComboBox.setPromptText("Course");
    
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            String name = nameField.getText();
            String id = idField.getText();
            String course = courseComboBox.getValue();
            try {
                Student newStudent = new Student(name, id, course);
                studentList.add(newStudent);
                tableView.refresh();
                addStudentStage.close();
            } catch (NumberFormatException ex) {
                System.out.println("Invalid grade input"); // Replace with error handling logic
            }
        });
    
        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));
        formLayout.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("ID:"), idField,
            new Label("Course:"), courseComboBox,
            addButton
        );
    
        addStudentStage.setScene(new Scene(formLayout, 300, 300));
        addStudentStage.show();
    }

    private void showStudentDetails(Student student) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Student Details");
    
        TextField nameField = new TextField(student.getName());
        TextField idField = new TextField(student.getId());
    
        // ComboBox for course selection
        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.getItems().addAll("Math", "Science", "History", "English"); // Add your courses here
        courseComboBox.setValue(student.getCourse()); // Set to current course
    
        TextField gradeField = new TextField(String.valueOf(student.getGrade()));
    
        Button updateButton = new Button("Update");
        updateButton.setOnAction(e -> {
            // Update the student's information
            student.setName(nameField.getText());
            student.setId(idField.getText());
            student.setCourse(courseComboBox.getValue());
            try {
                double grade = Double.parseDouble(gradeField.getText());
                student.setGrade(grade);
            } catch (NumberFormatException ex) {
                // Handle invalid grade input
            }
            tableView.refresh();  // Refresh the table to show updated data
            detailsStage.close(); // Close the details window
        });

        // Delete Button
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirm Deletion");
            confirmDialog.setHeaderText("Delete Student Record");
            confirmDialog.setContentText("Delete " + student.getName() + " from record?");

            // Optional: Customizing the buttons
            ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmDialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeCancel);

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == buttonTypeYes) {
                studentList.remove(student);
                tableView.refresh();
                detailsStage.close();
            }
        });
    
        // Layout for buttons
        HBox buttonLayout = new HBox(10);
        buttonLayout.getChildren().addAll(updateButton, deleteButton);
    
        // Layout for the form
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(
            new Label("Name:"), nameField,
            new Label("ID:"), idField,
            new Label("Course:"), courseComboBox,
            new Label("Grade:"), gradeField,
            buttonLayout
        );
    
        Scene scene = new Scene(layout, 300, 350);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    private void writeDataToFile(ObservableList<Student> studentList) throws IOException {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            for (Student student : studentList) {
                writer.write(student.getName() + "," + student.getId() + "," +
                             student.getCourse() + "," + student.getGrade() + "\n");
            }
        }
    }
    
    private ObservableList<Student> readDataFromFile() throws IOException {
        ObservableList<Student> tempList = FXCollections.observableArrayList();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                Student student = new Student(data[0], data[1], data[2]);
                student.setGrade(Double.parseDouble(data[3]));
                tempList.add(student);
            }
        } catch (FileNotFoundException e) {
            // Handle the case where the file does not exist
            System.out.println("No existing file found. A new file will be created.");
        }
        return tempList;
    }
    
    

    public static class Student {
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty id = new SimpleStringProperty();
        private final StringProperty course = new SimpleStringProperty();
        private final DoubleProperty grade = new SimpleDoubleProperty(0.0);


        public Student(String name, String id, String course) {
            setName(name);
            setId(id);
            setCourse(course);
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty idProperty() {
            return id;
        }

        public StringProperty courseProperty() {
            return course;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getId() {
            return id.get();
        }

        public void setId(String id) {
            this.id.set(id);
        }

        public String getCourse() {
            return course.get();
        }

        public void setCourse(String course) {
            this.course.set(course);
        }

        public DoubleProperty gradeProperty() {
            return grade;
        }

        public double getGrade() {
            return grade.get();
        }

        public void setGrade(double grade) {
            this.grade.set(grade);
        }
    }
}
