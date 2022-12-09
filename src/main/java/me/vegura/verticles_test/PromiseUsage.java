package me.vegura.verticles_test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class PromiseUsage extends AbstractVerticle {

  @Override
  public void start() {
    process()
      .onSuccess(System.out::println)
      .onFailure(error -> System.out.println(error.getMessage()));
  }

  private Future<String> process() {
    Promise<String> promise = Promise.promise();
    vertx.setTimer(5000, id -> {
      if (System.currentTimeMillis() % 2l == 0L) {
        promise.complete("Ok");
      } else {
        promise.fail(new RuntimeException("Bad luck..."));
      }
    });
    return promise.future();
  }
}
