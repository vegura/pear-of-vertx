package me.vegura.verticles_test.music_streaming;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;


public class Jukebox extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(Jukebox.class);

  private enum State {PLAYING, PAUSED}

  private static final String JUKEBOX_LIST_ROUTE = "jukebox.list";
  private static final String JUKEBOX_SCHEDULE_ROUTE = "jukebox.schedule";
  private static final String JUKEBOX_PAUSE_ROUTE = "jukebox.pause";
  private static final String JUKEBOX_PLAY_ROUTE = "jukebox.play";

  private State currentMode = State.PAUSED;

  private AsyncFile currentFile;
  private long positionInFile;

  private final Queue<String> playlists = new ArrayDeque<>();

  @Override
  public void start() {
    EventBus eventBus = vertx.eventBus();
    eventBus.consumer(JUKEBOX_LIST_ROUTE, this::list);
    eventBus.consumer(JUKEBOX_SCHEDULE_ROUTE, this::schedule);
    eventBus.consumer(JUKEBOX_PAUSE_ROUTE, this::pause);
    eventBus.consumer(JUKEBOX_PLAY_ROUTE, this::play);

    vertx.createHttpServer()
      .requestHandler(this::httpHandler)
      .listen(8080);

    vertx.setPeriodic(100, this::streamAudioChunk);
  }

  private void list(Message<?> message) {
    vertx.fileSystem().readDir("tracks", ".*mp3", asyncResult -> {
      if (asyncResult.succeeded()) {
        List<String> files = asyncResult.result()
          .stream()
          .map(File::new)
          .map(File::getName)
          .collect(Collectors.toList());
        JsonObject songs = new JsonObject().put("files", files);
        message.reply(songs);
      } else {
        logger.error("readDir failed", asyncResult.cause());
        message.fail(500, asyncResult.cause().getMessage());
      }
    });
  }

  private void schedule(Message<JsonObject> message) {
    String file = message.body().getString("file");
    if (playlists.isEmpty() && currentMode == State.PAUSED) {
      currentMode = State.PLAYING;
    }
    playlists.offer(file);
  }

  private void pause(Message<?> message) {
    this.currentMode = State.PAUSED;
  }

  private void play(Message<?> message) {
    this.currentMode = State.PLAYING;
  }

  private void httpHandler(HttpServerRequest request) {
    if ("/".equals(request.path())) {
      openAudioStream(request);
      return;
    }

    if (request.path().startsWith("/download")) {
      String sanitizedPath = request.path().substring(10).replaceAll("/", "");
      download(sanitizedPath, request);
      return;
    }

    request.response().setStatusCode(404).end();
  }

  private final Set<HttpServerResponse> streamers = new HashSet<>();

  private void openAudioStream(HttpServerRequest request) {
    HttpServerResponse response = request.response()
      .putHeader("Content-Type", "audio/mpeg")
      .setChunked(true);
    streamers.add(response);
    response.endHandler(v -> {
      streamers.remove(response);
      logger.info("A streamer left");
    });
  }

  private void download(String path, HttpServerRequest request) {
    String file = "tracks/" + path;
    if (!vertx.fileSystem().existsBlocking(file)) {
      request.response().setStatusCode(404).end();
    }

    OpenOptions openOptions = new OpenOptions().setRead(true);
    vertx.fileSystem().open(file, openOptions, asyncResult -> {
      if (asyncResult.succeeded()) {
        downloadFile(asyncResult.result(), request);

      } else {
        logger.error("Read failed", asyncResult.cause());
        request.response().setStatusCode(500).end();

      }
    });
  }

  private void downloadFile(AsyncFile asyncFile, HttpServerRequest request) {
    HttpServerResponse response = request.response();
    response.setStatusCode(200)
      .putHeader("Content-Type", "audio/mpeg")
      .setChunked(true);

//    pipeCustom(asyncFile, response);
    asyncFile.pipeTo(response);
  }

  private void pipeCustom(AsyncFile asyncFile, HttpServerResponse response) {
    asyncFile.handler(buffer -> {
      response.write(buffer);
      if (response.writeQueueFull()) {
        asyncFile.pause();
        response.drainHandler(__ -> asyncFile.resume());
      }
    });

    asyncFile.endHandler(__ -> response.end());
  }

  private void streamAudioChunk(long id) {
    if (currentMode == State.PAUSED)
      return;

    if (currentFile == null && playlists.isEmpty()) {
      currentMode = State.PAUSED;
      return;
    }

    if (currentFile == null)
      openNextFile();

    currentFile.read(Buffer.buffer(4096), 0, positionInFile, 4096, ar -> {
      if (ar.succeeded()) {
        processReadBuffer(ar.result());
      } else {
        logger.error("Read failed", ar.cause());
        closeCurrentFile();
      }
    });
  }

  private void processReadBuffer(Buffer bufferData) {
    positionInFile += bufferData.length();
    if (bufferData.length() == 0) {
      closeCurrentFile();
      return;
    }

    for (HttpServerResponse streamer : streamers) {
      if (!streamer.writeQueueFull()) {
        streamer.write(bufferData.copy());
      }
    }
  }

  private void openNextFile() {
    OpenOptions openOptions = new OpenOptions().setRead(true);
    currentFile = vertx.fileSystem()
      .openBlocking("tracks/" + playlists.poll(), openOptions);
    positionInFile = 0;
  }

  private void closeCurrentFile() {
    positionInFile = 0;
    currentFile.close();
    currentFile = null;
  }
}
