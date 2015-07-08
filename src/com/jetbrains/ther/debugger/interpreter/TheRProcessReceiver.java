package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;

class TheRProcessReceiver {

  @NotNull
  private final Reader myReader;

  @NotNull
  private final char[] myBuffer;

  public TheRProcessReceiver(@NotNull final Reader reader) {
    myReader = reader;
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
        if (TheRProcessResponseCalculator.isComplete(sb)) {
          return TheRProcessResponseCalculator.calculate(sb);
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
}
