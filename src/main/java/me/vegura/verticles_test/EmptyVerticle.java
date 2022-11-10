package me.vegura.verticles_test;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(EmptyVerticle.class);

  @Override
  public void start() {
    logger.info("Starting verticle");
  }

  @Override
  public void stop() {
    logger.info("Stopping verticle");
  }
}
