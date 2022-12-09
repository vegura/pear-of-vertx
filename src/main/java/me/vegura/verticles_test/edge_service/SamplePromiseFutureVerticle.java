package me.vegura.verticles_test.edge_service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamplePromiseFutureVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(SamplePromiseFutureVerticle.class);

  @Override
  public void start(Promise<Void> promise) {
    vertx.createHttpServer()
      .requestHandler(this::handleRequest)
      .listen(8080)
      .onFailure(promise::fail)
      .onSuccess(it -> {
        System.out.println("http://127.0.0.1:8080/");
        promise.complete();
      });
  }

  public void handleRequest(HttpServerRequest request) {
    logger.info("Handling the request");
  }
}
