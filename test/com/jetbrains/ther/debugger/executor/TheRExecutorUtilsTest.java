package com.jetbrains.ther.debugger.executor;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.mock.AlwaysSameResultTheRExecutor;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import org.junit.Test;

import java.util.Collections;

import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.PLUS;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.RESPONSE;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;
import static org.junit.Assert.assertEquals;

public class TheRExecutorUtilsTest {

  @Test(expected = TheRDebuggerException.class)
  public void invalidCommandExecuting() throws TheRDebuggerException {
    final String output = "abc";
    final TheRExecutionResultType type = RESPONSE;
    final TextRange resultRange = TextRange.allOf(output);
    final String error = "";

    final TheRExecutor executor = new AlwaysSameResultTheRExecutor(output, type, resultRange, error);

    execute(executor, "def", PLUS);
  }

  @Test
  public void correctCommandExecuting() throws TheRDebuggerException {
    final String output = "abc";
    final TheRExecutionResultType type = RESPONSE;
    final TextRange resultRange = TextRange.allOf(output);
    final String error = "";

    final TheRExecutor executor = new AlwaysSameResultTheRExecutor(output, type, resultRange, error);

    final TheRExecutionResult result = execute(executor, "def", RESPONSE);

    assertEquals(output, result.getOutput());
    assertEquals(RESPONSE, result.getType());
    assertEquals(resultRange, result.getResultRange());
    assertEquals(error, result.getError());
  }

  @Test
  public void errorCommandExecuting1() throws TheRDebuggerException {
    final String output = "abc";
    final TheRExecutionResultType type = RESPONSE;
    final TextRange resultRange = TextRange.allOf(output);
    final String error = "error";

    final TheRExecutor executor = new AlwaysSameResultTheRExecutor(output, type, resultRange, error);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final String result = execute(executor, "def", RESPONSE, receiver);

    assertEquals(output, result);
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(Collections.singletonList(error), receiver.getErrors());
  }

  @Test
  public void errorCommandExecuting2() throws TheRDebuggerException {
    final String output = "abc";
    final TheRExecutionResultType type = RESPONSE;
    final TextRange resultRange = TextRange.allOf(output);
    final String error = "error";

    final TheRExecutor executor = new AlwaysSameResultTheRExecutor(output, type, resultRange, error);
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRExecutionResult result = execute(executor, "def", receiver);

    assertEquals(output, result.getOutput());
    assertEquals(RESPONSE, result.getType());
    assertEquals(resultRange, result.getResultRange());
    assertEquals(error, result.getError());
    assertEquals(Collections.emptyList(), receiver.getOutputs());
    assertEquals(Collections.singletonList(error), receiver.getErrors());
  }
}