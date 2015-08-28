package com.jetbrains.ther.debugger.executor;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXITING_FROM;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator.calculate;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultCalculator.isComplete;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.DEBUGGING_IN;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.DEBUG_AT;
import static org.junit.Assert.*;

public class TheRExecutionResultCalculatorTest {

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
      "    .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
      "    {\n" +
      "        print(\"x\")\n" +
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
  public void calculateDebugAtUnbrace() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      TheRDebugConstants.DEBUG + ": x <- c(1)",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      DEBUG_AT,
      ""
    );
  }

  @Test
  public void calculateDebugAtWithOutput() {
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
      "[1] \"x\"\n" +
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
      "[1] \"x\"\n" +
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
      EXITING_FROM + " FUN(c(-1, 0, 1)[[1L]], ...)\n" +
      TheRDebugConstants.DEBUGGING_IN + ": FUN(c(-1, 0, 1)[[2L]], ...)\n" +
      "debug: {\n" +
      "    .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
      "    {\n" +
      "        print(\"x\")\n" +
      "    }\n" +
      "}",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      CONTINUE_TRACE,
      ""
    );
  }

  @Test
  public void calculateContinueTraceWithOutputBefore() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      "[1] 1 2 3\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[1L]], ...)\n" +
      TheRDebugConstants.DEBUGGING_IN + ": FUN(c(-1, 0, 1)[[2L]], ...)\n" +
      "debug: {\n" +
      "    .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
      "    {\n" +
      "        print(\"x\")\n" +
      "    }\n" +
      "}",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      CONTINUE_TRACE,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateContinueTraceWithOutputAfter() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      EXITING_FROM + " FUN(c(-1, 0, 1)[[1L]], ...)\n" +
      "[1] 1 2 3\n" +
      TheRDebugConstants.DEBUGGING_IN + ": FUN(c(-1, 0, 1)[[2L]], ...)\n" +
      "debug: {\n" +
      "    .doTrace(" + INTELLIJ_THER_X_ENTER + "(), \"on entry\")\n" +
      "    {\n" +
      "        print(\"x\")\n" +
      "    }\n" +
      "}",
      BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
      CONTINUE_TRACE,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateExitingFrom() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      TheRExecutionResultType.EXITING_FROM,
      ""
    );
  }

  @Test
  public void calculateExitingFromWithOutputBefore() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      "[1] 1 2 3\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      TheRExecutionResultType.EXITING_FROM,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateExitingFromWithOutputAfter() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      "[1] 1 2 3",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      TheRExecutionResultType.EXITING_FROM,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateExitingFromWithOutputBeforeAndDebugAt() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      "[1] 1 2 3\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      TheRDebugConstants.DEBUG_AT + "1: x <- c(1)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      TheRExecutionResultType.EXITING_FROM,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateExitingFromWithOutputAfterAndDebugAt() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      "[1] 1 2 3\n" +
      TheRDebugConstants.DEBUG_AT + "1: x <- c(1)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      TheRExecutionResultType.EXITING_FROM,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateRecursiveExitingFrom() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      RECURSIVE_EXITING_FROM,
      ""
    );
  }

  @Test
  public void calculateRecursiveExitingFromWithOutputBefore() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      "[1] 1 2 3\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      RECURSIVE_EXITING_FROM,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateRecursiveExitingFromWithOutputAfter() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      "[1] 1 2 3",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      RECURSIVE_EXITING_FROM,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateRecursiveExitingFromWithOutputBeforeAndDebugAt() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      "[1] 1 2 3\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      TheRDebugConstants.DEBUG_AT + "1: x <- c(1)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      RECURSIVE_EXITING_FROM,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateRecursiveExitingFromWithOutputAfterAndDebugAt() {
    check(
      EXECUTE_AND_STEP_COMMAND,
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
      "[1] 1 2 3\n" +
      TheRDebugConstants.DEBUG_AT + "1: x <- c(1)",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      RECURSIVE_EXITING_FROM,
      "[1] 1 2 3"
    );
  }

  @Test
  public void calculateOutputAndBrowse() {
    check(
      "ls()",
      "[1] \"x\"",
      BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
      RESPONSE,
      "[1] \"x\""
    );
  }

  private void check(@NotNull final String command,
                     @NotNull final String expectedOutput,
                     @NotNull final String tail,
                     @NotNull final TheRExecutionResultType expectedType,
                     @NotNull final String expectedResult) {
    final TheRExecutionResult result = calculate(
      calculateFinalCommand(command, expectedOutput, tail),
      ""
    );

    assertEquals(expectedOutput, result.getOutput());
    assertEquals(expectedType, result.getType());
    assertEquals(expectedResult, result.getResultRange().substring(result.getOutput()));
    assertEquals("", result.getError());
  }

  private String calculateFinalCommand(@NotNull final String command,
                                       @NotNull final String expectedOutput,
                                       @NotNull final String tail) {
    final StringBuilder sb = new StringBuilder();

    sb.append(command);
    sb.append(TheRDebugConstants.LINE_SEPARATOR);

    if (!expectedOutput.isEmpty()) {
      sb.append(expectedOutput);
      sb.append(TheRDebugConstants.LINE_SEPARATOR);
    }

    sb.append(tail);

    return sb.toString();
  }
}
