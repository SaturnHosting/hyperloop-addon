package dev.saturn.hyperloop.util;

import dev.saturn.hyperloop.Hyperloop;
import dev.saturn.hyperloop.modules.HyperloopModule;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Utils {
    public static final String API_URL = Modules.get().get(HyperloopModule.class).host.get();
    public static final String API_KEY = Modules.get().get(HyperloopModule.class).apiKey.get();



    public static String fetchLoops() {

        System.out.println(API_KEY);
        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/api/homes"))
                .header("Authorization", API_KEY)
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Error fetching loops";
        }
    }
}
