package com.example.controller;

import com.example.models.CsvFile;
import com.example.models.User;
import com.example.service.CsvService;
import com.example.service.DataBaseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private final CsvService csvService = new CsvService();
    private final DataBaseService dbService = DataBaseService.getInstance();

    private final ObservableList<User> userData = FXCollections.observableArrayList();

    public void initialize() {
        // CSV Yükleme Butonu
        uploadButton.setOnAction(event -> loadCSVFile());
    
        // MongoDB Tablosu Ayarları
        if (idColumn != null && nameColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        }
    
        // MongoDB Kullanıcıları Yükle
        // loadUsers();
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
            if (mongoTableView != null) {
                mongoTableView.setVisible(false); // MongoDB tablosunu gizle
            }
        }
    }
    

    private void loadUsers() {
        userData.clear();
        for (Document doc : dbService.getAllUsers()) {
            // Document'ten User nesnesi oluştur
            User user = new User(
                    doc.getObjectId("_id"), // _id
                    doc.getString("username"), // username
                    doc.getString("email"), // email
                    doc.getString("passwordHash"), // passwordHash
                    LocalDateTime.parse(doc.getString("createdAt")), // createdAt (String'den LocalDateTime'a
                                                                     // dönüştürülüyor)
                    doc.getList("csvFiles", ObjectId.class) // csvFiles
            );
            userData.add(user);
        }
        mongoTableView.setItems(userData);
    }

    private void addUser() {
        try {
            // Yeni kullanıcı oluştur
            User newUser = new User(
                    new ObjectId(), // id
                    "Yeni Kullanıcı", // username
                    "yeni@kullanici.com", // email
                    "hashed_password", // passwordHash
                    LocalDateTime.now(), // createdAt
                    new ArrayList<>() // csvFiles
            );

            // User'ı Document'e dönüştür
            Document userDoc = new Document()
                    .append("username", newUser.getUsername())
                    .append("email", newUser.getEmail())
                    .append("passwordHash", newUser.getPasswordHash())
                    .append("createdAt", newUser.getCreatedAt().toString()) // LocalDateTime'ı String'e dönüştür
                    .append("csvFiles", newUser.getCsvFiles());

            // Veritabanına kullanıcı ekle
            dbService.addUser(userDoc);

            // Tabloyu güncelle
            loadUsers();

            // Başarı mesajı göster
            showAlert("Success", "User added successfully!");
        } catch (Exception ex) {
            // Hata mesajı göster
            showAlert("Error", "Error adding user: " + ex.getMessage());
        }
    }

    // CSV dosyasını MongoDB'ye ekleyen metod
    private void addCsvFileToDatabase(String fileName, List<String[]> content) {
        CsvFile csvFile = new CsvFile(fileName, content);
        dbService.addCsvFile(csvFile.toDocument());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
