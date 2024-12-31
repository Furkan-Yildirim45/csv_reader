package com.example.controller;

import com.example.models.User;
import com.example.service.CsvService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;

public class PrimaryController {

    @FXML
    private Button uploadButton;

    @FXML
    private TableView<String[]> tableView;

    @FXML
    private TableView<User> mongoTableView;

    @FXML
    private TableColumn<User, String> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label userInfoLabel; // Kullanıcı bilgilerini gösterecek Label

    private boolean isLoggedIn = false;

    private final CsvService csvService = new CsvService();

    public void initialize() {
        // CSV Yükleme Butonu
        uploadButton.setOnAction(event -> loadCSVFile());
        addButton.setOnAction(event -> showLoginPopup());
        userInfoLabel.setText("Uknown User");
        registerButton.setOnAction(event -> showRegisterPopup());

        // MongoDB Tablosu Ayarları
        if (idColumn != null && nameColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        }

    }

    private void loadCSVFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            csvService.processFile(file, tableView);
            // tableView'ı görünür yapın
            if (tableView != null) {
                tableView.setVisible(true);
            }
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
}
