package com.example.service;

import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Callback;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvService {

    private static final long SMALL_FILE_SIZE = 10 * 1024; // 10 KB
    private static final long MEDIUM_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int NUM_THREADS = 4; // Paralel işlem için iş parçacığı sayısı

    // Ana dosya işleme metodu
    public void processFile(File file, TableView<String[]> tableView) {
        try {
            long fileSize = Files.size(file.toPath());
            long startTime = System.nanoTime();

            if (fileSize <= SMALL_FILE_SIZE) {
                readAndDisplaySmallCSV(file, tableView); // Küçük dosya işlemi
            } else if (fileSize <= MEDIUM_FILE_SIZE) {
                readAndDisplayBufferedCSV(file, tableView); // Orta boy dosya işlemi
            } else {
                readAndDisplayLargeCSVParallel(file, tableView); // Büyük dosya paralel işlem
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            System.out.println("File processed in " + duration + " ms");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Küçük dosyalar için CSV okuma ve gösterme metodu
    private void readAndDisplaySmallCSV(File file, TableView<String[]> tableView) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            displayCSVContent(br.lines(), tableView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Orta boy dosyalar için BufferedReader ile CSV okuma ve gösterme metodu
    private void readAndDisplayBufferedCSV(File file, TableView<String[]> tableView) {
        try (BufferedReader br = new BufferedReader(new FileReader(file), 8192)) {
            displayCSVContent(br.lines(), tableView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Büyük dosyalar için paralel CSV okuma ve gösterme metodu
    private void readAndDisplayLargeCSVParallel(File file, TableView<String[]> tableView) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        try (Stream<String> lines = Files.lines(file.toPath())) {
            List<Future<Void>> futures = new ArrayList<>();
            List<String> allLines = lines.collect(Collectors.toList());
            int chunkSize = allLines.size() / NUM_THREADS;

            for (int i = 0; i < NUM_THREADS; i++) {
                final int start = i * chunkSize;
                final int end = (i == NUM_THREADS - 1) ? allLines.size() : (i + 1) * chunkSize;

                futures.add(executor.submit(() -> {
                    processLinesInChunk(allLines.subList(start, end), tableView);
                    return null;
                }));
            }

            for (Future<Void> future : futures) {
                future.get();
            }

            executor.shutdown();

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Belirli bir parça içindeki satırları işleyen metot
    private void processLinesInChunk(List<String> lines, TableView<String[]> tableView) {
        boolean[] isHeader = { true };
        for (String line : lines) {
            String[] values = line.split(",");
            if (isHeader[0]) {
                setupTableColumns(values, tableView);
                isHeader[0] = false;
            } else {
                tableView.getItems().add(values);
            }
        }
    }

    // CSV içeriğini TableView'de gösterir
    private void displayCSVContent(Stream<String> lines, TableView<String[]> tableView) {
        boolean[] isHeader = { true };
        for (String line : (Iterable<String>) lines::iterator) {
            String[] values = line.split(",");
            if (isHeader[0]) {
                setupTableColumns(values, tableView);
                isHeader[0] = false;
            } else {
                tableView.getItems().add(values);
            }
        }
    }

    // TableView için sütunları ayarlayan metot
    private void setupTableColumns(String[] headers, TableView<String[]> tableView) {
        tableView.getColumns().clear();

        for (String header : headers) {
            final int columnIndex = tableView.getColumns().size();
            TableColumn<String[], String> column = new TableColumn<>(header);

            column.setCellValueFactory(cellData -> {
                String[] row = cellData.getValue();
                return columnIndex < row.length ? new SimpleStringProperty(row[columnIndex]) : null;
            });

            column.setCellFactory(new Callback<TableColumn<String[], String>, TableCell<String[], String>>() {
                @Override
                public TableCell<String[], String> call(TableColumn<String[], String> param) {
                    return new TableCell<String[], String>() {
                        private final TextField textField = new TextField();

                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                setText(item);
                                setGraphic(textField);
                                textField.setText(item);

                                textField.setOnAction(event -> {
                                    String newValue = textField.getText();
                                    String[] row = getTableRow().getItem();
                                    row[columnIndex] = newValue;
                                    commitEdit(newValue);
                                });
                            }
                        }
                    };
                }
            });

            tableView.getColumns().add(column);
        }
    }

    // Yeni: Başlıkları döndüren metot
    public List<String> getCsvHeaders(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String headerLine = br.readLine();
            if (headerLine != null) {
                return Arrays.asList(headerLine.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveToCsv(File file, List<String> headers, List<String[]> data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Header'ları yaz
            writer.write(String.join(",", headers));
            writer.newLine();
    
            // Verileri yaz
            for (String[] row : data) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
    }
    

}
