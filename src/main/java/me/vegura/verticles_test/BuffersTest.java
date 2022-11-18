package me.vegura.verticles_test;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuffersTest {

  private static final Logger logger = LoggerFactory.getLogger(BuffersTest.class);
  private static final String FILENAME = "hello.txt";

  public static void main(String[] args) {

  }

  private static void readFromBuffer(Vertx vertx) {
    AsyncFile file = vertx.fileSystem().openBlocking(FILENAME, new OpenOptions().setRead(true));
    RecordParser parser = RecordParser.newFixed(4, file);
    parser.handler(header -> readMagicNumber(header, parser));
  }

  private static void readMagicNumber(Buffer header, RecordParser parser) {
    logger.info("Magic numbers are: {} {} {} {}", header.getByte(0), header.getByte(1), header.getByte(2), header.getByte(3));
    parser.handler(version -> readVersion(version, parser));
  }

  private static void readVersion(Buffer version, RecordParser parser) {
    logger.info("Version is -> {}", version.getInt(0));
    parser.delimitedMode("\n");
    parser.handler(keyLength -> readKey(keyLength, parser));
  }

  private static void readKey(Buffer keyLength, RecordParser parser) {
    parser.fixedSizeMode(keyLength.getInt(0));
    parser.handler(key -> readValue(key.toString(), parser));
  }

  private static void readValue(String keyString, RecordParser parser) {
    parser.fixedSizeMode(4); // because INT
    parser.handler(valueSize -> finishEntry(keyString, valueSize, parser));
  }

  private static void finishEntry(String key, Buffer valueSize, RecordParser parser) {
    parser.fixedSizeMode(valueSize.getInt(0));
    parser.handler(value -> {
      logger.info("Key: {} - Value: {}", key, value);
      parser.fixedSizeMode(4);
      parser.handler(keyLength -> readKey(keyLength, parser));
    });
  }

  private static void writeFileWithBuffer(Vertx vertx) {
    AsyncFile file = vertx.fileSystem().openBlocking(FILENAME, new OpenOptions().setWrite(true).setAppend(true));
    Buffer buffer = Buffer.buffer();
    buffer.appendBytes(new byte[] {1,2,3,4});
    buffer.appendInt(99);
    buffer.appendString("DATABASE NAME\n");

    String key = "key";
    String value = "12345-value";
    buffer.appendInt(key.length())
      .appendString(key)
      .appendInt(value.length())
      .appendString(value);

    key = "foo@bar";
    value = "Foo Bar Baz";
    buffer.appendInt(key.length())
      .appendString(key)
      .appendInt(value.length())
      .appendString(value);

    file.end(buffer, ar -> vertx.close());
  }
}
