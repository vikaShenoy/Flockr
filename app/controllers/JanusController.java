package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class JanusController extends Controller {

    private final WSClient ws;

    @Inject
    public JanusController(WSClient ws) {
        this.ws = ws;

    }

    public CompletionStage<Result> getJanus(Http.Request request) {

        System.out.println(request.uri());
        WSRequest proxyRequest = ws.url("http://localhost:8088/janus");

        return proxyRequest.get().thenApplyAsync(response -> {
            System.out.println("Status is: " + response.getStatus());
            System.out.println("Body is: ");
            System.out.println(response.getBody());
            JsonNode resJson = response.asJson();
            return ok(resJson);
        });


    }

    public CompletionStage<Result> get(Http.Request request, String path) {

        System.out.println(request.uri());
        WSRequest proxyRequest = ws.url("http://localhost:8088/janus/" + path);

        return proxyRequest.get().thenApplyAsync(response -> {
            System.out.println("Status is: " + response.getStatus());
            System.out.println("Body is: ");
            System.out.println(response.getBody());
            JsonNode resJson = response.asJson();
            return ok(resJson);
        });


    }

    public CompletionStage<Result> postJanus(Http.Request request) {
        System.out.println("I am in here");
        WSRequest proxyRequest = ws.url("http://localhost:8088/janus");

        return proxyRequest.post(request.body().asJson()).thenApplyAsync(response -> {
            System.out.println("Status is: " + response.getStatus());
            System.out.println("Body is: ");
            System.out.println(response.getBody());
            JsonNode resJson = response.asJson();
            return ok(resJson);
        });
    }

    public CompletionStage<Result> post(Http.Request request, String path) {
        System.out.println("I am in here");
        WSRequest proxyRequest = ws.url("http://localhost:8088/janus/" + path);

        return proxyRequest.post(request.body().asJson()).thenApplyAsync(response -> {
            System.out.println("Status is: " + response.getStatus());
            System.out.println("Body is: ");
            System.out.println(response.getBody());
            JsonNode resJson = response.asJson();
            return ok(resJson);
        });
    }

}
