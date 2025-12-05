package dev.saturn.hyperloop.util;

import dev.saturn.hyperloop.Hyperloop;
import dev.saturn.hyperloop.modules.HyperloopModule;
import meteordevelopment.meteorclient.systems.modules.Modules;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Utils {

    public static String fetchLoops() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String url = (Modules.get().get(HyperloopModule.class).host.get());
            if(url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/api/homes"))
                .header("Authorization", Modules.get().get(HyperloopModule.class).apiKey.get())
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

    public static String teleport(String home, String username) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String url = (Modules.get().get(HyperloopModule.class).host.get());
            if(url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/api/teleport/" + home))
                .header("Authorization", Modules.get().get(HyperloopModule.class).apiKey.get())
                .header("Username", username)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Error teleporting";
        }
    }
}
