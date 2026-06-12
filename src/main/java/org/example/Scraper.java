package org.example;

public class Scraper {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Error: target URL is not specified");
            return;
        }
        String targetUrl = args[0];
        System.out.println("Data scraper initialized");
        System.out.println("Target URL is: " + targetUrl);

        HttpHandler httpHandler = new HttpHandler();

        try {
            String jsonResponse = httpHandler.sendGetRequest(targetUrl);

            System.out.println("JSON fetched");
            System.out.println(jsonResponse);

            // todo: string to object

        } catch (Exception e) {
            System.err.println("API error: " + e.getMessage());
        }

    }
}