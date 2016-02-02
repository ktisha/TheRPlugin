package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRResponseConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultTypeException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.mock.IllegalTheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.mock.MockTheRExecutor;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;
import static com.jetbrains.ther.debugger.mock.MockTheRExecutor.LS_FUNCTIONS_ERROR;
import static org.junit.Assert.assertEquals;

public class TheRFunctionDebuggerFactoryImplTest {

  @Test
  public void braceFunction() throws TheRDebuggerException {
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    final TheRFunctionDebugger debugger = new TheRFunctionDebuggerFactoryImpl().getFunctionDebugger(
      new BraceTheRExecutor(),
      new IllegalTheRFunctionDebuggerHandler(),
      outputReceiver
    );

    final TheRLocation expected = new TheRLocation("abc", 1);

    assertEquals(expected, debugger.getLocation());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Arrays.asList("error_entry", "error_st", "error_dbg_at", LS_FUNCTIONS_ERROR), outputReceiver.getErrors());
  }

  @Test
  public void unbraceFunction() throws TheRDebuggerException {
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    final TheRFunctionDebugger debugger = new TheRFunctionDebuggerFactoryImpl().getFunctionDebugger(
      new UnbraceTheRExecutor(),
      new IllegalTheRFunctionDebuggerHandler(),
      outputReceiver
    );

    final TheRLocation expected = new TheRLocation("abc", 0);

    assertEquals(expected, debugger.getLocation());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Arrays.asList("error_entry", "error_st", LS_FUNCTIONS_ERROR), outputReceiver.getErrors());
  }

  @Test(expected = TheRUnexpectedExecutionResultTypeException.class)
  public void unexpectedResult() throws TheRDebuggerException {
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    new TheRFunctionDebuggerFactoryImpl().getFunctionDebugger(
      new UnexpectedResultTheRExecutor(),
      new IllegalTheRFunctionDebuggerHandler(),
      outputReceiver
    );
  }

  private static class BraceTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        return new TheRExecutionResult(
          "",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_entry"
        );
      }

      if (getCounter() == 2) {
        return new TheRExecutionResult(
          TheRResponseConstants.TRACING_PREFIX + "abc(1) on entry\n" +
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
        return new TheRExecutionResult(
          TheRResponseConstants.DEBUG_AT_LINE_PREFIX + "2: x + 1",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class UnbraceTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        return new TheRExecutionResult(
          "",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_entry"
        );
      }

      if (getCounter() == 2) {
        return new TheRExecutionResult(
          TheRResponseConstants.TRACING_PREFIX + "abc(1) on entry\n" +
          "[1] \"abc\"\n" +
          "debug: x + 1",
          START_TRACE_UNBRACE,
          TextRange.EMPTY_RANGE,
          "error_st"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class UnexpectedResultTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() < 3) {
        return new TheRExecutionResult(
          "",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_entry"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }
}