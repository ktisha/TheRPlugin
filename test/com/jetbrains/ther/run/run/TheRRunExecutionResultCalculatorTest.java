package com.jetbrains.ther.run.run;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.PROMPT;
import static org.junit.Assert.*;

public class TheRRunExecutionResultCalculatorTest {

  @NotNull
  private static final TheRRunExecutionResultCalculator CALCULATOR = new TheRRunExecutionResultCalculator();

  @Test
  public void complete() {
    assertTrue(CALCULATOR.isComplete(PROMPT + "command\n" + PROMPT));
  }

  @Test
  public void incomplete() {
    assertFalse(CALCULATOR.isComplete(PROMPT));
  }

  @Test
  public void empty() {
    final TheRExecutionResult result = CALCULATOR.calculate(
      PROMPT + "command\n" + PROMPT,
      "error"
    );

    assertEquals("", result.getOutput());
    assertEquals(TheRExecutionResultType.RESPONSE, result.getType());
    assertEquals(TextRange.EMPTY_RANGE, result.getResultRange());
    assertEquals("error", result.getError());
  }

  @Test
  public void notEmpty() {
    final String resultOutput = "[1] \"OK\"";

    final TheRExecutionResult result = CALCULATOR.calculate(
      PROMPT + "command\n" + resultOutput + "\n" + PROMPT,
      "error"
    );

    assertEquals(resultOutput, result.getOutput());
    assertEquals(TheRExecutionResultType.RESPONSE, result.getType());
    assertEquals(TextRange.allOf(resultOutput), result.getResultRange());
    assertEquals("error", result.getError());
  }
}