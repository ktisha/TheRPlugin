package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TheRProcessReceiver {

  @NotNull
  private final InputStream myStream;

  @NotNull
  private final InputStreamReader myReader;

  @NotNull
  private final char[] myBuffer;

  @NotNull
  private final TheRProcessSender mySender;

  public TheRProcessReceiver(@NotNull final InputStream stream, @NotNull final TheRProcessSender sender) {
    myStream = stream;
    myReader = new InputStreamReader(stream);
    myBuffer = new char[TheRDebugConstants.DEFAULT_BUFFER];

    mySender = sender;
  }

  @NotNull
  public String receive() throws IOException, InterruptedException {
    final StringBuilder sb = new StringBuilder();
    int pings = 0;

    while (true) {
      waitForResponse();

      readResponse(sb);

      if (responseIsComplete(sb)) {
        break;
      }

      ping(); // pings interpreter to get tail of response
      pings++;
    }

    return removePingsAndCommand(sb, pings);
  }

  private void waitForResponse() throws IOException, InterruptedException {
    long millis = TheRDebugConstants.INITIAL_SLEEP;

    while (myStream.available() == 0) {
      Thread.sleep(millis);
      millis *= 2;
    }
  }

  private void readResponse(@NotNull final StringBuilder sb) throws IOException {
    while (myStream.available() != 0) {
      sb.append(myBuffer, 0, myReader.read(myBuffer));
    }
  }

  private boolean responseIsComplete(@NotNull final StringBuilder sb) {
    return TheRProcessResponseType.calculateResponseType(sb) != null;
  }

  private void ping() throws IOException {
    mySender.send(TheRDebugConstants.PING_COMMAND);
  }

  @NotNull
  private String removePingsAndCommand(final StringBuilder sb, final int pings) {
    for (int i = 0; i < pings; i++) {
      sb.setLength(sb.lastIndexOf(TheRDebugConstants.LINE_SEPARATOR) - 1);
    }

    return sb.substring(sb.indexOf(TheRDebugConstants.LINE_SEPARATOR) + 1);
  }
}
