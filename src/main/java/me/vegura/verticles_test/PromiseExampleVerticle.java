package me.vegura.verticles_test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromiseExampleVerticle extends AbstractVerticle {

  private static Logger logger = LoggerFactory.getLogger(PromiseExampleVerticle.class);

  @Override
  public void start(Promise<Void> promise) {
    vertx.createHttpServer()
      .requestHandler(request -> request.response().end("OK"))
      .listen(8080, ar -> {
        if (ar.succeeded()) {
          promise.complete();
          logger.info("The promise of starting server is completed");
        } else {
          promise.fail(ar.cause());
          logger.info("The promise of starting server is failed");
        }
      });
  }
}
