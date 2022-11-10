package me.vegura.verticles_test.heat_sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.UUID;

public class HeatSensor extends AbstractVerticle {

  private static final String SENSOR_UPDATES_ROUTE = "sensor.updates";

  private final Random random = new Random();
  private final String sensorId = UUID.randomUUID().toString();
  private double temperature = 21.0;

  @Override
  public void start() {
    scheduleNextUpdate();
  }

  private void scheduleNextUpdate() {
    vertx.setTimer(random.nextInt(5000) + 1000, this::update);
  }

  private void update(long timerId) {
    temperature = temperature + (delta() / 10);
    JsonObject payload = new JsonObject()
      .put("id", sensorId)
      .put("temp", temperature);
    vertx.eventBus().publish(SENSOR_UPDATES_ROUTE, payload);
    scheduleNextUpdate();
  }

  private double delta() {
    if (random.nextInt() > 0) {
      return random.nextGaussian();
    } else {
      return -random.nextGaussian();
    }
  }
}
