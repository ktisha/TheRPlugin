package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
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
  public TheRProcessResponse receive() throws IOException, InterruptedException {
    final StringBuilder sb = new StringBuilder();
    int pings = 0;

    while (true) {
      waitForResponse();
      appendResponse(sb);

      if (TheRProcessResponseTypeCalculator.isComplete(sb)) {
        return calculateResponse(sb, pings);
      }

      mySender.send(TheRDebugConstants.PING_COMMAND); // pings interpreter to get tail of response
      pings++;
    }
  }

  private void waitForResponse() throws IOException, InterruptedException {
    long millis = TheRDebugConstants.INITIAL_SLEEP;

    while (myStream.available() == 0) {
      Thread.sleep(millis);
      millis *= 2;
    }
  }

  private void appendResponse(@NotNull final StringBuilder sb) throws IOException {
    while (myStream.available() != 0) {
      final int length = myReader.read(myBuffer);

      sb.append(myBuffer, 0, length);
    }
  }

  @NotNull
  private TheRProcessResponse calculateResponse(@NotNull final StringBuilder response, final int pings) {
    removePings(response, pings);

    final TheRProcessResponseType type = calculateResponseType(response);

    response.setLength(response.lastIndexOf(TheRDebugConstants.LINE_SEPARATOR)); // remove last line

    return new TheRProcessResponse(toStringFromSecondLine(response), type);
  }

  private void removePings(@NotNull final StringBuilder response, final int pings) {
    for (int i = 0; i < pings; i++) {
      response.setLength(
        response.lastIndexOf(TheRDebugConstants.LINE_SEPARATOR) - 1
      );
    }
  }

  @NotNull
  private TheRProcessResponseType calculateResponseType(@NotNull final StringBuilder response) {
    // TODO exception

    return TheRProcessResponseTypeCalculator.calculate(response, response.indexOf(TheRDebugConstants.LINE_SEPARATOR) + 1);
  }

  @NotNull
  private String toStringFromSecondLine(@NotNull final StringBuilder response) {
    final int index = response.indexOf(TheRDebugConstants.LINE_SEPARATOR);

    return (index == -1) ? "" : response.substring(index + 1);
  }
}
