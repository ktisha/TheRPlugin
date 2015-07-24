package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.DEFAULT_BUFFER;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.INITIAL_SLEEP;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseCalculator.calculate;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseCalculator.isComplete;

class TheRProcessReceiver {

  @NotNull
  private final Reader myOutputReader;

  @NotNull
  private final Reader myErrorReader;

  @NotNull
  private final char[] myBuffer;

  public TheRProcessReceiver(@NotNull final Reader outputReader, @NotNull final Reader errorReader) {
    myOutputReader = outputReader;
    myErrorReader = errorReader;

    myBuffer = new char[DEFAULT_BUFFER];
  }

  @NotNull
  public TheRProcessResponse receive() throws TheRDebuggerException {
    final StringBuilder sb = new StringBuilder();
    long millis = INITIAL_SLEEP;

    try {
      while (true) {
        if (!appendOutput(sb)) {
          Thread.sleep(millis);

          millis *= 2;
        }
        else {
          if (isComplete(sb)) {
            return calculate(sb, readError());
          }

          millis = INITIAL_SLEEP;
        }
      }
    }
    catch (final IOException e) {
      throw new TheRDebuggerException(e);
    }
    catch (final InterruptedException e) {
      throw new TheRDebuggerException(e);
    }
  }

  private boolean appendOutput(@NotNull final StringBuilder sb) throws IOException {
    final int length = myOutputReader.read(myBuffer);

    sb.append(myBuffer, 0, length);

    return length != 0;
  }

  @NotNull
  private String readError() throws IOException {
    final StringBuilder sb = new StringBuilder();

    while (myErrorReader.ready()) {
      final int length = myErrorReader.read(myBuffer);

      sb.append(myBuffer, 0, length);
    }

    return sb.toString();
  }
}
