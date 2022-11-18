package me.vegura.verticles_test.music_streaming;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetControl extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(NetControl.class);

  private static final String JUKEBOX_PLAY_ROUTE = "jukebox.play";
  private static final String JUKEBOX_PAUSE_ROUTE = "jukebox.pause";
  private static final String JUKEBOX_SCHEDULE_ROUTE = "jukebox.schedule";
  private static final String JUKEBOX_LIST_ROUTE = "jukebox.list";

  @Override
  public void start() {
    vertx.createNetServer()
      .connectHandler(this::handleClient)
      .listen(3000);
  }

  private void handleClient(NetSocket netSocket) {
    RecordParser.newDelimited("\n", netSocket)
      .handler(buffer -> handleBuffer(netSocket, buffer))
      .endHandler(v -> logger.info("Connection ended"));
  }

  private void handleBuffer(NetSocket socket, Buffer buffer) {
    String command = buffer.toString();
    switch (command) {
      case "/list":
        listCommands(socket);
        break;
      case "/play":
        vertx.eventBus().send(JUKEBOX_PLAY_ROUTE, new JsonObject());
        break;
      case "/pause":
        vertx.eventBus().send(JUKEBOX_PAUSE_ROUTE, new JsonObject());
        break;
      default:
        if (command.startsWith("/schedule")) {
          schedule(command);
        } else {
          socket.write("Unknown command\n");
        }
    }
  }

  private void schedule(String command) {
    String track = command.substring(10);
    JsonObject musicFileJson = new JsonObject().put("file", track);
    vertx.eventBus().send(JUKEBOX_SCHEDULE_ROUTE, musicFileJson);
  }

  private void listCommands(NetSocket netSocket) {
    vertx.eventBus().request(JUKEBOX_LIST_ROUTE, "", reply -> {
      if (reply.succeeded()) {
        JsonObject data = (JsonObject) reply.result().body();
        data.getJsonArray("files")
          .stream().forEach(name -> netSocket.write(name + "\n"));
      } else {
        logger.error("/list error", reply.cause());
      }
    });
  }
}
