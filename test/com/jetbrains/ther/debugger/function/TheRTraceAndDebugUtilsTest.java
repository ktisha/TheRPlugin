package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.traceAndDebugFunctions;
import static com.jetbrains.ther.debugger.mock.MockTheRExecutor.LS_FUNCTIONS_ERROR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRTraceAndDebugUtilsTest {

  @NotNull
  public static final String LS_FUNCTIONS_COMMAND = FILTER_COMMAND + "(" +
                                                    "function(x) x == \"" + CLOSURE + "\", " +
                                                    EAPPLY_COMMAND + "(" + ENVIRONMENT + "(), " + TYPEOF_COMMAND + ")" +
                                                    ")";

  @NotNull
  public static final String NO_FUNCTIONS_RESULT = "named list()";

  @Test
  public void empty() throws TheRDebuggerException {
    final com.jetbrains.ther.debugger.mock.MockTheRExecutor executor = new com.jetbrains.ther.debugger.mock.MockTheRExecutor() {
      @NotNull
      @Override
      protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
        throw new IllegalStateException("Unexpected command");
      }
    };

    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    traceAndDebugFunctions(
      executor,
      receiver
    );

    assertEquals(1, executor.getCounter());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(Collections.singletonList(LS_FUNCTIONS_ERROR), receiver.getErrors());
  }

  @Test
  public void ordinary() throws TheRDebuggerException {
    final MockTheRExecutor executor = new MockTheRExecutor();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    traceAndDebugFunctions(
      executor,
      receiver
    );

    assertTrue(executor.check());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(
      Arrays.asList(LS_FUNCTIONS_ERROR, "error_x_e", "error_x_d", "error_y_e", "error_y_d"),
      receiver.getErrors()
    );
  }

  private static class MockTheRExecutor implements TheRExecutor {

    private int myLsExecuted = 0;

    private int myXEnterExecuted = 0;
    private int myXTraceExecuted = 0;
    private int myXDebugExecuted = 0;

    private int myYEnterExecuted = 0;
    private int myYTraceExecuted = 0;
    private int myYDebugExecuted = 0;

    @NotNull
    @Override
    public TheRExecutionResult execute(@NotNull final String command) throws TheRDebuggerException {
      final String xEnterFunctionName = "intellij_ther_x_enter";
      final String yEnterFunctionName = "intellij_ther_y_enter";

      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        myLsExecuted++;

        final String output = "$x\n" +
                              FUNCTION_TYPE + "\n\n" +
                              "$" + xEnterFunctionName + "\n" +
                              FUNCTION_TYPE + "\n\n" +
                              "$y\n" +
                              FUNCTION_TYPE;

        return new TheRExecutionResult(
          output,
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(output),
          LS_FUNCTIONS_ERROR
        );
      }

      if (command.equals(xEnterFunctionName + " <- function() { print(\"x\") }")) {
        if (myLsExecuted < 1) throw new IllegalStateException("Enter should be entered after ls");

        myXEnterExecuted++;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.EMPTY,
          TextRange.EMPTY_RANGE,
          "error_x_e"
        );
      }

      if (command
        .equals(TRACE_COMMAND + "(x, " + xEnterFunctionName + ", where = " + ENVIRONMENT + "())")) {
        if (myXEnterExecuted < 1) throw new IllegalStateException("Trace should be called after enter");

        myXTraceExecuted++;

        return new TheRExecutionResult(
          "[1] \"x\"",
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf("[1] \"x\""),
          "error_x_t"
        );
      }

      if (command.equals(DEBUG_COMMAND + "(x)")) {
        if (myXTraceExecuted < 1) throw new IllegalStateException("Debug should be called after trace");

        myXDebugExecuted++;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.EMPTY,
          TextRange.EMPTY_RANGE,
          "error_x_d"
        );
      }

      if (command.equals(yEnterFunctionName + " <- function() { print(\"y\") }")) {
        if (myLsExecuted < 1) throw new IllegalStateException("Enter should be entered after ls");

        myYEnterExecuted++;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.EMPTY,
          TextRange.EMPTY_RANGE,
          "error_y_e"
        );
      }

      if (command
        .equals(TRACE_COMMAND + "(y, " + yEnterFunctionName + ", where = " + ENVIRONMENT + "())")) {
        if (myYEnterExecuted < 1) throw new IllegalStateException("Trace should be called after enter");

        myYTraceExecuted++;

        return new TheRExecutionResult(
          "[1] \"y\"",
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf("[1] \"y\""),
          "error_y_t"
        );
      }

      if (command.equals(DEBUG_COMMAND + "(y)")) {
        if (myYTraceExecuted < 1) throw new IllegalStateException("Debug should be called after trace");

        myYDebugExecuted++;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.EMPTY,
          TextRange.EMPTY_RANGE,
          "error_y_d"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    public boolean check() {
      return myLsExecuted == 1 &&
             myXEnterExecuted == 1 &&
             myXTraceExecuted == 1 &&
             myXDebugExecuted == 1 &&
             myYEnterExecuted == 1 &&
             myYTraceExecuted == 1 &&
             myYDebugExecuted == 1;
    }
  }
}