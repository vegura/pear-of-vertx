package me.vegura.verticles_test.heat_sensor;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(HeatSensor.class.getName(), new DeploymentOptions().setInstances(4));
    vertx.deployVerticle(HttpServer.class.getName());
    vertx.deployVerticle(Listener.class.getName());
    vertx.deployVerticle(SensorData.class.getName());
  }
}
