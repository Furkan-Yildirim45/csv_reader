package com.example.controller;

import com.example.service.DataBaseService;
import com.example.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField epostaField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private PrimaryController primaryController;

    public void setPrimaryController(PrimaryController controller) {
        this.primaryController = controller;
    }

    @FXML
    public void initialize() {
        loginButton.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String ePosta = epostaField.getText();
        String password = passwordField.getText();
    
        if (ePosta.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password cannot be empty!");
            errorLabel.setVisible(true);
            return;
        }
    
        // MongoDB üzerinden kullanıcı doğrulama
        User authenticatedUser = DataBaseService.getInstance().loginUser(ePosta, password);
    
        if (authenticatedUser != null) {
            // Kullanıcı başarıyla doğrulandı, bilgileri güncelle
            System.out.println("User authenticated: " + authenticatedUser.getUsername());
    
            if (primaryController != null) {
                primaryController.updateUserInfo(authenticatedUser);
            }
    
            // Login başarılıysa popup'ı kapat
            loginButton.getScene().getWindow().hide();
        } else {
            // Eğer kullanıcı doğrulanamazsa hata mesajı göster
            errorLabel.setText("Invalid credentials!");
            errorLabel.setVisible(true);
        }
    }
    
}
