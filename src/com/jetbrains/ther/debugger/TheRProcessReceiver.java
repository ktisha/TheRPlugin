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
  private final InputStreamReader myReader;

  @NotNull
  private final char[] myBuffer;

  public TheRProcessReceiver(@NotNull final InputStream stream) {
    myReader = new InputStreamReader(stream);
    myBuffer = new char[TheRDebugConstants.DEFAULT_BUFFER];
  }

  @NotNull
  public TheRProcessResponse receive() throws IOException, InterruptedException {
    final StringBuilder sb = new StringBuilder();
    long millis = TheRDebugConstants.INITIAL_SLEEP;

    while (true) {
      if (!appendResponse(sb)) {
        Thread.sleep(millis);

        millis *= 2;
      }
      else {
        if (TheRProcessResponseTypeCalculator.isComplete(sb)) {
          return calculateResponse(sb);
        }

        millis = TheRDebugConstants.INITIAL_SLEEP;
      }
    }
  }

  private boolean appendResponse(@NotNull final StringBuilder sb) throws IOException {
    final int length = myReader.read(myBuffer);

    sb.append(myBuffer, 0, length);

    return length != 0;
  }

  @NotNull
  private TheRProcessResponse calculateResponse(@NotNull final StringBuilder response) {
    final TheRProcessResponseType type = calculateResponseType(response);

    response.setLength(response.lastIndexOf(TheRDebugConstants.LINE_SEPARATOR)); // remove last line

    return new TheRProcessResponse(toStringFromSecondLine(response), type);
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
