package org.example;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import java.io.IOException;

public class HttpHandler {

    public String fetchRegistryEntries(String lastUpdateFrom,
                                       String lastUpdateTo,
                                       Integer pageNum,
                                       Integer pageSize
    ) throws IOException {
        String targetUrl = "https://budget.gov.ru/epbs/registry/ubpandnubp/data?" +
                "filterminloaddate=" + lastUpdateFrom +
                "&filtermaxloaddate=" + lastUpdateTo +
                "&pageSize=" + pageSize.toString() +
                "&pageNum=" + pageNum.toString();

        return sendGetRequest(targetUrl);
    }

    public String sendGetRequest(String url) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            HttpClientResponseHandler<String> responseHandler = response -> {
                int statusCode = response.getCode();
                if (statusCode != 200) {
                    throw new IOException("Ошибка сервера: " + statusCode);
                }
                if (response.getEntity() != null) {
                    return EntityUtils.toString(response.getEntity(), "UTF-8");
                }
                return "";
            };

            return httpClient.execute(request, responseHandler);
        }
    }
}
