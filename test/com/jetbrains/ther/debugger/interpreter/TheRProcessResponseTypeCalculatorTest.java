package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseTypeCalculator.calculate;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseTypeCalculator.isComplete;
import static org.junit.Assert.*;

public class TheRProcessResponseTypeCalculatorTest {

  @NotNull
  private static final String INTELLIJ_THER_X_EXIT = SERVICE_FUNCTION_PREFIX + "x" + SERVICE_EXIT_FUNCTION_SUFFIX;

  @NotNull
  private static final String INTELLIJ_THER_X_ENTER = SERVICE_FUNCTION_PREFIX + "x" + SERVICE_ENTER_FUNCTION_SUFFIX;

  @Test
  public void completePlus() {
    assertTrue(isComplete("x <- function() {\n" + PLUS_AND_SPACE));
  }

  @Test
  public void completeBrowser() {
    assertTrue(isComplete("ls()\n[1] \"x\"\n" + BROWSE_PREFIX + "1" + BROWSE_SUFFIX));
  }

  @Test
  public void completeIncomplete() {
    assertFalse(isComplete("ls()\n[1] \"x\"\n" + BROWSE_PREFIX));
  }

  @Test
  public void calculatePlus() {
    assertEquals(TheRProcessResponseType.PLUS, calculate(PLUS_AND_SPACE));
  }

  @Test
  public void calculateJustBrowse() {
    assertEquals(TheRProcessResponseType.JUST_BROWSE, calculate(BROWSE_PREFIX + "1" + BROWSE_SUFFIX));
  }

  @Test
  public void calculateIncomplete() {
    assertNull(calculate("ls()\n[1] \"x\"\n" + BROWSE_PREFIX));
  }

  @Test
  public void calculateDebuggingIn() {
    assertEquals(
      TheRProcessResponseType.DEBUGGING_IN,
      calculate(
        DEBUGGING_IN + ": x()\n" +
        "debug: {\n" +
        "    on.exit(.doTrace(" + INTELLIJ_THER_X_EXIT + "(), \"on exit\"))\n" +
        "    {\n" +
        "        .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
        "        {\n" +
        "            print(\"x\")\n" +
        "        }\n" +
        "    }\n" +
        "}\n" +
        BROWSE_PREFIX + "3" + BROWSE_SUFFIX
      )
    );
  }

  @Test
  public void calculateDebugAt() {
    assertEquals(
      TheRProcessResponseType.DEBUG_AT,
      calculate(
        TheRDebugConstants.DEBUG_AT + "1: x <- c(1)\n" +
        BROWSE_PREFIX + "3" + BROWSE_SUFFIX
      )
    );
  }

  @Test
  public void calculateDebugAtWithResponse() {
    assertEquals(
      TheRProcessResponseType.DEBUG_AT,
      calculate(
        "[1] 1 2 3\n" +
        TheRDebugConstants.DEBUG_AT + "1: x <- c(1)\n" +
        BROWSE_PREFIX + "3" + BROWSE_SUFFIX
      )
    );
  }

  @Test
  public void calculateStartTrace() {
    assertEquals(
      TheRProcessResponseType.START_TRACE,
      calculate(
        TRACING + " x() on entry \n" +
        "[1] \"enter x\"\n" +
        "debug: {\n" +
        "    print(\"x\")\n" +
        "}\n" +
        BROWSE_PREFIX + "3" + BROWSE_SUFFIX
      )
    );
  }

  @Test
  public void calculateContinueTrace() {
    assertEquals(
      TheRProcessResponseType.CONTINUE_TRACE,
      calculate(
        TRACING + " FUN(c(-1, 0, 1)[[1L]], ...) on exit \n" +
        "[1] \"exit x\"\n" +
        "exiting from: FUN(c(-1, 0, 1)[[1L]], ...)\n" +
        DEBUGGING_IN + ": FUN(c(-1, 0, 1)[[2L]], ...)\n" +
        "debug: {\n" +
        "    on.exit(.doTrace(" + INTELLIJ_THER_X_EXIT + "(), \"on exit\"))\n" +
        "    {\n" +
        "        .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
        "        {\n" +
        "            print(\"x\")\n" +
        "        }\n" +
        "    }\n" +
        "}\n" +
        BROWSE_PREFIX + "3" + BROWSE_SUFFIX
      )
    );
  }

  @Test
  public void calculateContinueTraceWithResponse() {
    assertEquals(
      TheRProcessResponseType.CONTINUE_TRACE,
      calculate(
        "[1] 1 2 3\n" +
        TRACING + " FUN(c(-1, 0, 1)[[1L]], ...) on exit \n" +
        "[1] \"exit x\"\n" +
        "exiting from: FUN(c(-1, 0, 1)[[1L]], ...)\n" +
        DEBUGGING_IN + ": FUN(c(-1, 0, 1)[[2L]], ...)\n" +
        "debug: {\n" +
        "    on.exit(.doTrace(" + INTELLIJ_THER_X_EXIT + "(), \"on exit\"))\n" +
        "    {\n" +
        "        .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
        "        {\n" +
        "            print(\"x\")\n" +
        "        }\n" +
        "    }\n" +
        "}\n" +
        BROWSE_PREFIX + "3" + BROWSE_SUFFIX
      )
    );
  }

  @Test
  public void calculateEndTrace() {
    assertEquals(
      TheRProcessResponseType.END_TRACE,
      calculate(
        TRACING + " FUN(c(-1, 0, 1)[[3L]], ...) on exit \n" +
        "[1] \"exit x\"\n" +
        "exiting from: FUN(c(-1, 0, 1)[[3L]], ...)\n" +
        BROWSE_PREFIX + "1" + BROWSE_SUFFIX
      )
    );
  }

  @Test
  public void calculateEndTraceWithResponse() {
    assertEquals(
      TheRProcessResponseType.END_TRACE,
      calculate(
        "[1] 1 2 3\n" +
        TRACING + " FUN(c(-1, 0, 1)[[3L]], ...) on exit \n" +
        "[1] \"exit x\"\n" +
        "exiting from: FUN(c(-1, 0, 1)[[3L]], ...)\n" +
        BROWSE_PREFIX + "1" + BROWSE_SUFFIX
      )
    );
  }

  @Test
  public void calculateResponseAndBrowse() {
    assertEquals(
      TheRProcessResponseType.RESPONSE_AND_BROWSE,
      calculate(
        "[1] \"x\"\n" +
        BROWSE_PREFIX + "1" + BROWSE_SUFFIX
      )
    );
  }

  @Test
  public void calculateOffset() {
    assertNull(calculate(BROWSE_PREFIX + "1" + BROWSE_SUFFIX, BROWSE_PREFIX.length()));
  }
}
