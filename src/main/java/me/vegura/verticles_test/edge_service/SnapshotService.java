package me.vegura.verticles_test.edge_service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotService extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public void start() {
    vertx.createHttpServer()
      .requestHandler(this::handleRequest)
      .listen(config().getInteger("http.port", 4000));
  }

  private void handleRequest(HttpServerRequest request) {
    if (isRequestBad(request)) {
      request.response()
        .setStatusCode(400)
        .end();
    }

    request.bodyHandler(buffer -> {
      logger.info("Latest temperatures: {}", buffer.toJsonObject().encodePrettily());
      request.response().end();
    });
  }

  private boolean isRequestBad(HttpServerRequest request) {
    return !request.method().equals(HttpMethod.POST) ||
      !"application/json".equals(request.getHeader("Content-Type"));
  }
}
