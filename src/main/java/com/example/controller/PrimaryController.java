package com.example.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrimaryController {

    @FXML
    private Button uploadButton;

    @FXML
    private TableView<String[]> tableView;

    private static final long SMALL_FILE_SIZE = 10 * 1024; // 10 KB
    private static final long MEDIUM_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int NUM_THREADS = 4; // Çoklu iş parçacığı sayısı

    public void initialize() {
        uploadButton.setOnAction(event -> loadCSVFile());
    }

    private void loadCSVFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());
        if (file != null) {
            determineAndProcessFile(file);
        }
    }

    private void determineAndProcessFile(File file) {
        try {
            long fileSize = Files.size(file.toPath());

            long startTime = System.nanoTime(); // Performans ölçümünün başlangıcı

            if (fileSize <= SMALL_FILE_SIZE) {
                // Küçük dosyalar için basit okuma
                System.out.println("Using Simple File Reader (Small File)");
                readAndDisplaySmallCSV(file);
            } else if (fileSize <= MEDIUM_FILE_SIZE) {
                // Orta boyutlu dosyalar için tamponlu okuma
                System.out.println("Using Buffered Reader (Medium File)");
                readAndDisplayBufferedCSV(file);
            } else {
                // Büyük dosyalar için paralel işlem
                System.out.println("Using Parallel Processing (Large File)");
                readAndDisplayLargeCSVParallel(file);
            }

            long endTime = System.nanoTime(); // Performans ölçümünün bitişi
            long duration = (endTime - startTime) / 1_000_000; // milisaniye cinsinden süre
            System.out.println("File processed in " + duration + " ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAndDisplaySmallCSV(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            displayCSVContent(br.lines(), "Small File");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAndDisplayBufferedCSV(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file), 8192)) { // 8 KB buffer
            displayCSVContent(br.lines(), "Buffered File");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAndDisplayLargeCSVParallel(File file) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        try (Stream<String> lines = Files.lines(file.toPath())) {
            List<Future<Void>> futures = new ArrayList<>();

            // Paralel işleme başlat
            final List<String> allLines = lines.collect(Collectors.toList());
            int chunkSize = allLines.size() / NUM_THREADS;
            for (int i = 0; i < NUM_THREADS; i++) {
                final int start = i * chunkSize;
                final int end = (i == NUM_THREADS - 1) ? allLines.size() : (i + 1) * chunkSize;

                futures.add(executor.submit(() -> {
                    processLinesInChunk(allLines.subList(start, end));
                    return null;
                }));
            }

            // Tüm iş parçacıklarının bitmesini bekleyin
            for (Future<Void> future : futures) {
                future.get();
            }

            executor.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void processLinesInChunk(List<String> lines) {
        boolean[] isHeader = {true};
        long lineNumber = 0;

        // Her satırı okurken loglama yapıyoruz
        for (String line : lines) {
            lineNumber++;

            // Her satırı işlediğimizde log yazalım
            System.out.println("Processing line " + lineNumber + ": " + line);

            String[] values = line.split(",");

            if (isHeader[0]) {
                setupTableColumns(values);
                isHeader[0] = false;
            } else {
                tableView.getItems().add(values);
            }

            // Eğer belirli bir satır sayısına geldiysek, işlem ilerlemesini gösterebiliriz
            if (lineNumber % 100 == 0) {
                System.out.println("Processed " + lineNumber + " lines so far...");
            }
        }
    }

    private void displayCSVContent(Stream<String> lines, String fileType) {
        // Lazy loading ile satırları okuyoruz
        boolean[] isHeader = {true};
        long lineNumber = 0;

        System.out.println("Processing " + fileType + " - Start reading lines:");

        for (String line : (Iterable<String>) lines::iterator) {
            lineNumber++;

            // Her satırı işlediğimizde log yazalım
            System.out.println("Processing line " + lineNumber + ": " + line);

            String[] values = line.split(",");

            if (isHeader[0]) {
                setupTableColumns(values);
                isHeader[0] = false;
            } else {
                tableView.getItems().add(values);
            }

            // Eğer belirli bir satır sayısına geldiysek, işlem ilerlemesini gösterebiliriz
            if (lineNumber % 100 == 0) {
                System.out.println("Processed " + lineNumber + " lines so far...");
            }
        }

        System.out.println("Finished processing " + fileType + " - Total lines: " + lineNumber);
    }

    private void setupTableColumns(String[] headers) {
        tableView.getColumns().clear();

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
