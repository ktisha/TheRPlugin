package com.jetbrains.ther.debugger.utils;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static org.junit.Assert.assertEquals;

public class TheRLoadableVarHandlerUtilsTest {

  @Test
  public void traceAndDebug() throws Exception {
    TheRLoadableVarHandlerUtils.traceAndDebug(
      new TraceAndDebugTheRProcess(),
      "abc"
    );
  }

  @Test
  public void enterFunction() throws Exception {
    assertEquals(
      expectedEnterFunction(),
      TheRLoadableVarHandlerUtils.enterFunction("abc")
    );
  }

  @Test
  public void exitFunction() throws Exception {
    assertEquals(
      expectedExitFunction(),
      TheRLoadableVarHandlerUtils.exitFunction("abc")
    );
  }

  @Test
  public void traceCommand() throws Exception {
    assertEquals(
      expectedTraceCommand(),
      TheRLoadableVarHandlerUtils.traceCommand("abc")
    );
  }

  @Test
  public void debugCommand() throws Exception {
    assertEquals(
      expectedDebugCommand(),
      TheRLoadableVarHandlerUtils.debugCommand("abc")
    );
  }

  @NotNull
  private static String expectedEnterFunction() {
    return enterFunctionName() + " <- function() { print(\"enter abc\") }";
  }

  @NotNull
  private static String expectedExitFunction() {
    return exitFunctionName() + " <- function() { print(\"exit abc\") }";
  }

  @NotNull
  private static String expectedTraceCommand() {
    return "trace(abc, " + enterFunctionName() + ", exit = " + exitFunctionName() + ")";
  }

  @NotNull
  private static String expectedDebugCommand() {
    return "debug(abc)";
  }

  @NotNull
  private static String enterFunctionName() {
    return SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_ENTER_FUNCTION_SUFFIX;
  }

  @NotNull
  private static String exitFunctionName() {
    return SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_EXIT_FUNCTION_SUFFIX;
  }

  private static class TraceAndDebugTheRProcess extends TheRProcess {

    private boolean myIsEnterExecuted = false;
    private boolean myIsExitExecuted = false;
    private boolean myIsTraceExecuted = false;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
      if (command.equals(expectedEnterFunction())) {
        myIsEnterExecuted = true;

        return new TheRProcessResponse("text", TheRProcessResponseType.JUST_BROWSE);
      }

      if (command.equals(expectedExitFunction())) {
        myIsExitExecuted = true;

        return new TheRProcessResponse("text", TheRProcessResponseType.JUST_BROWSE);
      }

      if (command.equals(expectedTraceCommand())) {
        if (!(myIsEnterExecuted && myIsExitExecuted)) {
          throw new IllegalStateException("Enter and exit function should be defined");
        }

        myIsTraceExecuted = true;

        return new TheRProcessResponse("text", TheRProcessResponseType.RESPONSE_AND_BROWSE);
      }

      if (command.equals(expectedDebugCommand())) {
        if (!(myIsEnterExecuted && myIsExitExecuted && myIsTraceExecuted)) {
          throw new IllegalStateException("Enter and exit function should be defined. Also target function should be marked as traced");
        }

        return new TheRProcessResponse("text", TheRProcessResponseType.JUST_BROWSE);
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }
  }
}