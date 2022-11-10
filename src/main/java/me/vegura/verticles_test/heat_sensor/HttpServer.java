package me.vegura.verticles_test.heat_sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.TimeoutStream;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

public class HttpServer extends AbstractVerticle {

  private static final String SENSOR_UPDATES_ROUTE = "sensor.updates";
  private static final String SENSOR_AVERAGE_ROUTE = "sensor.average";

  private static final JsonObject EMPTY = new JsonObject();

  @Override
  public void start() {
    vertx.createHttpServer()
      .requestHandler(this::handler)
      .listen(config().getInteger("port", 8080));
  }

  private void handler(HttpServerRequest request) {
    if ("/".equals(request.path())) {
      request.response().sendFile("index.html");
    } else if ("/sse".equals(request.path())) {
        sse(request);
    } else {
      request.response().setStatusCode(404);
    }
  }

  private void sse(HttpServerRequest request) {
    HttpServerResponse response = request.response();
    response
      .putHeader("Content-Type", "text/event-stream")
      .putHeader("Cache-Control", "no-cache")
      .setChunked(true);

    MessageConsumer<JsonObject> consumer = vertx.eventBus().<JsonObject>consumer(SENSOR_UPDATES_ROUTE);
    consumer.handler(message -> {
      response.write("event: update\n");
      response.write("data: " + message.body().encode() + "\n\n");
    });

    TimeoutStream ticks = vertx.periodicStream(1000);
    ticks.handler(id -> {
      vertx.eventBus().<JsonObject>request(SENSOR_AVERAGE_ROUTE, "", reply -> {
        if (reply.succeeded()) {
          response.write("event: average\n");
          response.write("data: " + reply.result().body().encode() + "\n\n");
        }
      });
    });

    response.endHandler(v -> {
      consumer.unregister();
      ticks.cancel();
    });
  }
}
