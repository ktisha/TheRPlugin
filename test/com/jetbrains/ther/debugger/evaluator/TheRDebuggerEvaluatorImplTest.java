package com.jetbrains.ther.debugger.evaluator;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.mock.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.ENVIRONMENT;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRDebuggerEvaluatorImplTest {

  @Test
  public void unexpectedResponseType() {
    final String expression = "def <- function() {";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      "",
      PLUS,
      TextRange.EMPTY_RANGE,
      "error"
    );
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheROutputReceiver(),
      handler,
      0
    );

    final TheRDebuggerEvaluatorErrorReceiver receiver = new TheRDebuggerEvaluatorErrorReceiver();

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(0, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
    assertEquals(1, receiver.getCounter());
  }

  @Test
  public void errorDuringExecution() {
    final String expression = "abc";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      "",
      EMPTY,
      TextRange.EMPTY_RANGE,
      "error"
    );
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheROutputReceiver(),
      handler,
      0
    );

    final TheRDebuggerEvaluatorErrorReceiver receiver = new TheRDebuggerEvaluatorErrorReceiver();

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(0, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
    assertEquals(1, receiver.getCounter());
  }

  @Test
  public void exceptionDuringExecution() {
    final String expression = "def";

    final ExceptionDuringExecutionTheRProcess process = new ExceptionDuringExecutionTheRProcess();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheROutputReceiver(),
      handler,
      0
    );

    final TheRDebuggerEvaluatorErrorReceiver receiver = new TheRDebuggerEvaluatorErrorReceiver();

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(0, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
    assertEquals(1, receiver.getCounter());
  }

  @Test
  public void expression() {
    final String expression = "def(c(1:5))";
    final String output = "[1] 1 2 3";
    final String error = "error";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      output,
      RESPONSE,
      TextRange.allOf(output),
      error
    );

    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      outputReceiver,
      handler,
      0
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(output);

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Collections.singletonList(error), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(0, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
  }

  @Test
  public void inDebugExpression() {
    final String expression = "def(c(1:5))";
    final String output = "[1] 1 2 3";

    final InDebugTheRProcess process = new InDebugTheRProcess();
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      outputReceiver,
      handler,
      0
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(output);

    evaluator.evalExpression(expression, receiver);

    assertEquals(2, process.getCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Arrays.asList("abc", "def"), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(0, handler.myCounter);
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

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      output,
      RESPONSE,
      TextRange.allOf(output),
      error
    );

    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      outputReceiver,
      handler,
      0
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(result);

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Collections.singletonList(error), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(0, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
  }

  @Test
  public void stack1() {
    /*
    def() {
      instruction1
    }
    */

    final String expression = "def(c(1:5))";
    final String error = "error";
    final String result = "[1] 1 2 3";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      TheRDebugConstants.DEBUGGING_IN + ": " + expression,
      DEBUGGING_IN,
      TextRange.EMPTY_RANGE,
      error
    );

    final Stack1TheRFunctionDebugger functionDebugger = new Stack1TheRFunctionDebugger();
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(functionDebugger, null);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      debuggerFactory,
      outputReceiver,
      handler,
      0
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(result);

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(1, debuggerFactory.getNotMainCounter());
    assertEquals(0, debuggerFactory.getMainCounter());
    assertEquals(1, functionDebugger.getCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Collections.singletonList(error), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(0, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
  }

  @Test
  public void stack21() {
    /*
    def() {
      instruction1
      abc() {
        instruction1
        instruction2
      }
      instruction2
    }
    */

    final String expression = "def(c(1:5))";
    final String error = "error";
    final String result = "[1] 1 2 3";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      TheRDebugConstants.DEBUGGING_IN + ": " + expression,
      DEBUGGING_IN,
      TextRange.EMPTY_RANGE,
      error
    );

    final MockTheRFunctionDebugger secondFunctionDebugger = new MockTheRFunctionDebugger("abc", 2);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack211TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger, null);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      debuggerFactory,
      outputReceiver,
      handler,
      0
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(result);

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getMainCounter());
    assertEquals(1, debuggerFactory.getNotMainCounter());
    assertEquals(Collections.singletonList(error), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(0, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
  }

  @Test
  public void stack22() {
    /*
    def() {
      instruction1
      abc() {
        instruction1
        instruction2
      }
    }
    */

    final String expression = "def(c(1:5))";
    final String error = "error";
    final String result = "[1] 1 2 3";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      TheRDebugConstants.DEBUGGING_IN + ": " + expression,
      DEBUGGING_IN,
      TextRange.EMPTY_RANGE,
      error
    );

    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack222TheRFunctionDebugger();
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack221TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger, null);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      debuggerFactory,
      outputReceiver,
      handler,
      0
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(result);

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getMainCounter());
    assertEquals(1, debuggerFactory.getNotMainCounter());
    assertEquals(Collections.singletonList(error), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(0, handler.myCounter);
    assertEquals(expression, handler.myLastExpression);
  }

  @Test
  public void stack3() {
    /*
    def() {
      instruction1
      abc() {
        instruction1
        ghi() {
          instruction1
          instruction2
        }
      }
      instruction2
    }
    */

    final String expression = "def(c(1:5))";
    final String error = "error";
    final String result = "[1] 1 2 3";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      TheRDebugConstants.DEBUGGING_IN + ": " + expression,
      DEBUGGING_IN,
      TextRange.EMPTY_RANGE,
      error
    );

    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack33TheRFunctionDebugger();
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack32TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack31TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger, null);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler handler = new MockTheRExpressionHandler();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      debuggerFactory,
      outputReceiver,
      handler,
      0
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(result);

    evaluator.evalExpression(expression, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getMainCounter());
    assertEquals(1, debuggerFactory.getNotMainCounter());
    assertEquals(Collections.singletonList(error), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
    assertEquals(0, handler.myCounter);
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

  private static class ExceptionDuringExecutionTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      throw new TheRDebuggerException("");
    }
  }

  private static class InDebugTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "2: " + TheRDebugConstants.SYS_FRAME_COMMAND + "(0)$abc",
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "abc"
        );
      }

      if (getCounter() == 2) {
        return new TheRProcessResponse(
          "[1] 1 2 3",
          RESPONSE,
          TextRange.allOf("[1] 1 2 3"),
          "def"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class Stack1TheRFunctionDebugger extends MockTheRFunctionDebugger {

    public Stack1TheRFunctionDebugger() {
      super("", 1);
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      throw new IllegalStateException("GetLocation shouldn't be called");
    }

    @NotNull
    @Override
    public String getResult() {
      return "[1] 1 2 3";
    }
  }

  private static class Stack211TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack211TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger debugger) {
      super("def", 3);

      myNextFunctionDebugger = debugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      throw new IllegalStateException("GetLocation shouldn't be called");
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        getHandler().appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      return "[1] 1 2 3";
    }
  }

  private static class Stack221TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack221TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger debugger) {
      super("def", 2);

      myNextFunctionDebugger = debugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      throw new IllegalStateException("GetLocation shouldn't be called");
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        myNextFunctionDebugger.setHandler(getHandler());
        getHandler().appendDebugger(myNextFunctionDebugger);
      }
    }
  }

  private static class Stack222TheRFunctionDebugger extends MockTheRFunctionDebugger {

    public Stack222TheRFunctionDebugger() {
      super("abc", 2);
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      throw new IllegalStateException("GetLocation shouldn't be called");
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        getHandler().setDropFrames(2);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      return "[1] 1 2 3";
    }
  }

  private static class Stack31TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack31TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super("def", 3);

      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        myNextFunctionDebugger.setHandler(getHandler());
        getHandler().appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      return "[1] 1 2 3";
    }
  }

  private static class Stack32TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack32TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super("abc", 2);

      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        myNextFunctionDebugger.setHandler(getHandler());
        getHandler().appendDebugger(myNextFunctionDebugger);
      }
    }
  }

  private static class Stack33TheRFunctionDebugger extends MockTheRFunctionDebugger {

    public Stack33TheRFunctionDebugger() {
      super("ghi", 2);
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        getHandler().setDropFrames(2);
      }
    }
  }
}