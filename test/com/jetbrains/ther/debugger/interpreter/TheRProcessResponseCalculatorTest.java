package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.*;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.DEBUGGING_IN;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.DEBUG_AT;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseCalculator.calculate;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseCalculator.isComplete;
import static org.junit.Assert.*;

public class TheRProcessResponseCalculatorTest {

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
    check(
      "x <- function() {",
      "",
      PLUS_AND_SPACE,
      PLUS,
      ""
    );
  }

  @Test
  public void calculateJustBrowse() {
    check(
      DEBUG_COMMAND + "(x)",
      "",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      EMPTY,
      ""
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void calculateIncomplete() {
    check(
      "ls()",
      "[1] \"x\"",
      BROWSE_PREFIX,
      RESPONSE,
      "[1] \"x\""
    );
  }

  @Test
  public void calculateDebuggingIn() {
    check(
      "x()",
      TheRDebugConstants.DEBUGGING_IN + ": x()\n" +
      "debug: {\n" +
      "    on.exit(.doTrace(" + INTELLIJ_THER_X_EXIT + "(), \"on exit\"))\n" +
      "    {\n" +
      "        .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
      "        {\n" +
      "            print(\"x\")\n" +
      "        }\n" +
      "    }\n" +
      "}",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      DEBUGGING_IN,
      ""
    );
  }

  @Test
  public void calculateDebugAt() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TheRDebugConstants.DEBUG_AT + "1: x <- c(1)",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      DEBUG_AT,
      ""
    );
  }

  @Test
  public void calculateDebugAtWithResponse() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      "[1] 1 2 3\n" +
      TheRDebugConstants.DEBUG_AT + "1: x <- c(1)",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      DEBUG_AT,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateDebugAtFunction() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TheRDebugConstants.DEBUG_AT + "2: x <- function() {\n" +
      "print(\"x\")\n" +
      "}",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      DEBUG_AT,
      ""
    );
  }

  @Test
  public void calculateStartTraceBrace() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TRACING + " x() on entry \n" +
      "[1] \"enter x\"\n" +
      "debug: {\n" +
      "    print(\"x\")\n" +
      "}",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      START_TRACE_BRACE,
      ""
    );
  }

  @Test
  public void calculateStartTraceUnbrace() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TRACING + " x() on entry \n" +
      "[1] \"enter x\"\n" +
      "debug: print(\"x\")",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      START_TRACE_UNBRACE,
      ""
    );
  }

  @Test
  public void calculateContinueTrace() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TRACING + " FUN(c(-1, 0, 1)[[1L]], ...) on exit \n" +
      "[1] \"exit x\"\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[1L]], ...)\n" +
      TheRDebugConstants.DEBUGGING_IN + ": FUN(c(-1, 0, 1)[[2L]], ...)\n" +
      "debug: {\n" +
      "    on.exit(.doTrace(" + INTELLIJ_THER_X_EXIT + "(), \"on exit\"))\n" +
      "    {\n" +
      "        .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
      "        {\n" +
      "            print(\"x\")\n" +
      "        }\n" +
      "    }\n" +
      "}",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      CONTINUE_TRACE,
      ""
    );
  }

  @Test
  public void calculateContinueTraceWithResponseBefore() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      "[1] 1 2 3\n" +
      TRACING + " FUN(c(-1, 0, 1)[[1L]], ...) on exit \n" +
      "[1] \"exit x\"\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[1L]], ...)\n" +
      TheRDebugConstants.DEBUGGING_IN + ": FUN(c(-1, 0, 1)[[2L]], ...)\n" +
      "debug: {\n" +
      "    on.exit(.doTrace(" + INTELLIJ_THER_X_EXIT + "(), \"on exit\"))\n" +
      "    {\n" +
      "        .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
      "        {\n" +
      "            print(\"x\")\n" +
      "        }\n" +
      "    }\n" +
      "}",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      CONTINUE_TRACE,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateContinueTraceWithResponseAfter() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TRACING + " FUN(c(-1, 0, 1)[[1L]], ...) on exit \n" +
      "[1] \"exit x\"\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[1L]], ...)\n" +
      "[1] 1 2 3\n" +
      TheRDebugConstants.DEBUGGING_IN + ": FUN(c(-1, 0, 1)[[2L]], ...)\n" +
      "debug: {\n" +
      "    on.exit(.doTrace(" + INTELLIJ_THER_X_EXIT + "(), \"on exit\"))\n" +
      "    {\n" +
      "        .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
      "        {\n" +
      "            print(\"x\")\n" +
      "        }\n" +
      "    }\n" +
      "}",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      CONTINUE_TRACE,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateEndTrace() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TRACING + " FUN(c(-1, 0, 1)[[3L]], ...) on exit \n" +
      "[1] \"exit x\"\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      END_TRACE,
      ""
    );
  }

  @Test
  public void calculateEndTraceWithResponseBefore() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      "[1] 1 2 3\n" +
      TRACING + " FUN(c(-1, 0, 1)[[3L]], ...) on exit \n" +
      "[1] \"exit x\"\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      END_TRACE,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateEndTraceWithResponseAfter() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TRACING + " FUN(c(-1, 0, 1)[[3L]], ...) on exit \n" +
      "[1] \"exit x\"\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      "[1] 1 2 3",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      END_TRACE,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateEndTraceWithResponseBeforeAndDebugAt() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      "[1] 1 2 3\n" +
      TRACING + " FUN(c(-1, 0, 1)[[3L]], ...) on exit \n" +
      "[1] \"exit x\"\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      TheRDebugConstants.DEBUG_AT + "1: x <- c(1)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      END_TRACE,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateEndTraceWithResponseAfterAndDebugAt() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TRACING + " FUN(c(-1, 0, 1)[[3L]], ...) on exit \n" +
      "[1] \"exit x\"\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      "[1] 1 2 3\n" +
      TheRDebugConstants.DEBUG_AT + "1: x <- c(1)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      END_TRACE,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateResponseAndBrowse() {
    check(
      "ls()",
      "[1] \"x\"",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      RESPONSE,
      "[1] \"x\""
    );
  }

  private void check(@NotNull final String command,
                     @NotNull final String expectedResponse,
                     @NotNull final String tail,
                     @NotNull final TheRProcessResponseType expectedType,
                     @NotNull final String expectedOutput) {
    final TheRProcessResponse response = calculate(
      calculateFinalCommand(command, expectedResponse, tail)
    );

    assertEquals(expectedResponse, response.getText());
    assertEquals(expectedType, response.getType());
    assertEquals(expectedOutput, response.getOutputRange().substring(response.getText()));
  }

  private String calculateFinalCommand(@NotNull final String command,
                                       @NotNull final String expectedResponse,
                                       @NotNull final String tail) {
    final StringBuilder sb = new StringBuilder();

    sb.append(command);
    sb.append(TheRDebugConstants.LINE_SEPARATOR);

    if (!expectedResponse.isEmpty()) {
      sb.append(expectedResponse);
      sb.append(TheRDebugConstants.LINE_SEPARATOR);
    }

    sb.append(tail);

    return sb.toString();
  }
}
