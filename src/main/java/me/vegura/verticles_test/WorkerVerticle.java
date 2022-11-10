package me.vegura.verticles_test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(WorkerVerticle.class);

    @Override
    public void start() {
      vertx.setPeriodic(10_000, id -> {
        try {
          logger.info("Zzz...");
          Thread.sleep(8000);
          logger.info("Up!");
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      });
    }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DeploymentOptions options = new DeploymentOptions()
      .setInstances(2)
      .setWorker(true);
    vertx.deployVerticle("me.vegura.verticles_test.WorkerVerticle", options);
  }
}
