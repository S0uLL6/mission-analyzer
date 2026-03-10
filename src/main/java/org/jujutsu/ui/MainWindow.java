package org.jujutsu.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

    private static final String BG      = "#1a1a2e";
    private static final String PANEL   = "#16213e";
    private static final String CARD    = "#0f1f3d";
    private static final String ACCENT  = "#e2b96f";
    private static final String BORDER  = "#2d3748";
    private static final String TEXT    = "#e2e8f0";
    private static final String MUTED   = "#a0aec0";
    private static final String SUCCESS = "#48bb78";
    private static final String FAIL    = "#fc8181";

    private final Stage stage;
    private final ObservableList<Mission> missions = FXCollections.observableArrayList();
    private final ListView<Mission> missionList    = new ListView<>(missions);

    private final Label lblId          = lbl(""), lblDate      = lbl(""),
                        lblLocation    = lbl(""), lblOutcome   = lbl(""),
                        lblDamage      = lbl(""), lblCurseName = lbl(""),
                        lblCurseThreat = lbl(""), lblNote      = lbl("");

    private final TableView<Sorcerer>  sorcTable = new TableView<>();
    private final TableView<Technique> techTable = new TableView<>();

    public MainWindow(Stage stage) { this.stage = stage; }

    public BorderPane buildLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");
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
        title.setFill(Color.web(ACCENT));
        title.setFont(Font.font("System", FontWeight.BOLD, 15));

        Button btn = new Button("Открыть файл миссии");
        btn.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: " + BG + ";" +
                     "-fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 7 16;");
        btn.setOnAction(e -> openFile());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(12, title, spacer, btn);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 20, 14, 20));
        bar.setStyle("-fx-background-color: " + PANEL + "; -fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
        return bar;
    }

    private VBox buildLeftPanel() {
        Label header = new Label("МИССИИ");
        header.setFont(Font.font("System", FontWeight.BOLD, 10));
        header.setTextFill(Color.web(MUTED));
        header.setMaxWidth(Double.MAX_VALUE);
        header.setPadding(new Insets(12, 14, 10, 14));
        header.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

        missionList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(missionList, Priority.ALWAYS);

        VBox panel = new VBox(0, header, missionList);
        panel.setPrefWidth(230);
        panel.setStyle("-fx-background-color: " + PANEL + "; -fx-border-color: " + BORDER + "; -fx-border-width: 0 1 0 0;");
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
        addRow(curse, 0, "Название:",       lblCurseName);
        addRow(curse, 1, "Уровень угрозы:", lblCurseThreat);

        lblNote.setWrapText(true);

        VBox detail = new VBox(12,
                card("ОБЩАЯ ИНФОРМАЦИЯ",    general),
                card("ПРОКЛЯТИЕ",           curse),
                card("УЧАСТНИКИ ОПЕРАЦИИ",  sorcTable),
                card("ПРИМЕНЁННЫЕ ТЕХНИКИ", techTable),
                card("ПРИМЕЧАНИЕ",          lblNote));
        detail.setPadding(new Insets(20));
        detail.setStyle("-fx-background-color: " + BG + ";");

        ScrollPane scroll = new ScrollPane(detail);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    private VBox card(String title, Node content) {
        Label h = new Label(title);
        h.setFont(Font.font("System", FontWeight.BOLD, 10));
        h.setTextFill(Color.web(ACCENT));

        HBox header = new HBox(h);
        header.setPadding(new Insets(9, 16, 9, 16));
        header.setStyle("-fx-background-color: #12213a;" +
                        "-fx-background-radius: 7 7 0 0;" +
                        "-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

        VBox body = new VBox(content);
        body.setPadding(new Insets(12, 16, 14, 16));

        VBox box = new VBox(0, header, body);
        box.setStyle("-fx-background-color: " + CARD + ";" +
                     "-fx-background-radius: 7; -fx-border-radius: 7;" +
                     "-fx-border-color: " + BORDER + "; -fx-border-width: 1;");
        return box;
    }

    private GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(16); g.setVgap(10);
        g.setStyle("-fx-background-color: transparent;");
        return g;
    }

    private void addRow(GridPane g, int row, String key, Label val) {
        Label k = new Label(key);
        k.setTextFill(Color.web(MUTED));
        k.setFont(Font.font("System", 12));
        k.setMinWidth(110);
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
        table.setStyle("-fx-background-color: transparent; -fx-table-cell-border-color: " + BORDER + ";");
        table.setPrefHeight(150);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label(""));
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
        lblOutcome.setTextFill(Color.web("SUCCESS".equalsIgnoreCase(m.getOutcome()) ? SUCCESS : FAIL));

        if (m.getCurse() != null) {
            lblCurseName.setText(m.getCurse().getName());
            String t = m.getCurse().getThreatLevel();
            lblCurseThreat.setText(t);
            lblCurseThreat.setTextFill(switch (t.toUpperCase()) {
                case "SPECIAL_GRADE" -> Color.web(FAIL);
                case "HIGH"          -> Color.web("#f6ad55");
                case "MEDIUM"        -> Color.web("#faf089");
                default              -> Color.web(TEXT);
            });
        }

        sorcTable.setItems(FXCollections.observableArrayList(
                m.getSorcerers() != null ? m.getSorcerers() : List.of()));
        techTable.setItems(FXCollections.observableArrayList(
                m.getTechniques() != null ? m.getTechniques() : List.of()));
    }

    private static Label lbl(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web(TEXT));
        l.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        return l;
    }

    private static class MissionCell extends ListCell<Mission> {
        @Override
        protected void updateItem(Mission item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setGraphic(null); setStyle("-fx-background-color: transparent; -fx-padding: 0;"); return; }

            boolean sel = isSelected();
            Label id = new Label(item.getMissionId());
            id.setFont(Font.font("System", FontWeight.BOLD, 12));
            id.setTextFill(Color.web(sel ? ACCENT : TEXT));

            Label loc = new Label(item.getLocation());
            loc.setFont(Font.font("System", 11));
            loc.setTextFill(Color.web(MUTED));

            Label out = new Label(item.getOutcome());
            out.setFont(Font.font("System", FontWeight.BOLD, 10));
            out.setTextFill(Color.web("SUCCESS".equalsIgnoreCase(item.getOutcome()) ? SUCCESS : FAIL));

            VBox box = new VBox(3, id, loc, out);
            box.setPadding(new Insets(9, 14, 9, sel ? 12 : 14));
            if (sel) box.setStyle("-fx-border-color: " + ACCENT + "; -fx-border-width: 0 0 0 2;");

            setGraphic(box);
            setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        }
    }
}
