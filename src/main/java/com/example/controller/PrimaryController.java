package com.example.controller;

import com.example.service.CsvService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;

public class PrimaryController {

    @FXML
    private Button uploadButton;

    @FXML
    private TableView<String[]> tableView;

    private final CsvService csvService = new CsvService();

    public void initialize() {
        uploadButton.setOnAction(event -> loadCSVFile());
    }

    private void loadCSVFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            csvService.processFile(file, tableView);
        }
    }
}
