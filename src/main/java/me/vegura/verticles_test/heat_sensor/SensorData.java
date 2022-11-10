package me.vegura.verticles_test.heat_sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.stream.Collectors;

public class SensorData extends AbstractVerticle {

  private static final String SENSOR_UPDATES_ROUTE = "sensor.updates";
  private static final String SENSOR_AVERAGE_ROUTE = "sensor.average";

  private final HashMap<String, Double> temperatureUpdateMap = new HashMap<>();

  @Override
  public void start() {
    EventBus eventBus = vertx.eventBus();
    eventBus.<JsonObject>consumer(SENSOR_UPDATES_ROUTE, this::handleUpdate);
    eventBus.<JsonObject>consumer(SENSOR_AVERAGE_ROUTE, this::handleAverage);
  }

  private void handleUpdate(Message<JsonObject> message) {
    JsonObject temperatureUpdate = message.body();
    temperatureUpdateMap.put(temperatureUpdate.getString("id"), temperatureUpdate.getDouble("temp"));
  }

  private void handleAverage(Message<JsonObject> message) {
    Double averageTemperature = temperatureUpdateMap.values().stream().collect(Collectors.averagingDouble(Double::doubleValue));
    JsonObject averageTemperatureJson = new JsonObject().put("average", averageTemperature);
    message.reply(averageTemperatureJson);
  }
}
