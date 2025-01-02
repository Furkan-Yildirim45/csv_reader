package com.example.controller;

import com.example.models.User;
import com.example.service.CsvService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PrimaryController {

    @FXML
    private Button uploadButton;

    @FXML
    private TableView<String[]> tableView;

    @FXML
    private TableView<User> mongoTableView;

    @FXML
    private Button addButton;
    @FXML
    private Button addRowButton;

    @FXML
    private Button registerButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button logoutButton;

    @FXML
    private Label userInfoLabel; // Kullanıcı bilgilerini gösterecek Label

    private boolean isLoggedIn = false;
    List<String> headers;
    private final CsvService csvService = new CsvService();

    private Stage popupStage;
    private AddRowPopupController popupController;

    public void initialize() {
        // CSV Yükleme Butonu
        uploadButton.setOnAction(event -> loadCSVFile());
        addButton.setOnAction(event -> showLoginPopup());
        userInfoLabel.setText("Uknown User");
        registerButton.setOnAction(event -> showRegisterPopup());
        addRowButton.setOnAction(event -> openAddRowPopup());
        saveButton.setOnAction(event -> saveCSVFile());
        logoutButton.setOnAction(event -> handleLogout());
    }

    private void loadCSVFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            headers = csvService.getCsvHeaders(file);

            csvService.processFile(file, tableView); // Verileri ekle

            tableView.setVisible(true); // TableView'i görünür yap
            saveButton.setVisible(true); // Kaydet butonunu görünür yap
            addRowButton.setVisible(true);
        }
    }

    private void showLoginPopup() {
        if (!isLoggedIn) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/login.fxml"));
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL); // Popup ana pencereyi engeller
                stage.setScene(new Scene(loader.load()));
                LoginController controller = loader.getController();
                controller.setPrimaryController(this);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showRegisterPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/register.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Popup ana pencereyi engeller
            stage.setScene(new Scene(loader.load()));

            // RegisterController'ı al ve PrimaryController'ı ayarla
            RegisterController controller = loader.getController();
            controller.setPrimaryController(this);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateUserInfo(User user) {
        if (user != null) {
            // Kullanıcı bilgilerini ekranda göster
            userInfoLabel.setText("Welcome: " + user.getUsername());
        } else {
            // Kullanıcı bilgisi boşsa etiketleri temizle
            userInfoLabel.setText("No user logged in");
        }
    }

    // AddRowPopup'u açan metot
    private void openAddRowPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/addRowPopup.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL); // Popup ana pencereyi engeller
            stage.setScene(new Scene(loader.load()));

            // RegisterController'ı al ve PrimaryController'ı ayarla
            AddRowPopupController controller = loader.getController();
            controller.setPopupStage(stage);
            if (headers != null) {
                controller.setParentController(this, headers);
            } else {
                System.out.println("headers is null");
            }

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addRowToTableView(String[] newRow) {
        tableView.getItems().add(newRow);
    }

    private void saveCSVFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        // Kullanıcının dosya kaydetmek için seçtiği yer
        File file = fileChooser.showSaveDialog(saveButton.getScene().getWindow());
        if (file != null) {
            try {
                // TableView'daki verileri alın
                List<String[]> data = tableView.getItems();

                // CSV'yi kaydedin
                csvService.saveToCsv(file, headers, data);

                // Kaydetme işlemi başarılı olduğunda bir mesaj göster
                showSaveSuccessMessage(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();

                // Hata durumunda bir hata mesajı göster
                showErrorMessage("CSV dosyası kaydedilemedi.");
            }
        }
    }

    private void showSaveSuccessMessage(String filePath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Başarılı");
        alert.setHeaderText("Kaydetme Tamamlandı");
        alert.setContentText("CSV dosyası başarıyla kaydedildi: \n" + filePath);
        alert.showAndWait(); // Kullanıcının mesajı kapatmasını bekler
    }

    private void showErrorMessage(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Hata");
        alert.setHeaderText("Kaydetme Başarısız");
        alert.setContentText(errorMessage);
        alert.showAndWait(); // Kullanıcının mesajı kapatmasını bekler
    }

    private void handleLogout() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Çıkış Yap");
    alert.setHeaderText("Çıkış yapmak istediğinizden emin misiniz?");
    alert.setContentText("Lütfen bir seçim yapın:");

    ButtonType buttonYes = new ButtonType("Evet");
    ButtonType buttonNo = new ButtonType("Hayır");
    alert.getButtonTypes().setAll(buttonYes, buttonNo);

    // Kullanıcı seçim işlemi
    alert.showAndWait().ifPresent(response -> {
        if (response == buttonYes) {
            performLogout();
        }
    });
}

private void performLogout() {
    // Çıkış yapma işlemleri burada gerçekleşir.
    userInfoLabel.setText("No user logged in");
    isLoggedIn = false;
    logoutButton.setVisible(false); // Logout butonunu gizle
    addButton.setVisible(true);     // Login butonunu tekrar görünür yap
}

    public void setVisibleLogoutButton(){
        logoutButton.setVisible(true);
    }
    public void setVisibleLoginButton(){
        addButton.setVisible(false);
    }


}
