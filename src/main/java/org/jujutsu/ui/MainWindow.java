package org.jujutsu.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jujutsu.model.Mission;
import org.jujutsu.model.Sorcerer;
import org.jujutsu.model.Technique;
import org.jujutsu.parser.ParserFactory;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class MainWindow {

    private final Stage stage;
    private final ListView<Mission> missionList = new ListView<>();
    private final ObservableList<Mission> missions = FXCollections.observableArrayList();

    // Detail pane fields
    private final Label lblId = styledLabel("");
    private final Label lblDate = styledLabel("");
    private final Label lblLocation = styledLabel("");
    private final Label lblOutcome = styledLabel("");
    private final Label lblDamage = styledLabel("");
    private final Label lblCurseName = styledLabel("");
    private final Label lblCurseThreat = styledLabel("");
    private final Label lblNote = styledLabel("");

    @SuppressWarnings("unchecked")
    private final TableView<Sorcerer> sorcererTable = new TableView<>();
    @SuppressWarnings("unchecked")
    private final TableView<Technique> techniqueTable = new TableView<>();

    public MainWindow(Stage stage) {
        this.stage = stage;
    }

    public BorderPane buildLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        root.setTop(buildTopBar());
        root.setLeft(buildLeftPanel());
        root.setCenter(buildDetailPanel());

        missionList.setItems(missions);
        missionList.setCellFactory(lv -> new MissionCell());
        missionList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, selected) -> showMission(selected));

        return root;
    }

    // ─── Top Bar ─────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color: #16213e;");

        Text title = new Text("Анализатор миссий — Токийский магический колледж");
        title.setFill(Color.web("#e2b96f"));
        title.setFont(Font.font("System", FontWeight.BOLD, 15));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnOpen = new Button("Открыть файл миссии");
        styleButton(btnOpen);
        btnOpen.setOnAction(e -> openFile());

        bar.getChildren().addAll(title, spacer, btnOpen);
        return bar;
    }

    // ─── Left Panel ──────────────────────────────────────────────────────────

    private VBox buildLeftPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(240);
        panel.setStyle("-fx-background-color: #16213e;");

        Label header = new Label("Загруженные миссии");
        header.setFont(Font.font("System", FontWeight.BOLD, 13));
        header.setTextFill(Color.web("#a0aec0"));

        missionList.setStyle("-fx-background-color: #0f3460; -fx-border-color: #2d3748;");
        missionList.setPrefHeight(600);
        VBox.setVgrow(missionList, Priority.ALWAYS);

        panel.getChildren().addAll(header, missionList);
        return panel;
    }

    // ─── Detail Panel ────────────────────────────────────────────────────────

    private ScrollPane buildDetailPanel() {
        VBox detail = new VBox(16);
        detail.setPadding(new Insets(20));
        detail.setStyle("-fx-background-color: #1a1a2e;");

        detail.getChildren().addAll(
                buildSection("Общая информация", buildGeneralGrid()),
                buildSection("Проклятие", buildCurseGrid()),
                buildSection("Участники операции", sorcererTable),
                buildSection("Применённые техники", techniqueTable),
                buildNoteSection()
        );

        buildSorcererTable();
        buildTechniqueTable();

        ScrollPane scroll = new ScrollPane(detail);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        return scroll;
    }

    private TitledPane buildSection(String title, javafx.scene.Node content) {
        TitledPane pane = new TitledPane(title, content);
        pane.setExpanded(true);
        pane.setStyle("""
                -fx-text-fill: #e2b96f;
                -fx-background-color: #16213e;
                -fx-border-color: #2d3748;
                -fx-font-weight: bold;
                """);
        return pane;
    }

    private GridPane buildGeneralGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #16213e;");

        addRow(grid, 0, "ID миссии:", lblId);
        addRow(grid, 1, "Дата:", lblDate);
        addRow(grid, 2, "Локация:", lblLocation);
        addRow(grid, 3, "Исход:", lblOutcome);
        addRow(grid, 4, "Ущерб:", lblDamage);
        return grid;
    }

    private GridPane buildCurseGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #16213e;");

        addRow(grid, 0, "Название:", lblCurseName);
        addRow(grid, 1, "Уровень угрозы:", lblCurseThreat);
        return grid;
    }

    private VBox buildNoteSection() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #16213e; -fx-border-color: #2d3748; -fx-border-radius: 4;");

        Label header = new Label("Примечание");
        header.setFont(Font.font("System", FontWeight.BOLD, 13));
        header.setTextFill(Color.web("#e2b96f"));

        lblNote.setWrapText(true);
        box.getChildren().addAll(header, lblNote);
        return box;
    }

    @SuppressWarnings("unchecked")
    private void buildSorcererTable() {
        TableColumn<Sorcerer, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Sorcerer, String> rankCol = new TableColumn<>("Ранг");
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        rankCol.setPrefWidth(140);

        sorcererTable.getColumns().addAll(nameCol, rankCol);
        styleTable(sorcererTable);
    }

    @SuppressWarnings("unchecked")
    private void buildTechniqueTable() {
        TableColumn<Technique, String> nameCol = new TableColumn<>("Техника");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Technique, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);

        TableColumn<Technique, String> ownerCol = new TableColumn<>("Владелец");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("owner"));
        ownerCol.setPrefWidth(180);

        TableColumn<Technique, Long> dmgCol = new TableColumn<>("Урон (¥)");
        dmgCol.setCellValueFactory(new PropertyValueFactory<>("damage"));
        dmgCol.setPrefWidth(110);

        techniqueTable.getColumns().addAll(nameCol, typeCol, ownerCol, dmgCol);
        styleTable(techniqueTable);
    }

    // ─── Logic ───────────────────────────────────────────────────────────────

    private void openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите файл миссии");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Файлы миссий", "*.txt", "*.json", "*.xml"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            Mission mission = ParserFactory.getParser(file).parse(file);
            // Avoid duplicates by ID
            missions.removeIf(m -> m.getMissionId().equals(mission.getMissionId()));
            missions.add(mission);
            missionList.getSelectionModel().select(mission);
        } catch (Exception ex) {
            showError("Ошибка загрузки файла", ex.getMessage());
        }
    }

    private void showMission(Mission m) {
        if (m == null) return;

        lblId.setText(m.getMissionId());
        lblDate.setText(m.getDate());
        lblLocation.setText(m.getLocation());
        lblDamage.setText(formatMoney(m.getDamageCost()));
        lblNote.setText(m.getNote() != null ? m.getNote() : "—");

        // Outcome with color
        lblOutcome.setText(m.getOutcome());
        if ("SUCCESS".equalsIgnoreCase(m.getOutcome())) {
            lblOutcome.setTextFill(Color.web("#48bb78"));
        } else if ("FAILURE".equalsIgnoreCase(m.getOutcome())) {
            lblOutcome.setTextFill(Color.web("#fc8181"));
        } else {
            lblOutcome.setTextFill(Color.web("#e2e8f0"));
        }

        if (m.getCurse() != null) {
            lblCurseName.setText(m.getCurse().getName());
            String threat = m.getCurse().getThreatLevel();
            lblCurseThreat.setText(threat);
            lblCurseThreat.setTextFill(threatColor(threat));
        }

        sorcererTable.setItems(FXCollections.observableArrayList(
                m.getSorcerers() != null ? m.getSorcerers() : java.util.List.of()));
        techniqueTable.setItems(FXCollections.observableArrayList(
                m.getTechniques() != null ? m.getTechniques() : java.util.List.of()));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void addRow(GridPane grid, int row, String labelText, Label value) {
        Label key = new Label(labelText);
        key.setTextFill(Color.web("#a0aec0"));
        key.setFont(Font.font("System", FontWeight.BOLD, 13));
        grid.add(key, 0, row);
        grid.add(value, 1, row);
    }

    private static Label styledLabel(String text) {
        Label lbl = new Label(text);
        lbl.setTextFill(Color.web("#e2e8f0"));
        lbl.setFont(Font.font("System", 13));
        return lbl;
    }

    private void styleButton(Button btn) {
        btn.setStyle("""
                -fx-background-color: #e2b96f;
                -fx-text-fill: #1a1a2e;
                -fx-font-weight: bold;
                -fx-border-radius: 4;
                -fx-background-radius: 4;
                -fx-padding: 6 14;
                """);
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#e2b96f", "#c9a45a")));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("#c9a45a", "#e2b96f")));
    }

    private <T> void styleTable(TableView<T> table) {
        table.setStyle("""
                -fx-background-color: #0f3460;
                -fx-table-cell-border-color: #2d3748;
                -fx-text-fill: #e2e8f0;
                """);
        table.setPrefHeight(150);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private Color threatColor(String threat) {
        return switch (threat.toUpperCase()) {
            case "SPECIAL_GRADE" -> Color.web("#fc8181");
            case "HIGH" -> Color.web("#f6ad55");
            case "MEDIUM" -> Color.web("#faf089");
            default -> Color.web("#e2e8f0");
        };
    }

    private String formatMoney(long amount) {
        return NumberFormat.getNumberInstance(Locale.of("ru")).format(amount) + " ¥";
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ─── Mission List Cell ───────────────────────────────────────────────────

    private static class MissionCell extends ListCell<Mission> {
        @Override
        protected void updateItem(Mission item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            VBox box = new VBox(2);
            Label id = new Label(item.getMissionId());
            id.setFont(Font.font("System", FontWeight.BOLD, 12));
            id.setTextFill(Color.web("#e2b96f"));

            Label loc = new Label(item.getLocation());
            loc.setFont(Font.font("System", 11));
            loc.setTextFill(Color.web("#a0aec0"));

            String outcome = item.getOutcome();
            Label out = new Label(outcome);
            out.setFont(Font.font("System", FontWeight.BOLD, 11));
            out.setTextFill("SUCCESS".equalsIgnoreCase(outcome)
                    ? Color.web("#48bb78") : Color.web("#fc8181"));

            box.getChildren().addAll(id, loc, out);
            setGraphic(box);
            setStyle("-fx-background-color: transparent;");
        }
    }
}
