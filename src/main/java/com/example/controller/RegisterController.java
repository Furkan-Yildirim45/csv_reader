package com.example.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.models.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private PrimaryController primaryController; // PrimaryController referansı

    // PrimaryController referansını ayarlamak için bir setter
    public void setPrimaryController(PrimaryController primaryController) {
        this.primaryController = primaryController;
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
    
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "All fields are required!");
            return;
        }
    
        // Şifreyi hashleme (basit bir örnek, gerçek bir hashleme algoritması kullanmalısınız)
        String passwordHash = Integer.toHexString(password.hashCode());
    
        // Yeni kullanıcı oluşturma
        User newUser = new User(
            new org.bson.types.ObjectId(), // Benzersiz bir ObjectId oluşturulur
            username,
            email,
            passwordHash,
            LocalDateTime.now(), // Kullanıcının oluşturulma zamanı
            List.of() // Varsayılan olarak boş bir CSV dosya listesi
        );
    
        // PrimaryController'ı bilgilendirme
        if (primaryController != null) {
            primaryController.updateUserInfo(newUser);
        }
    
        showAlert("Success", "User registered successfully!");
        closeWindow();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

