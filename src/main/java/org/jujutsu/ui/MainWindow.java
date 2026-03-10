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
import java.util.List;
import java.util.Locale;

public class MainWindow {

    private final Stage stage;
    private final ObservableList<Mission> missions = FXCollections.observableArrayList();
    private final ListView<Mission> missionList = new ListView<>(missions);

    private final Label lblId          = lbl(""), lblDate     = lbl(""),
                        lblLocation    = lbl(""), lblOutcome  = lbl(""),
                        lblDamage      = lbl(""), lblCurseName = lbl(""),
                        lblCurseThreat = lbl(""), lblNote     = lbl("");

    private final TableView<Sorcerer>  sorcTable = new TableView<>();
    private final TableView<Technique> techTable = new TableView<>();

    public MainWindow(Stage stage) { this.stage = stage; }

    public BorderPane buildLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        root.setTop(buildTopBar());
        root.setLeft(buildLeftPanel());
        root.setCenter(buildDetailPanel());

        missionList.setCellFactory(lv -> new MissionCell());
        missionList.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> showMission(sel));

        setupTable(sorcTable,
                new String[]{"Имя", "Ранг"},
                new String[]{"name", "rank"},
                new int[]{180, 140});
        setupTable(techTable,
                new String[]{"Техника", "Тип", "Владелец", "Урон (¥)"},
                new String[]{"name", "type", "owner", "damage"},
                new int[]{200, 120, 180, 110});

        return root;
    }

    private HBox buildTopBar() {
        Text title = new Text("Анализатор миссий — Токийский магический колледж");
        title.setFill(Color.web("#e2b96f"));
        title.setFont(Font.font("System", FontWeight.BOLD, 15));

        Button btn = new Button("Открыть файл миссии");
        btn.setStyle("-fx-background-color: #e2b96f; -fx-text-fill: #1a1a2e;" +
                     "-fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 6 14;");
        btn.setOnAction(e -> openFile());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(12, title, spacer, btn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color: #16213e;");
        return bar;
    }

    private VBox buildLeftPanel() {
        Label header = new Label("Загруженные миссии");
        header.setFont(Font.font("System", FontWeight.BOLD, 13));
        header.setTextFill(Color.web("#a0aec0"));

        missionList.setStyle("-fx-background-color: #0f3460; -fx-border-color: #2d3748;");
        VBox.setVgrow(missionList, Priority.ALWAYS);

        VBox panel = new VBox(8, header, missionList);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(240);
        panel.setStyle("-fx-background-color: #16213e;");
        return panel;
    }

    private ScrollPane buildDetailPanel() {
        GridPane general = grid();
        addRow(general, 0, "ID миссии:", lblId);
        addRow(general, 1, "Дата:",      lblDate);
        addRow(general, 2, "Локация:",   lblLocation);
        addRow(general, 3, "Исход:",     lblOutcome);
        addRow(general, 4, "Ущерб:",     lblDamage);

        GridPane curse = grid();
        addRow(curse, 0, "Название:",        lblCurseName);
        addRow(curse, 1, "Уровень угрозы:",  lblCurseThreat);

        Label noteHeader = new Label("Примечание");
        noteHeader.setFont(Font.font("System", FontWeight.BOLD, 13));
        noteHeader.setTextFill(Color.web("#e2b96f"));
        lblNote.setWrapText(true);
        VBox noteBox = new VBox(6, noteHeader, lblNote);
        noteBox.setPadding(new Insets(10));
        noteBox.setStyle("-fx-background-color: #16213e; -fx-border-color: #2d3748; -fx-border-radius: 4;");

        VBox detail = new VBox(16,
                section("Общая информация",     general),
                section("Проклятие",            curse),
                section("Участники операции",   sorcTable),
                section("Применённые техники",  techTable),
                noteBox);
        detail.setPadding(new Insets(20));
        detail.setStyle("-fx-background-color: #1a1a2e;");

        ScrollPane scroll = new ScrollPane(detail);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1a1a2e; -fx-background-color: #1a1a2e;");
        return scroll;
    }

    private TitledPane section(String title, javafx.scene.Node content) {
        TitledPane p = new TitledPane(title, content);
        p.setExpanded(true);
        p.setStyle("-fx-text-fill: #e2b96f; -fx-background-color: #16213e;" +
                   "-fx-border-color: #2d3748; -fx-font-weight: bold;");
        return p;
    }

    private GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(16); g.setVgap(8);
        g.setPadding(new Insets(10));
        g.setStyle("-fx-background-color: #16213e;");
        return g;
    }

    private void addRow(GridPane g, int row, String key, Label val) {
        Label k = new Label(key);
        k.setTextFill(Color.web("#a0aec0"));
        k.setFont(Font.font("System", FontWeight.BOLD, 13));
        g.add(k, 0, row);
        g.add(val, 1, row);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setupTable(TableView table, String[] titles, String[] props, int[] widths) {
        for (int i = 0; i < titles.length; i++) {
            TableColumn col = new TableColumn<>(titles[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            col.setPrefWidth(widths[i]);
            table.getColumns().add(col);
        }
        table.setStyle("-fx-background-color: #0f3460; -fx-table-cell-border-color: #2d3748;");
        table.setPrefHeight(150);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void openFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите файл миссии");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Файлы миссий", "*.txt", "*.json", "*.xml"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;
        try {
            Mission m = ParserFactory.getParser(file).parse(file);
            missions.removeIf(x -> x.getMissionId().equals(m.getMissionId()));
            missions.add(m);
            missionList.getSelectionModel().select(m);
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            alert.setHeaderText("Ошибка загрузки файла");
            alert.showAndWait();
        }
    }

    private void showMission(Mission m) {
        if (m == null) return;
        lblId.setText(m.getMissionId());
        lblDate.setText(m.getDate());
        lblLocation.setText(m.getLocation());
        lblNote.setText(m.getNote() != null ? m.getNote() : "—");
        lblDamage.setText(NumberFormat.getNumberInstance(Locale.of("ru")).format(m.getDamageCost()) + " ¥");

        lblOutcome.setText(m.getOutcome());
        lblOutcome.setTextFill(Color.web("SUCCESS".equalsIgnoreCase(m.getOutcome()) ? "#48bb78" : "#fc8181"));

        if (m.getCurse() != null) {
            lblCurseName.setText(m.getCurse().getName());
            String t = m.getCurse().getThreatLevel();
            lblCurseThreat.setText(t);
            lblCurseThreat.setTextFill(switch (t.toUpperCase()) {
                case "SPECIAL_GRADE" -> Color.web("#fc8181");
                case "HIGH"          -> Color.web("#f6ad55");
                case "MEDIUM"        -> Color.web("#faf089");
                default              -> Color.web("#e2e8f0");
            });
        }

        sorcTable.setItems(FXCollections.observableArrayList(
                m.getSorcerers() != null ? m.getSorcerers() : List.of()));
        techTable.setItems(FXCollections.observableArrayList(
                m.getTechniques() != null ? m.getTechniques() : List.of()));
    }

    private static Label lbl(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#e2e8f0"));
        l.setFont(Font.font("System", 13));
        return l;
    }

    private static class MissionCell extends ListCell<Mission> {
        @Override
        protected void updateItem(Mission item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); setStyle("-fx-background-color: transparent;"); return; }

            Label id  = new Label(item.getMissionId());
            id.setFont(Font.font("System", FontWeight.BOLD, 12));
            id.setTextFill(Color.web("#e2b96f"));

            Label loc = new Label(item.getLocation());
            loc.setFont(Font.font("System", 11));
            loc.setTextFill(Color.web("#a0aec0"));

            Label out = new Label(item.getOutcome());
            out.setFont(Font.font("System", FontWeight.BOLD, 11));
            out.setTextFill(Color.web("SUCCESS".equalsIgnoreCase(item.getOutcome()) ? "#48bb78" : "#fc8181"));

            setGraphic(new VBox(2, id, loc, out));
            setStyle("-fx-background-color: transparent;");
        }
    }
}
