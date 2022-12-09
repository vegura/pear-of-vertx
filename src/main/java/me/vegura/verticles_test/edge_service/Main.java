package me.vegura.verticles_test.edge_service;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Main {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(HeatSensor.class.getName(),
      new DeploymentOptions().setConfig(new JsonObject().put("http.port", 3000)));

    vertx.deployVerticle(HeatSensor.class.getName(),
      new DeploymentOptions().setConfig(new JsonObject().put("http.port", 3001)));

    vertx.deployVerticle(HeatSensor.class.getName(),
      new DeploymentOptions().setConfig(new JsonObject().put("http.port", 3002)));

    vertx.deployVerticle(CollectorService.class.getName());
    vertx.deployVerticle(SnapshotService.class.getName());
  }
}
