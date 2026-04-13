package hostel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HostelApp extends Application implements AttendanceTasks.StatusCallback, AttendanceTasks.ClockCallback {

    // ── Data ──────────────────────────────────────
    private final ObservableList<Student> pendingList = FXCollections.observableArrayList();
    private final ObservableList<Student> completedList = FXCollections.observableArrayList();
    private final List<Student> allStudents = new ArrayList<>();
    private AttendanceTasks.ClockUpdater clockUpdater;

    // ── UI References ─────────────────────────────
    private TableView<Student> pendingTable;
    private TableView<Student> historyTable;
    private Label statusLabel;
    private Label totalLabel;
    private Label presentLabel;
    private Label absentLabel;
    private Label leaveLabel;
    private Label clockLabel;
    
    private TextField nameField;
    private TextField rollField;
    private TextField roomField;
    private ComboBox<String> blockBox;
    private Button registerBtn;
    
    private Button markPresentBtn;
    private Button markAbsentBtn;
    private Button markLeaveBtn;

    // ── State ─────────────────────────────────────
    private boolean taskRunning = false;

    // ─────────────────────────────────────────────
    @Override
    public void start(Stage primaryStage) {
        // Load saved data
        List<Student> saved = DataManager.loadStudents();
        if (!saved.isEmpty()) {
            allStudents.addAll(saved);
            int maxId = saved.stream().mapToInt(Student::getId).max().orElse(0);
            Student.setCounter(maxId + 1);
            refreshTables();
        }

        // Build UI
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0b0f19;");

        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(buildCenter());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1150, 750);
        String css = getClass().getResource("/hostel/style.css") != null ? 
        getClass().getResource("/hostel/style.css").toExternalForm() : "";
        if (!css.isEmpty()) scene.getStylesheets().add(css);

        primaryStage.setTitle("CampusSync – Hostel Attendance Management");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();

        // Start background clock updater
        clockUpdater = new AttendanceTasks.ClockUpdater(this);
        Thread t = new Thread(clockUpdater);
        t.setDaemon(true);
        t.start();

        primaryStage.setOnCloseRequest(e -> {
            clockUpdater.stop();
            DataManager.saveStudents(allStudents);
        });

        updateStats();
    }

    // ─────────────────────────────────────────────
    //  HEADER
    // ─────────────────────────────────────────────
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(18, 28, 18, 28));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(16);
        header.setStyle("-fx-background-color: #0f172a; -fx-border-color: #1e293b; -fx-border-width: 0 0 1 0;");

        // Icon
        Label icon = new Label("🏢");
        icon.setStyle("-fx-font-size: 26px;");

        Label title = new Label("CampusSync");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");

        Label subtitle = new Label("Hostel Attendance Console");
        subtitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-font-family: 'Segoe UI';");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Stats chips
        totalLabel = new Label("👥 Total: 0");
        totalLabel.getStyleClass().add("stat-chip");

        presentLabel = new Label("✔ Present: 0");
        presentLabel.getStyleClass().add("stat-chip-green");

        absentLabel = new Label("✖ Absent: 0");
        absentLabel.getStyleClass().add("stat-chip-red");
        
        leaveLabel = new Label("✈ Leave: 0");
        leaveLabel.getStyleClass().add("stat-chip-gold");

        header.getChildren().addAll(icon, title, subtitle, spacer, totalLabel, presentLabel, absentLabel, leaveLabel);
        return header;
    }

    // ─────────────────────────────────────────────
    //  SIDEBAR – Registration & Actions
    // ─────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(24, 20, 24, 20));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: #0f172a; -fx-border-color: #1e293b; -fx-border-width: 0 1 0 0;");

        Label regTitle = new Label("ONBOARD STUDENT");
        regTitle.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 12px; -fx-font-weight: bold; -fx-letter-spacing: 1.5px;");

        nameField = styledField("Full Name");
        rollField = styledField("Roll Number");
        roomField = styledField("Room Number");

        blockBox = new ComboBox<>();
        blockBox.getItems().addAll("Block A", "Block B", "Block C", "Block D");
        blockBox.setValue("Block A");
        blockBox.setMaxWidth(Double.MAX_VALUE);
        blockBox.getStyleClass().add("combo-dark");

        registerBtn = new Button("➕ Register Student");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setOnAction(e -> registerStudent());

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #1e293b;");
        sep.setPadding(new Insets(10, 0, 10, 0));

        Label actTitle = new Label("ATTENDANCE ACTIONS");
        actTitle.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-letter-spacing: 1.5px;");

        markPresentBtn = new Button("✔ Mark Present (Biometric)");
        markPresentBtn.setMaxWidth(Double.MAX_VALUE);
        markPresentBtn.getStyleClass().add("btn-success");
        markPresentBtn.setOnAction(e -> markPresent());

        markAbsentBtn = new Button("✖ Mark Absent (SMS Alert)");
        markAbsentBtn.setMaxWidth(Double.MAX_VALUE);
        markAbsentBtn.getStyleClass().add("btn-danger");
        markAbsentBtn.setOnAction(e -> markAbsent());

        markLeaveBtn = new Button("✈ Process Leave");
        markLeaveBtn.setMaxWidth(Double.MAX_VALUE);
        markLeaveBtn.getStyleClass().add("btn-warning");
        markLeaveBtn.setOnAction(e -> markLeave());

        sidebar.getChildren().addAll(
            regTitle,
            label("Student Name"), nameField,
            label("Roll Number"), rollField,
            label("Room Number"), roomField,
            label("Assigned Block"), blockBox,
            registerBtn, sep, actTitle,
            markPresentBtn, markAbsentBtn, markLeaveBtn
        );
        return sidebar;
    }

    // ─────────────────────────────────────────────
    //  CENTER – Queue Tables
    // ─────────────────────────────────────────────
    private VBox buildCenter() {
        VBox center = new VBox(0);
        center.setStyle("-fx-background-color: #0b0f19;");

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getStyleClass().add("dark-tabs");

        Tab pendingTab = new Tab("  ⏳ Pending Actions  ");
        pendingTab.setContent(buildPendingTable());

        Tab historyTab = new Tab("  📋 Attendance Log  ");
        historyTab.setContent(buildHistoryTable());

        tabs.getTabs().addAll(pendingTab, historyTab);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        center.getChildren().add(tabs);
        return center;
    }

    @SuppressWarnings("unchecked")
    private VBox buildPendingTable() {
        pendingTable = new TableView<>();
        pendingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pendingTable.getStyleClass().add("dark-table");
        pendingTable.setPlaceholder(new Label("No pending students."));

        TableColumn<Student, String> rollCol = new TableColumn<>("Roll No.");
        rollCol.setCellValueFactory(new PropertyValueFactory<>("rollNumber"));
        rollCol.setMaxWidth(120);

        TableColumn<Student, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> blockCol = new TableColumn<>("Block");
        blockCol.setCellValueFactory(new PropertyValueFactory<>("blockString"));
        blockCol.setMaxWidth(100);

        TableColumn<Student, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomCol.setMaxWidth(80);

        TableColumn<Student, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusString"));
        statusCol.setMaxWidth(120);

        pendingTable.getColumns().addAll(rollCol, nameCol, blockCol, roomCol, statusCol);
        pendingTable.setItems(pendingList);

        VBox wrapper = new VBox(pendingTable);
        VBox.setVgrow(pendingTable, Priority.ALWAYS);
        wrapper.setPadding(new Insets(16));
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private VBox buildHistoryTable() {
        historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.getStyleClass().add("dark-table");
        historyTable.setPlaceholder(new Label("No completed attendance logs."));

        TableColumn<Student, String> rollCol = new TableColumn<>("Roll No.");
        rollCol.setCellValueFactory(new PropertyValueFactory<>("rollNumber"));
        rollCol.setMaxWidth(120);

        TableColumn<Student, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, String> blockCol = new TableColumn<>("Block");
        blockCol.setCellValueFactory(new PropertyValueFactory<>("blockString"));
        blockCol.setMaxWidth(100);

        TableColumn<Student, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomCol.setMaxWidth(80);

        TableColumn<Student, String> statusCol = new TableColumn<>("Final Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusString"));
        statusCol.setMaxWidth(150);

        historyTable.getColumns().addAll(rollCol, nameCol, blockCol, roomCol, statusCol);
        
        // Color code the final status rows
        historyTable.setRowFactory(tv -> new TableRow<Student>() {
            @Override
            protected void updateItem(Student s, boolean empty) {
                super.updateItem(s, empty);
                if (s == null || empty) {
                    setStyle("");
                } else {
                    switch (s.getStatus()) {
                        case PRESENT:
                            setStyle("-fx-background-color: rgba(34, 197, 94, 0.1);");
                            break;
                        case ABSENT:
                            setStyle("-fx-background-color: rgba(244, 63, 94, 0.1);");
                            break;
                        case ON_LEAVE:
                            setStyle("-fx-background-color: rgba(234, 179, 8, 0.1);");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        historyTable.setItems(completedList);

        VBox wrapper = new VBox(historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        wrapper.setPadding(new Insets(16));
        
        // Add a Clear button for history
        Button clearBtn = new Button("🗑 Clear Logs & Reset Pending");
        clearBtn.getStyleClass().add("btn-secondary");
        clearBtn.setOnAction(e -> {
            for (Student s : allStudents) {
                s.setStatus(Student.Status.PENDING);
            }
            refreshTables();
            updateStats();
            DataManager.saveStudents(allStudents);
            statusLabel.setText("All students have been reset to PENDING status for a new day.");
        });
        
        wrapper.getChildren().add(clearBtn);
        return wrapper;
    }

    // ─────────────────────────────────────────────
    //  STATUS BAR
    // ─────────────────────────────────────────────
    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(12, 24, 12, 24));
        bar.setStyle("-fx-background-color: #0f172a; -fx-border-color: #1e293b; -fx-border-width: 1 0 0 0;");
        bar.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("System ready. Select a student to mark attendance.");
        statusLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        clockLabel = new Label();
        clockLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px; -fx-font-weight: bold;");

        bar.setSpacing(10);
        bar.getChildren().addAll(dot, statusLabel, spacer, clockLabel);
        return bar;
    }

    // ─────────────────────────────────────────────
    //  LOGIC
    // ─────────────────────────────────────────────
    private void registerStudent() {
        String name = nameField.getText().trim();
        String roll = rollField.getText().trim();
        String room = roomField.getText().trim();
        String blockStr = blockBox.getValue();

        if (name.isEmpty() || roll.isEmpty() || room.isEmpty()) {
            showAlert("Missing Information", "Please fill in all student details.", Alert.AlertType.WARNING);
            return;
        }

        Student.Block block;
        switch (blockStr) {
            case "Block A": block = Student.Block.BLOCK_A; break;
            case "Block B": block = Student.Block.BLOCK_B; break;
            case "Block C": block = Student.Block.BLOCK_C; break;
            case "Block D": block = Student.Block.BLOCK_D; break;
            default: block = Student.Block.BLOCK_A; break;
        }

        Student student = new Student(name, roll, room, block);
        allStudents.add(student);
        DataManager.saveStudents(allStudents);
        refreshTables();
        updateStats();

        statusLabel.setText("Student " + name + " (" + roll + ") registered successfully.");

        nameField.clear(); rollField.clear(); roomField.clear();
        blockBox.setValue("Block A");
    }

    private void markPresent() {
        Student selected = pendingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a student from the pending list.", Alert.AlertType.INFORMATION);
            return;
        }
        if (taskRunning) {
            showAlert("Processing", "A background task is already running. Please wait.", Alert.AlertType.WARNING);
            return;
        }

        taskRunning = true;
        setButtonsDisabled(true);

        Thread t = new Thread(new AttendanceTasks.MarkPresentTask(selected, this));
        t.setDaemon(true);
        t.start();
    }

    private void markAbsent() {
        Student selected = pendingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a student from the pending list.", Alert.AlertType.INFORMATION);
            return;
        }
        if (taskRunning) {
            showAlert("Processing", "A background task is already running. Please wait.", Alert.AlertType.WARNING);
            return;
        }

        taskRunning = true;
        setButtonsDisabled(true);

        Thread t = new Thread(new AttendanceTasks.MarkAbsentTask(selected, this));
        t.setDaemon(true);
        t.start();
    }

    private void markLeave() {
        Student selected = pendingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a student from the pending list.", Alert.AlertType.INFORMATION);
            return;
        }
        if (taskRunning) {
            showAlert("Processing", "A background task is already running. Please wait.", Alert.AlertType.WARNING);
            return;
        }

        taskRunning = true;
        setButtonsDisabled(true);

        Thread t = new Thread(new AttendanceTasks.MarkLeaveTask(selected, this));
        t.setDaemon(true);
        t.start();
    }

    private void refreshTables() {
        if (pendingTable == null || historyTable == null) return;
        
        pendingList.setAll(
            allStudents.stream()
            .filter(s -> s.getStatus() == Student.Status.PENDING)
            .collect(Collectors.toList())
        );
        pendingTable.refresh();

        completedList.setAll(
            allStudents.stream()
            .filter(s -> s.getStatus() != Student.Status.PENDING)
            .collect(Collectors.toList())
        );
        historyTable.refresh();
    }

    private void updateStats() {
        long total = allStudents.size();
        long present = allStudents.stream().filter(s -> s.getStatus() == Student.Status.PRESENT).count();
        long absent = allStudents.stream().filter(s -> s.getStatus() == Student.Status.ABSENT).count();
        long leave = allStudents.stream().filter(s -> s.getStatus() == Student.Status.ON_LEAVE).count();

        totalLabel.setText("👥 Total: " + total);
        presentLabel.setText("✔ Present: " + present);
        absentLabel.setText("✖ Absent: " + absent);
        leaveLabel.setText("✈ Leave: " + leave);
    }
    
    private void setButtonsDisabled(boolean disabled) {
        markPresentBtn.setDisable(disabled);
        markAbsentBtn.setDisable(disabled);
        markLeaveBtn.setDisable(disabled);
        registerBtn.setDisable(disabled);
    }

    // ─────────────────────────────────────────────
    //  Callbacks from threads
    // ─────────────────────────────────────────────
    @Override
    public void onUpdate(String message) {
        statusLabel.setText(message);
    }

    @Override
    public void onVerificationComplete(Student student) {
        finishTask();
    }

    @Override
    public void onAlertComplete(Student student) {
        finishTask();
    }

    @Override
    public void onLeaveProcessed(Student student) {
        finishTask();
    }
    
    private void finishTask() {
        taskRunning = false;
        setButtonsDisabled(false);
        refreshTables();
        updateStats();
        DataManager.saveStudents(allStudents);
    }

    @Override
    public void onTick() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        LocalDateTime now = LocalDateTime.now();  
        clockLabel.setText(dtf.format(now));
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────
    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("dark-field");
        return tf;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        return l;
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
