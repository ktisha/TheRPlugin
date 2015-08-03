package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import com.jetbrains.ther.debugger.mock.AlwaysSameResponseTheRProcess;
import com.jetbrains.ther.debugger.mock.IllegalTheROutputReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRTraceAndDebugUtilsTest {

  @Test
  public void empty() throws TheRDebuggerException {
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      NO_FUNCTIONS_RESPONSE,
      TheRProcessResponseType.RESPONSE,
      TextRange.allOf(NO_FUNCTIONS_RESPONSE),
      ""
    );

    traceAndDebugFunctions(
      process,
      new IllegalTheROutputReceiver()
    );

    assertEquals(1, process.getCounter());
  }

  @Test
  public void ordinary() throws TheRDebuggerException {
    final MockTheRProcess process = new MockTheRProcess();

    traceAndDebugFunctions(
      process,
      new IllegalTheROutputReceiver()
    );

    assertTrue(process.check());
  }

  private static class MockTheRProcess implements TheRProcess {

    private int myLsExecuted = 0;

    private int myXEnterExecuted = 0;
    private int myXTraceExecuted = 0;
    private int myXDebugExecuted = 0;

    private int myYEnterExecuted = 0;
    private int myYTraceExecuted = 0;
    private int myYDebugExecuted = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
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

        return new TheRProcessResponse(
          output,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(output),
          ""
        );
      }

      if (command.equals(xEnterFunctionName + " <- function() { print(\"x\") }")) {
        if (myLsExecuted < 1) throw new IllegalStateException("Enter should be entered after ls");

        myXEnterExecuted++;

        return new TheRProcessResponse(
          "",
          TheRProcessResponseType.EMPTY,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command
        .equals(TRACE_COMMAND + "(x, " + xEnterFunctionName + ", where = " + ENVIRONMENT + "())")) {
        if (myXEnterExecuted < 1) throw new IllegalStateException("Trace should be called after enter");

        myXTraceExecuted++;

        return new TheRProcessResponse(
          "[1] \"x\"",
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf("[1] \"x\""),
          ""
        );
      }

      if (command.equals(DEBUG_COMMAND + "(x)")) {
        if (myXTraceExecuted < 1) throw new IllegalStateException("Debug should be called after trace");

        myXDebugExecuted++;

        return new TheRProcessResponse(
          "",
          TheRProcessResponseType.EMPTY,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command.equals(yEnterFunctionName + " <- function() { print(\"y\") }")) {
        if (myLsExecuted < 1) throw new IllegalStateException("Enter should be entered after ls");

        myYEnterExecuted++;

        return new TheRProcessResponse(
          "",
          TheRProcessResponseType.EMPTY,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command
        .equals(TRACE_COMMAND + "(y, " + yEnterFunctionName + ", where = " + ENVIRONMENT + "())")) {
        if (myYEnterExecuted < 1) throw new IllegalStateException("Trace should be called after enter");

        myYTraceExecuted++;

        return new TheRProcessResponse(
          "[1] \"y\"",
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf("[1] \"y\""),
          ""
        );
      }

      if (command.equals(DEBUG_COMMAND + "(y)")) {
        if (myYTraceExecuted < 1) throw new IllegalStateException("Debug should be called after trace");

        myYDebugExecuted++;

        return new TheRProcessResponse(
          "",
          TheRProcessResponseType.EMPTY,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
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