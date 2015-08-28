package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.mock.IllegalTheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.mock.MockTheRExecutor;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;

import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtilsTest.NO_FUNCTIONS_RESULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRFunctionDebuggerFactoryImplTest {

  @Test
  public void braceFunction() throws TheRDebuggerException {
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    final TheRFunctionDebugger debugger = new TheRFunctionDebuggerFactoryImpl().getNotMainFunctionDebugger(
      new BraceTheRExecutor(),
      new IllegalTheRFunctionDebuggerHandler(),
      outputReceiver
    );

    final TheRLocation expected = new TheRLocation("abc", 1);

    assertEquals(expected, debugger.getLocation());
    assertEquals(Arrays.asList("error_entry", "error_st", "error_dbg_at", "error_trc"), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
  }

  @Test
  public void unbraceFunction() throws TheRDebuggerException {
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    final TheRFunctionDebugger debugger = new TheRFunctionDebuggerFactoryImpl().getNotMainFunctionDebugger(
      new UnbraceTheRExecutor(),
      new IllegalTheRFunctionDebuggerHandler(),
      outputReceiver
    );

    final TheRLocation expected = new TheRLocation("abc", 0);

    assertEquals(expected, debugger.getLocation());
    assertEquals(Arrays.asList("error_entry", "error_st", "error_trc"), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
  }

  @Test(expected = TheRUnexpectedExecutionResultException.class)
  public void unexpectedResult() throws TheRDebuggerException {
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    new TheRFunctionDebuggerFactoryImpl().getNotMainFunctionDebugger(
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
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT + "2: x + 1",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at"
        );
      }

      if (getCounter() == 4) {
        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_trc"
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
          TheRDebugConstants.TRACING + " abc(1) on entry\n" +
          "[1] \"abc\"\n" +
          "debug: x + 1",
          START_TRACE_UNBRACE,
          TextRange.EMPTY_RANGE,
          "error_st"
        );
      }

      if (getCounter() == 3) {
        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_trc"
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