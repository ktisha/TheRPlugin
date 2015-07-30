package com.jetbrains.ther.debugger.evaluator;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import com.jetbrains.ther.debugger.mock.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRDebuggerEvaluatorImplTest {

  @Test
  public void unexpectedResponseType() {
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      "",
      TheRProcessResponseType.PLUS,
      TextRange.EMPTY_RANGE,
      ""
    );

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheROutputReceiver()
    );

    final TheRDebuggerEvaluatorErrorReceiver receiver = new TheRDebuggerEvaluatorErrorReceiver();

    evaluator.evalExpression("def <- function() {", receiver);

    assertEquals(1, process.getCounter());
    assertEquals(1, receiver.getCounter());
  }

  @Test
  public void errorDuringExecution() {
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      "",
      TheRProcessResponseType.EMPTY,
      TextRange.EMPTY_RANGE,
      "error"
    );

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheROutputReceiver()
    );

    final TheRDebuggerEvaluatorErrorReceiver receiver = new TheRDebuggerEvaluatorErrorReceiver();

    evaluator.evalExpression("abc", receiver);

    assertEquals(1, process.getCounter());
    assertEquals(1, receiver.getCounter());
  }

  @Test
  public void exceptionDuringExecution() {
    final ExceptionDuringExecutionTheRProcess process = new ExceptionDuringExecutionTheRProcess();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheROutputReceiver()
    );

    final TheRDebuggerEvaluatorErrorReceiver receiver = new TheRDebuggerEvaluatorErrorReceiver();

    evaluator.evalExpression("def", receiver);

    assertEquals(1, process.getCounter());
    assertEquals(1, receiver.getCounter());
  }

  @Test
  public void function() {
    final String text = "[1] 1 2 3";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      text,
      TheRProcessResponseType.RESPONSE,
      TextRange.allOf(text),
      "abc"
    );

    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      outputReceiver
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver(text);

    evaluator.evalExpression("def(c(1:5))", receiver);

    assertEquals(1, process.getCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Collections.singletonList("abc"), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
  }

  @Test
  public void debuggedFunction() {
    final String command = "def(c(1:5))";

    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(
      TheRDebugConstants.DEBUGGING_IN + ": " + command,
      TheRProcessResponseType.DEBUGGING_IN,
      TextRange.EMPTY_RANGE,
      "abc"
    );

    final Stack1TheRFunctionDebugger functionDebugger = new Stack1TheRFunctionDebugger();
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(functionDebugger, null);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      debuggerFactory,
      outputReceiver
    );

    final TheRDebuggerEvaluatorReceiver receiver = new TheRDebuggerEvaluatorReceiver("[1] 1 2 3");

    evaluator.evalExpression(command, receiver);

    assertEquals(1, process.getCounter());
    assertEquals(1, debuggerFactory.getNotMainCounter());
    assertEquals(0, debuggerFactory.getMainCounter());
    assertEquals(1, functionDebugger.getCounter());
    assertEquals(1, receiver.getCounter());
    assertEquals(Collections.singletonList("abc"), outputReceiver.getErrors());
    assertTrue(outputReceiver.getOutputs().isEmpty());
  }

  private static class ExceptionDuringExecutionTheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      myCounter++;

      throw new TheRDebuggerException("");
    }

    @Override
    public void stop() {
    }

    public int getCounter() {
      return myCounter;
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
}