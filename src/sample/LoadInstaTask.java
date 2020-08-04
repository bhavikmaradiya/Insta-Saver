package sample;

import com.google.gson.Gson;
import javafx.concurrent.Task;
import sample.InstaModals.InstaVideoModal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoadInstaTask extends Task<InstaVideoModal> {
    String requesturl;

    public LoadInstaTask(String requesturl) {
        this.requesturl = requesturl;
    }

    @Override
    protected InstaVideoModal call() throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(requesturl)).GET().build();
        return new Gson().fromJson(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body(), InstaVideoModal.class);
    }
}
