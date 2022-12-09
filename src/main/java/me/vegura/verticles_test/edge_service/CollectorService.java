package me.vegura.verticles_test.edge_service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.predicate.ResponsePredicate;
import io.vertx.ext.web.codec.BodyCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class CollectorService extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(CollectorService.class);
  private WebClient webClient;

  @Override
  public void start() {
      webClient = WebClient.create(vertx);
      vertx.createHttpServer()
        .requestHandler(this::handleRequest)
        .listen(8080);
  }

  private void handleRequest(HttpServerRequest request) {
    List<JsonObject> responses = new ArrayList<>();
    AtomicInteger counter = new AtomicInteger(0);
    for (int i  = 0; i < 3; i++) {
      webClient.get(3000 + i, "localhost", "/")
        .expect(ResponsePredicate.SC_SUCCESS)
        .as(BodyCodec.jsonObject())
        .send(asyncResult -> {
          if (asyncResult.succeeded()) {
            responses.add(asyncResult.result().body());
          } else {
            logger.error("Sensor is probably down", asyncResult.cause());
          }

          if (counter.incrementAndGet() == 3) {
            JsonObject data = new JsonObject()
              .put("data", new JsonArray(responses));
            sendToSnapshot(request, data);
          }
        });
    }
  }

  private void handleRequest1(HttpServerRequest request) {

  }

  private void sendToSnapshot(HttpServerRequest request, JsonObject data) {
    webClient.post(4000, "localhost", "/")
      .expect(ResponsePredicate.SC_SUCCESS)
      .sendJsonObject(data, asyncResult -> {
        if (asyncResult.succeeded()) {
          sendResponse(request, data);
        } else {
          logger.error("Snapshot is probably down", asyncResult.cause());
          request.response().setStatusCode(500).end();
        }
      });
  }

  private void sendResponse(HttpServerRequest request, JsonObject data) {
    request.response().putHeader("Content-Type", "application/json")
      .end(data.encode());
  }

  private Future<JsonObject> fetchTemperature(int port) {
    return (Future<JsonObject>) webClient
      .get(port, "localhost", "/")
      .expect(ResponsePredicate.SC_SUCCESS)
      .as(BodyCodec.jsonObject())
      .send()
      .map(HttpResponse::body);
  }
}
