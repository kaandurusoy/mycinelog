package com.mycinelog;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

// JavaFX tabanli, daha modern MyCineLog arayuzu.
public class MyCineLogFXApp extends Application {
    private final ObservableList<FilmRecord> filmler = FXCollections.observableArrayList();
    private final FilmStore store = new FilmStore(Path.of("filmler.txt"));
    private final TilePane kartAlani = new TilePane();

    private final TextField araField = new TextField();
    private final TextField adField = new TextField();
    private final TextField puanField = new TextField();
    private final TextField oyField = new TextField();
    private final TextField turField = new TextField();
    private final TextField tarihField = new TextField();

    @Override
    public void start(Stage stage) {
        filmler.setAll(store.yukle());

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        root.setTop(ustBar());
        root.setLeft(solPanel());
        root.setCenter(ortaIcerik());

        Scene scene = new Scene(root, 1200, 760);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("MyCineLog FX");
        stage.setScene(scene);
        stage.show();

        kartlariYenile(filmler);
    }

    // Ustte baslik + arama alani.
    private VBox ustBar() {
        Label baslik = new Label("MyCineLog");
        baslik.getStyleClass().add("title");

        araField.setPromptText("Film ara...");
        araField.getStyleClass().add("input");
        araField.textProperty().addListener((obs, oldVal, newVal) -> filtrele());

        VBox box = new VBox(12, baslik, araField);
        box.setPadding(new Insets(20));
        return box;
    }

    // Solda form alanlari ve kayit butonu.
    private VBox solPanel() {
        adField.setPromptText("Film Adi");
        puanField.setPromptText("IMDb Puan");
        oyField.setPromptText("IMDb Oy");
        turField.setPromptText("Tur");
        tarihField.setPromptText("Izlenme Tarihi (dd.MM.yyyy)");
        tarihField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        adField.getStyleClass().add("input");
        puanField.getStyleClass().add("input");
        oyField.getStyleClass().add("input");
        turField.getStyleClass().add("input");
        tarihField.getStyleClass().add("input");

        Button ekleBtn = new Button("Filmi Ekle");
        ekleBtn.getStyleClass().add("primary-btn");
        ekleBtn.setMaxWidth(Double.MAX_VALUE);
        ekleBtn.setOnAction(e -> filmEkle());

        VBox panel = new VBox(10,
            new Label("Yeni Film"),
            adField, puanField, oyField, turField, tarihField, ekleBtn
        );
        panel.getStyleClass().add("left-panel");
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(300);
        return panel;
    }

    // Ortada kaydirilabilir film kart alani.
    private ScrollPane ortaIcerik() {
        kartAlani.setHgap(16);
        kartAlani.setVgap(16);
        kartAlani.setPadding(new Insets(20));
        kartAlani.setPrefColumns(4);

        ScrollPane sp = new ScrollPane(kartAlani);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("cards-scroll");
        return sp;
    }

    private void filmEkle() {
        try {
            String ad = adField.getText().trim();
            String tur = turField.getText().trim();
            String tarih = tarihField.getText().trim();
            double puan = Double.parseDouble(puanField.getText().trim());
            long oy = Long.parseLong(oyField.getText().trim().replace(".", "").replace(",", ""));

            if (ad.isEmpty() || tur.isEmpty() || tarih.isEmpty()) {
                throw new IllegalArgumentException("Tum alanlar dolu olmali.");
            }
            if (puan < 0 || puan > 10) {
                throw new IllegalArgumentException("Puan 0-10 arasi olmali.");
            }

            filmler.add(new FilmRecord(ad, puan, oy, tur, tarih));
            store.kaydet(filmler);
            temizle();
            filtrele();
        } catch (Exception ex) {
            mesaj("Hata", ex.getMessage());
        }
    }

    private void temizle() {
        adField.clear();
        puanField.clear();
        oyField.clear();
        turField.clear();
        tarihField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }

    private void filtrele() {
        String q = araField.getText().trim().toLowerCase();
        List<FilmRecord> sonuc = filmler.stream()
            .filter(f -> f.getAd().toLowerCase().contains(q) || f.getTur().toLowerCase().contains(q))
            .collect(Collectors.toList());
        kartlariYenile(sonuc);
    }

    // Listeyi kart komponentlerine cevirip ekranda gosterir.
    private void kartlariYenile(List<FilmRecord> liste) {
        kartAlani.getChildren().clear();
        for (FilmRecord f : liste) {
            VBox card = new VBox(8);
            card.getStyleClass().add("film-card");
            card.setPrefWidth(220);

            Label ad = new Label(f.getAd());
            ad.getStyleClass().add("film-name");
            ad.setWrapText(true);

            Label tur = new Label("Tur: " + f.getTur());
            Label puan = new Label("IMDb: " + f.getPuan() + " / 10");
            Label oy = new Label("Oy: " + formatOy(f.getOy()));
            Label tarih = new Label("Izlenme: " + f.getIzlenmeTarihi());

            HBox alt = new HBox(8, puan, oy);
            HBox.setHgrow(puan, Priority.ALWAYS);
            alt.setAlignment(Pos.CENTER_LEFT);

            card.getChildren().addAll(ad, tur, alt, tarih);
            kartAlani.getChildren().add(card);
        }
    }

    private String formatOy(long oy) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("tr", "TR"));
        symbols.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,###", symbols);
        return format.format(oy);
    }

    private void mesaj(String baslik, String icerik) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("MyCineLog");
        alert.setHeaderText(baslik);
        alert.setContentText(icerik);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
