package me.vegura.verticles_test.music_streaming;

import io.vertx.core.Vertx;

public class Main {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(Jukebox.class.getName());
    vertx.deployVerticle(NetControl.class.getName());
  }
}
