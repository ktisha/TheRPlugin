package com.jetbrains.ther.debugger.evaluator;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.mock.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.ENVIRONMENT;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRDebuggerEvaluatorImplTest {

  @Test
  public void unexpectedResultType() {
    final String expression = "def <- function() {";

    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      "",
      PLUS,
      TextRange.EMPTY_RANGE,
      "error"
    );
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheROutputReceiver(),
      handler,
      1
    );

    final TheRDebuggerEvaluatorErrorReceiver receiver = new TheRDebuggerEvaluatorErrorReceiver();

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, executor.getCounter());
    assertEquals(1, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
    assertEquals(1, receiver.getCounter());
  }

  @Test
  public void errorDuringExecution() {
    final String expression = "abc";

    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      "",
      EMPTY,
      TextRange.EMPTY_RANGE,
      "error"
    );
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheROutputReceiver(),
      handler,
      1
    );

    final TheRDebuggerEvaluatorErrorReceiver receiver = new TheRDebuggerEvaluatorErrorReceiver();

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, executor.getCounter());
    assertEquals(1, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
    assertEquals(1, receiver.getCounter());
  }

  @Test
  public void exceptionDuringExecution() {
    final String expression = "def";

    final ExceptionDuringExecutionTheRExecutor executor = new ExceptionDuringExecutionTheRExecutor();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheROutputReceiver(),
      handler,
      1
    );

    final TheRDebuggerEvaluatorErrorReceiver receiver = new TheRDebuggerEvaluatorErrorReceiver();

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, executor.getCounter());
    assertEquals(1, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
    assertEquals(1, receiver.getCounter());
  }

  @Test
  public void expression() {
    final String expression = "def(c(1:5))";
    final String output = "[1] 1 2 3";
    final String error = "error";

    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      output,
      RESPONSE,
      TextRange.allOf(output),
      error
    );

    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null, null),
      outputReceiver,
      handler,
      1
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(output);

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, executor.getCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Collections.singletonList(error), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(1, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
  }

  @Test
  public void inDebugExpression() {
    final String expression = "def(c(1:5))";
    final String output = "[1] 1 2 3";

    final InDebugTheRExecutor executor = new InDebugTheRExecutor();
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null, null),
      outputReceiver,
      handler,
      1
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(output);

    evaluator.evalExpression(expression, receiver);

    assertEquals(2, executor.getCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Arrays.asList("abc", "def"), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(1, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
  }

  @Test
  public void innerFunctionValue() {
    final String expression = "def";
    final String error = "error";

    final String output = "function(x) {\n" +
                          "    x ^ 2\n" +
                          "}\n" +
                          "<" + ENVIRONMENT + ": 0xfffffff>";

    final String result = "function(x) {\n" +
                          "    x ^ 2\n" +
                          "}";

    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      output,
      RESPONSE,
      TextRange.allOf(output),
      error
    );

    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      executor,
      new MockTheRFunctionDebuggerFactory(null, null),
      outputReceiver,
      handler,
      1
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(result);

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, executor.getCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Collections.singletonList(error), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(1, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
  }

  @Test
  public void function() {
    final String expression = "def(c(1:5))";
    final String error = "error";
    final String result = "[1] 1 2 3";

    final AlwaysSameResultTheRExecutor executor = new AlwaysSameResultTheRExecutor(
      TheRDebugConstants.DEBUGGING_IN + ": " + expression,
      DEBUGGING_IN,
      TextRange.EMPTY_RANGE,
      error
    );

    final MyFunctionDebugger debugger = new MyFunctionDebugger();
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(debugger, null);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      executor,
      factory,
      outputReceiver,
      handler,
      1
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(result);

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, executor.getCounter());
    assertEquals(2, debugger.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(1, factory.getNotMainCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Collections.singletonList(error), outputReceiver.getErrors());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(1, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
  }

  private static class MockTheRExpressionHandler extends IllegalTheRExpressionHandler {

    @Nullable
    private String myLastExpression = null;

    private int myCounter = 0;

    @NotNull
    @Override
    public String handle(final int frameNumber, @NotNull final String expression) {
      myCounter += frameNumber;
      myLastExpression = expression;

      return expression;
    }
  }

  private static class ExceptionDuringExecutionTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      throw new TheRDebuggerException("");
    }
  }

  private static class InDebugTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT + "2: " + TheRDebugConstants.SYS_FRAME_COMMAND + "(0)$abc",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "abc"
        );
      }

      if (getCounter() == 2) {
        return new TheRExecutionResult(
          "[1] 1 2 3",
          RESPONSE,
          TextRange.allOf("[1] 1 2 3"),
          "def"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class MyFunctionDebugger extends MockTheRFunctionDebugger {

    public MyFunctionDebugger() {
      super("def", 2);
    }

    @NotNull
    @Override
    public String getResult() {
      return "[1] 1 2 3";
    }
  }
}