package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
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
  public TheRProcessResponse receive() throws TheRDebuggerException {
    final StringBuilder sb = new StringBuilder();
    long millis = TheRDebugConstants.INITIAL_SLEEP;

    while (true) {
      if (!appendResponse(sb)) {
        sleep(millis);

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

  private boolean appendResponse(@NotNull final StringBuilder sb) throws TheRDebuggerException {
    final int length = read();

    sb.append(myBuffer, 0, length);

    return length != 0;
  }

  private void sleep(final long millis) throws TheRDebuggerException {
    try {
      Thread.sleep(millis);
    }
    catch (final InterruptedException e) {
      throw new TheRDebuggerException(e);
    }
  }

  private int read() throws TheRDebuggerException {
    try {
      return myReader.read(myBuffer);
    }
    catch (final IOException e) {
      throw new TheRDebuggerException(e);
    }
  }
}
