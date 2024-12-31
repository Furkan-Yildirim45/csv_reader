package com.example.service;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvService {

    // Küçük, orta ve büyük dosya boyutlarını tanımlayan sabitler
    private static final long SMALL_FILE_SIZE = 10 * 1024; // 10 KB
    private static final long MEDIUM_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final int NUM_THREADS = 4; // Paralel işlem için iş parçacığı sayısı

    // Ana dosya işleme metodu
    public void processFile(File file, TableView<String[]> tableView) {
        try {
            // Dosya boyutunu hesaplar
            long fileSize = Files.size(file.toPath());

            // İşlem süresini ölçmek için başlangıç zamanı
            long startTime = System.nanoTime();

            // Dosya boyutuna göre işleme yöntemi seçimi
            if (fileSize <= SMALL_FILE_SIZE) {
                readAndDisplaySmallCSV(file, tableView); // Küçük dosya işlemi
            } else if (fileSize <= MEDIUM_FILE_SIZE) {
                readAndDisplayBufferedCSV(file, tableView); // Orta boy dosya işlemi
            } else {
                readAndDisplayLargeCSVParallel(file, tableView); // Büyük dosya paralel işlem
            }

            // İşlem süresini ölçmek için bitiş zamanı ve süreyi hesaplama
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000;
            System.out.println("File processed in " + duration + " ms");

        } catch (IOException e) {
            e.printStackTrace(); // Hata durumunda istisna yakalama
        }
    }

    // Küçük dosyalar için CSV okuma ve gösterme metodu
    private void readAndDisplaySmallCSV(File file, TableView<String[]> tableView) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            displayCSVContent(br.lines(), tableView, "Small File"); // Satırları TableView'de gösterir
        } catch (IOException e) {
            e.printStackTrace(); // Hata durumunda istisna yakalama
        }
    }

    // Orta boy dosyalar için BufferedReader ile CSV okuma ve gösterme metodu
    private void readAndDisplayBufferedCSV(File file, TableView<String[]> tableView) {
        try (BufferedReader br = new BufferedReader(new FileReader(file), 8192)) { // 8 KB buffer kullanımı
            displayCSVContent(br.lines(), tableView, "Buffered File");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Büyük dosyalar için paralel CSV okuma ve gösterme metodu
    private void readAndDisplayLargeCSVParallel(File file, TableView<String[]> tableView) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS); // İş parçacıkları havuzu oluşturma
        try (Stream<String> lines = Files.lines(file.toPath())) { // Dosyayı satır satır okuma
            List<Future<Void>> futures = new ArrayList<>();

            // Tüm satırları listeye al
            List<String> allLines = lines.collect(Collectors.toList());
            int chunkSize = allLines.size() / NUM_THREADS; // İş parçacıkları için parça boyutu

            // Her iş parçacığı için bir görev oluştur
            for (int i = 0; i < NUM_THREADS; i++) {
                final int start = i * chunkSize; // Başlangıç indeksi
                final int end = (i == NUM_THREADS - 1) ? allLines.size() : (i + 1) * chunkSize; // Bitiş indeksi

                // Görevi iş parçacığı havuzuna ekle
                futures.add(executor.submit(() -> {
                    processLinesInChunk(allLines.subList(start, end), tableView);
                    return null;
                }));
            }

            // Tüm görevlerin tamamlanmasını bekle
            for (Future<Void> future : futures) {
                future.get();
            }

            executor.shutdown(); // İş parçacığı havuzunu kapat

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Hata durumunda istisna yakalama
        }
    }

    // Belirli bir parça içindeki satırları işleyen metot
    private void processLinesInChunk(List<String> lines, TableView<String[]> tableView) {
        boolean[] isHeader = {true}; // İlk satırın başlık olup olmadığını takip eder
        for (String line : lines) {
            String[] values = line.split(","); // Satırı virgülle ayırır
            if (isHeader[0]) {
                setupTableColumns(values, tableView); // Başlık için sütunları oluşturur
                isHeader[0] = false;
            } else {
                tableView.getItems().add(values); // Veriyi tabloya ekler
            }
        }
    }

    // CSV içeriğini TableView'de gösterir
    private void displayCSVContent(Stream<String> lines, TableView<String[]> tableView, String fileType) {
        boolean[] isHeader = {true}; // İlk satır başlık mı kontrolü
        for (String line : (Iterable<String>) lines::iterator) {
            String[] values = line.split(","); // Satır verilerini virgülle ayırma
            if (isHeader[0]) {
                setupTableColumns(values, tableView); // Başlık için sütunları ayarla
                isHeader[0] = false;
            } else {
                tableView.getItems().add(values); // Veriyi tabloya ekle
            }
        }
    }

    // TableView için sütunları ayarlayan metot
    private void setupTableColumns(String[] headers, TableView<String[]> tableView) {
        tableView.getColumns().clear(); // Mevcut sütunları temizle

        for (String header : headers) {
            TableColumn<String[], String> column = new TableColumn<>(header); // Yeni bir sütun oluştur
            final int columnIndex = tableView.getColumns().size(); // Sütun indeksi belirle

            // Sütun hücre değeri için veri kaynağını ayarla
            column.setCellValueFactory(cellData -> {
                String[] row = cellData.getValue();
                return columnIndex < row.length ? new SimpleStringProperty(row[columnIndex]) : null;
            });

            tableView.getColumns().add(column); // Sütunu tabloya ekle
        }
    }
}
