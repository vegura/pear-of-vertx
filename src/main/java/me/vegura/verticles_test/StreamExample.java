package me.vegura.verticles_test;

import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;

public class StreamExample {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    OpenOptions options = new OpenOptions().setRead(true);
    int chunkNumber = 0;
    vertx.fileSystem().open("build.gradle.kts", options, ar -> {
      if (ar.succeeded()) {
        AsyncFile file = ar.result();
        file.handler(System.out::println)
          .exceptionHandler(Throwable::printStackTrace)
          .endHandler(done -> {
            System.out.println("\n--- DONE");
            vertx.close();
          });
      } else {
        ar.cause().printStackTrace();
      }
    });
  }
}
