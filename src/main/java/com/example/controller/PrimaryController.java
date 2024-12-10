package com.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PrimaryController {

    @FXML
    private Button uploadButton;

    @FXML
    private TableView<String[]> tableView;

    public void initialize() {
        uploadButton.setOnAction(event -> loadCSVFile());
    }

    private void loadCSVFile() {
        // FileChooser ile CSV dosyasını seçme
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            readAndDisplayCSV(file);
        }
    }

    private void readAndDisplayCSV(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                if (isHeader) {
                    // Sütun başlıklarını ayarla
                    setupTableColumns(values);
                    isHeader = false;
                } else {
                    // Verileri tabloya ekle
                    tableView.getItems().add(values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTableColumns(String[] headers) {
        tableView.getColumns().clear(); // Eski sütunları temizle

        for (String header : headers) {
            TableColumn<String[], String> column = new TableColumn<>(header);
            final int columnIndex = tableView.getColumns().size();

            column.setCellValueFactory(cellData -> {
                String[] row = cellData.getValue();
                return columnIndex < row.length ? new SimpleStringProperty(row[columnIndex]) : null;
            });

            tableView.getColumns().add(column);
        }
    }
}
