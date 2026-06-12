package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;

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

        String registryDirName = "epbs-registry-entries";
        Files.createDirectories(Paths.get(registryDirName));

        while (pageNum <= totalPages) {
            try {
                int pageSize = 1000;
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

                String fileName = String.format(registryDirName + "/epbs_%s_%s_page_%d.json", fromDate, toDate, pageNum);
                File outputFile = new File(fileName);
                objectMapper.writeValue(outputFile, rootNode);
                System.out.println("Сохранено в: " + registryDirName + "/" + outputFile.getName());

                pageNum++;
                Thread.sleep(100);

            } catch (Exception e) {
                System.err.println("\nОшибка: Сбой на странице " + pageNum + ": " + e.getMessage());
                break;
            }
        }

        System.out.println("Сканирование успешно завершено. Все страницы сохранены в каталоге " + registryDirName);
    }
}