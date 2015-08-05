package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.mock.IllegalTheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import com.jetbrains.ther.debugger.mock.MockTheRProcess;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;

import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.NO_FUNCTIONS_RESPONSE;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRFunctionDebuggerFactoryImplTest {

  @Test
  public void braceFunction() throws TheRDebuggerException {
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    final TheRFunctionDebugger debugger = new TheRFunctionDebuggerFactoryImpl().getNotMainFunctionDebugger(
      new BraceTheRProcess(),
      new IllegalTheRFunctionDebuggerHandler(),
      outputReceiver
    );

    final TheRLocation expected = new TheRLocation("abc", 1);

    assertEquals(expected, debugger.getLocation());
    assertEquals(Arrays.asList("error1", "error_st", "error_dbg_at", "error_trc"), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
  }

  @Test
  public void unbraceFunction() throws TheRDebuggerException {
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    final TheRFunctionDebugger debugger = new TheRFunctionDebuggerFactoryImpl().getNotMainFunctionDebugger(
      new UnbraceTheRProcess(),
      new IllegalTheRFunctionDebuggerHandler(),
      outputReceiver
    );

    final TheRLocation expected = new TheRLocation("abc", 0);

    assertEquals(expected, debugger.getLocation());
    assertEquals(Arrays.asList("error1", "error_st", "error_trc"), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
  }

  @Test(expected = UnexpectedResponseException.class)
  public void unexpectedResponse() throws TheRDebuggerException {
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    new TheRFunctionDebuggerFactoryImpl().getNotMainFunctionDebugger(
      new UnexpectedResponseTheRProcess(),
      new IllegalTheRFunctionDebuggerHandler(),
      outputReceiver
    );
  }

  private static class BraceTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        return new TheRProcessResponse(
          "",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      if (getCounter() == 2) {
        return new TheRProcessResponse(
          TheRDebugConstants.TRACING + " abc(1) on entry\n" +
          "[1] \"abc\"\n" +
          "debug: {\n" +
          "    x + 1\n" +
          "}",
          START_TRACE_BRACE,
          TextRange.EMPTY_RANGE,
          "error_st"
        );
      }

      if (getCounter() == 3) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "2: x + 1",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at"
        );
      }

      if (getCounter() == 4) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error_trc"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class UnbraceTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        return new TheRProcessResponse(
          "",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      if (getCounter() == 2) {
        return new TheRProcessResponse(
          TheRDebugConstants.TRACING + " abc(1) on entry\n" +
          "[1] \"abc\"\n" +
          "debug: x + 1",
          START_TRACE_UNBRACE,
          TextRange.EMPTY_RANGE,
          "error_st"
        );
      }

      if (getCounter() == 3) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error_trc"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class UnexpectedResponseTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() < 3) {
        return new TheRProcessResponse(
          "",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }
}