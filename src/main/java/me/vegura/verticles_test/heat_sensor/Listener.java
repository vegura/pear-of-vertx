package me.vegura.verticles_test.heat_sensor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class Listener extends AbstractVerticle {

    private static final String SENSOR_UPDATES_ROUTE = "sensor.updates";

    private final Logger logger = LoggerFactory.getLogger(Listener.class);
    private final DecimalFormat format = new DecimalFormat("#.##");

    @Override
    public void start() {
      EventBus eventBus = vertx.eventBus();
      eventBus.<JsonObject>consumer(SENSOR_UPDATES_ROUTE, this::handleSensor);
//      eventBus.consumer(SENSOR_UPDATES_ROUTE, (Message<JsonObject> msg) -> {
//        logger.info("Message handler -> {}, type -> {}", msg.body(), msg.body().getClass());
//        JsonObject body = msg.body();
//        String id = body.getString("id");
//        String temp = format.format(body.getDouble("temp"));
//        logger.info("Update received: Sensor -> {} with temperature -> {} ", id, temp);
//      });
    }

    private void handleSensor(Message<JsonObject> payload) {
      JsonObject body = payload.body();
      String id = body.getString("id");
      String temp = format.format(body.getDouble("temp"));
      logger.info("Update received: Sensor -> {} with temperature -> {} ", id, temp);
    }
}
