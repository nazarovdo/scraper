package org.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ScraperApp {
    public static void main(String[] args) {

        String lastUpdateFromStr = null;
        String lastUpdateToStr = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--") && i + 1 < args.length) {
                String flag = args[i];
                String value = args[i + 1];

                switch (flag) {
                    case "--lastUpdateFrom":
                        lastUpdateFromStr = value;
                        break;
                    case "--lastUpdateTo":
                        lastUpdateToStr = value;
                        break;
                    default:
                        System.out.println("Неизвестный формат аргумента: " + flag);
                        break;
                }
                i++;
            }
        }
        if (lastUpdateFromStr == null || lastUpdateToStr == null) {
            System.out.println("Ошибка: Отсутствуют обязательные аргументы");
            return;
        }

        System.out.println("Сборщик данных инициализирован");
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate.parse(lastUpdateFromStr, formatter);
            LocalDate.parse(lastUpdateToStr, formatter);
            System.out.println("Даты корректны");

            RegistryScraper registryScraper = new RegistryScraper();
            registryScraper.scrapeAllData(lastUpdateFromStr, lastUpdateToStr);

        } catch (java.time.format.DateTimeParseException e) {
            System.err.println("Ошибка: Неверный формат даты или дата не существует! Используйте строго DD.MM.YYYY (например, 15.01.2026)");
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

    }
}