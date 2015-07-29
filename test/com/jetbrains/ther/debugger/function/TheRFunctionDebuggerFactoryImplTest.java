package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.mock.IllegalTheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.mock.IllegalTheROutputReceiver;
import com.jetbrains.ther.debugger.mock.TheROutputErrorReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;
import static org.junit.Assert.assertEquals;

public class TheRFunctionDebuggerFactoryImplTest {

  @Test
  public void braceFunction() throws TheRDebuggerException {
    final TheROutputErrorReceiver outputReceiver = new TheROutputErrorReceiver("error");

    final TheRFunctionDebugger debugger = new TheRFunctionDebuggerFactoryImpl().getNotMainFunctionDebugger(
      new BraceTheRProcess(),
      new IllegalTheRFunctionDebuggerHandler(),
      outputReceiver
    );

    final TheRLocation expected = new TheRLocation("abc", 1);

    assertEquals(expected, debugger.getLocation());
    assertEquals(1, outputReceiver.getErrorReceived());
  }

  @Test
  public void unbraceFunction() throws TheRDebuggerException {
    final TheRFunctionDebugger debugger = new TheRFunctionDebuggerFactoryImpl().getNotMainFunctionDebugger(
      new UnbraceTheRProcess(),
      new IllegalTheRFunctionDebuggerHandler(),
      new IllegalTheROutputReceiver()
    );

    final TheRLocation expected = new TheRLocation("abc", 0);

    assertEquals(expected, debugger.getLocation());
  }

  @Test(expected = UnexpectedResponseException.class)
  public void unexpectedResponse() throws TheRDebuggerException {
    new TheRFunctionDebuggerFactoryImpl().getNotMainFunctionDebugger(
      new UnexpectedResponseTheRProcess(),
      new IllegalTheRFunctionDebuggerHandler(),
      new IllegalTheROutputReceiver()
    );
  }

  private static class BraceTheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      if (myCounter < 3) {
        myCounter++;

        return new TheRProcessResponse(
          "",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (myCounter == 3) {
        myCounter++;

        return new TheRProcessResponse(
          "Tracing abc(1) on entry\n" +
          "[1] \"abc\"\n" +
          "debug: {\n" +
          "    x + 1\n" +
          "}",
          START_TRACE_BRACE,
          TextRange.EMPTY_RANGE,
          "error"
        );
      }

      if (myCounter == 4) {
        myCounter++;

        return new TheRProcessResponse(
          "debug at #2: x + 1",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (myCounter == 5) {
        myCounter++;

        final String output = "named list()";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          ""
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }
  }

  private static class UnbraceTheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      if (myCounter < 3) {
        myCounter++;

        return new TheRProcessResponse(
          "",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (myCounter == 3) {
        myCounter++;

        return new TheRProcessResponse(
          "Tracing abc(1) on entry\n" +
          "[1] \"abc\"\n" +
          "debug: x + 1",
          START_TRACE_UNBRACE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (myCounter == 4) {
        myCounter++;

        final String output = "named list()";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          ""
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }
  }

  private static class UnexpectedResponseTheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      if (myCounter < 3) {
        myCounter++;

        return new TheRProcessResponse(
          "",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (myCounter == 3) {
        myCounter++;

        return new TheRProcessResponse(
          "",
          PLUS,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }
  }
}