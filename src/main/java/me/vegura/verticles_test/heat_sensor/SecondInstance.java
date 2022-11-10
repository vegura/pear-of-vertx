package me.vegura.verticles_test.heat_sensor;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondInstance {
  private static final Logger logger = LoggerFactory.getLogger(SecondInstance.class);

  public static void main(String[] args) {
    Vertx.clusteredVertx(new VertxOptions(), ar -> {
      if (ar.succeeded()) {
        logger.info("Second instance is started");
        Vertx vertx = ar.result();
        vertx.deployVerticle(HeatSensor.class.getName(), new DeploymentOptions().setInstances(4));
        vertx.deployVerticle(Listener.class.getName());
        vertx.deployVerticle(SensorData.class.getName());
        JsonObject httpVerticleConfig = new JsonObject().put("port", 8081);
        vertx.deployVerticle(HttpServer.class.getName(), new DeploymentOptions().setConfig(httpVerticleConfig));
      } else {
        logger.error("Cluster could not start. ", ar.cause());
      }
    });
  }
}
