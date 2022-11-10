package me.vegura.verticles_test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class MixedThreading extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(MixedThreading.class);

  @Override
  public void start() {
    Context ctx = vertx.getOrCreateContext();
    new Thread(() -> {
      try {
        run(ctx);
      } catch (InterruptedException ex) {
        logger.error("Error: ", ex);
      }
    }).start();
  }

  private void run(Context context) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    logger.info("I am in a non-Vertx thread");
    context.runOnContext(v -> {
      logger.info("Event loop running");
      vertx.setTimer(1000, id -> {
        logger.info("This is a final countdown");
        latch.countDown();
      });
    });
    logger.info("Waiting on the countdown latch....");
    latch.await();
    logger.info("Done!");
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MixedThreading());
  }
}
