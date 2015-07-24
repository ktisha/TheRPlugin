package com.jetbrains.ther.debugger.interpreter;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.mock.IllegalTheRProcess;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.EMPTY;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.RESPONSE;
import static org.junit.Assert.*;

public class TheRLoadableVarHandlerImplTest {

  @Test
  public void functionType() throws TheRDebuggerException {
    final TraceAndDebugTheRProcess process = new TraceAndDebugTheRProcess();

    assertEquals(
      FUNCTION_TYPE,
      new TheRLoadableVarHandlerImpl().handleType(
        process,
        "abc",
        FUNCTION_TYPE
      )
    );

    assertTrue(process.isComplete());
  }

  @Test
  public void serviceEnterFunctionType() throws TheRDebuggerException {
    assertNull(
      new TheRLoadableVarHandlerImpl().handleType(
        new IllegalTheRProcess(),
        enterFunctionName(),
        FUNCTION_TYPE
      )
    );
  }

  @Test
  public void serviceExitFunctionType() throws TheRDebuggerException {
    assertNull(
      new TheRLoadableVarHandlerImpl().handleType(
        new IllegalTheRProcess(),
        exitFunctionName(),
        FUNCTION_TYPE
      )
    );
  }

  @Test
  public void ordinaryType() throws TheRDebuggerException {
    assertEquals(
      "typeAbc",
      new TheRLoadableVarHandlerImpl().handleType(
        new IllegalTheRProcess(),
        "abc",
        "typeAbc"
      )
    );
  }

  @Test
  public void functionValue() {
    assertEquals(
      "function(x) {\n" +
      "    x ^ 2\n" +
      "}",
      new TheRLoadableVarHandlerImpl().handleValue(
        "abc",
        FUNCTION_TYPE,
        "function(x) {\n" +
        "    x ^ 2\n" +
        "}"
      )
    );
  }

  @Test
  public void innerFunctionValue() {
    assertEquals(
      "function(x) {\n" +
      "    x ^ 2\n" +
      "}",
      new TheRLoadableVarHandlerImpl().handleValue(
        "abc",
        FUNCTION_TYPE,
        "function(x) {\n" +
        "    x ^ 2\n" +
        "}\n" +
        "<" + ENVIRONMENT + ": 0xfffffff>"
      )
    );
  }

  @Test
  public void ordinaryValue() {
    assertEquals(
      "valueAbc",
      new TheRLoadableVarHandlerImpl().handleValue(
        "abc",
        "typeAbc",
        "valueAbc"
      )
    );
  }

  @NotNull
  private static String enterFunctionName() {
    return SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_ENTER_FUNCTION_SUFFIX;
  }

  @NotNull
  private static String exitFunctionName() {
    return SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_EXIT_FUNCTION_SUFFIX;
  }

  @NotNull
  private static String expectedEnterFunction() {
    return enterFunctionName() + " <- function() { print(\"abc\") }";
  }

  @NotNull
  private static String expectedExitFunction() {
    return exitFunctionName() + " <- function() { print(\"abc\") }";
  }

  @NotNull
  private static String expectedTraceCommand() {
    return TRACE_COMMAND +
           "(" +
           "abc, " +
           enterFunctionName() + ", " +
           "exit = " + exitFunctionName() + ", " +
           "where = environment()" +
           ")";
  }

  @NotNull
  private static String expectedDebugCommand() {
    return DEBUG_COMMAND + "(abc)";
  }

  private static class TraceAndDebugTheRProcess implements TheRProcess {

    private boolean myIsEnterExecuted = false;
    private boolean myIsExitExecuted = false;
    private boolean myIsTraceExecuted = false;
    private boolean myIsDebugExecuted = false;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(expectedEnterFunction())) {
        myIsEnterExecuted = true;

        return new TheRProcessResponse(
          "text",
          EMPTY,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command.equals(expectedExitFunction())) {
        myIsExitExecuted = true;

        return new TheRProcessResponse(
          "text",
          EMPTY,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command.equals(expectedTraceCommand())) {
        if (!(myIsEnterExecuted && myIsExitExecuted)) {
          throw new IllegalStateException("Enter and exit function should be defined");
        }

        myIsTraceExecuted = true;

        return new TheRProcessResponse(
          "text",
          RESPONSE,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      if (command.equals(expectedDebugCommand())) {
        if (!(myIsEnterExecuted && myIsExitExecuted && myIsTraceExecuted)) {
          throw new IllegalStateException("Enter and exit function should be defined. Also target function should be marked as traced");
        }

        myIsDebugExecuted = true;

        return new TheRProcessResponse(
          "text",
          EMPTY,
          TextRange.EMPTY_RANGE,
          ""
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }

    public boolean isComplete() {
      return myIsDebugExecuted;
    }
  }
}