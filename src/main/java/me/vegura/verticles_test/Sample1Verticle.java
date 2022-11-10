package me.vegura.verticles_test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sample1Verticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(Deployer.class);

  @Override
  public void start() {
    logger.info("n = {}", config().getInteger("n", -1));
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    for (int n = 0; n < 4; n++) {
      JsonObject conf = new JsonObject().put("n", n);
      DeploymentOptions options = new DeploymentOptions()
        .setConfig(conf)
        .setInstances(n);
      vertx.deployVerticle("me.vegura.verticles_test.Sample1Verticle", options);
    }
  }
}
