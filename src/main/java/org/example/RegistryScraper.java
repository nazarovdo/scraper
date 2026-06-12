package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RegistryScraper {

    private final HttpHandler httpHandler;
    private final ObjectMapper objectMapper;

    public RegistryScraper() {
        this.httpHandler = new HttpHandler();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void scrapeAllData(String fromDate, String toDate) throws IOException {
        int pageNum = 1;
        int totalPages = Integer.MAX_VALUE;
        int pageSize = 1000;

        String registryDirName = "epbs-registry-entries";
        Files.createDirectories(Paths.get(registryDirName));
        String zipFilePath = String.format("%s/epbs_%s_%s.zip", registryDirName, fromDate, toDate);

        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            while (pageNum <= totalPages) {
                try {
                    String jsonResponse = httpHandler.fetchRegistryEntries(fromDate, toDate, pageNum, pageSize);
                    JsonNode rootNode = objectMapper.readTree(jsonResponse);

                    if (rootNode == null || rootNode.isEmpty()) {
                        System.out.println("Получен пустой ответ от сервера. Прерываю работу");
                        break;
                    }

                    if (pageNum == 1) {
                        if (rootNode.has("pageCount")) {
                            totalPages = rootNode.get("pageCount").asInt();
                            int totalRecords = rootNode.has("recordCount") ? rootNode.get("recordCount").asInt() : 0;
                            System.out.println("Всего записей в реестре c " + fromDate + " по " + toDate + ": " + totalRecords);
                        } else {
                            System.out.println("Ключ pageCount не найден");
                        }
                    }

                    System.out.printf("Получение страницы %d из %d\n", pageNum, totalPages);

                    String fileName = String.format("epbs_%s_%s_page_%d.json", fromDate, toDate, pageNum);
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zos.putNextEntry(zipEntry);
                    String prettyJson = objectMapper.writeValueAsString(rootNode);
                    byte[] bytes = prettyJson.getBytes(StandardCharsets.UTF_8);
                    zos.write(bytes, 0, bytes.length);
                    zos.closeEntry();

                    pageNum++;
                    Thread.sleep(100);

                } catch (Exception e) {
                    System.err.println("\nОшибка: Сбой на странице " + pageNum + ": " + e.getMessage());
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка: " + e.getMessage());
            throw e;
        }

        System.out.println("Сканирование успешно завершено. Все страницы сохранены в " + zipFilePath);
    }
}