package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.*;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.mock.*;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRDebuggerEvaluatorImplTest {

  @Test
  public void evalTrueCondition() {
    final String text = "[1] TRUE";

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      new AlwaysSameResponseTheRProcess(
        text,
        TheRProcessResponseType.RESPONSE,
        TextRange.allOf(text)
      ),
      new IllegalTheRFunctionDebuggerFactory(),
      new IllegalTheRFunctionDebuggerHandler(),
      new IllegalTheRFunctionResolver(),
      new IllegalTheRLoadableVarHandler(),
      new TheRLocation(
        new TheRFunction(
          Collections.singletonList("abc")
        ),
        10
      )
    );

    final MockConditionReceiver receiver = new MockConditionReceiver(true);

    evaluator.evalCondition("5 > 4", receiver);

    assertTrue(receiver.isResultReceived());
  }

  @Test
  public void evalFalseCondition() {
    final String text = "[1] FALSE";

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      new AlwaysSameResponseTheRProcess(
        text,
        TheRProcessResponseType.RESPONSE,
        TextRange.allOf(text)
      ),
      new IllegalTheRFunctionDebuggerFactory(),
      new IllegalTheRFunctionDebuggerHandler(),
      new IllegalTheRFunctionResolver(),
      new IllegalTheRLoadableVarHandler(),
      new TheRLocation(
        new TheRFunction(
          Collections.singletonList("abc")
        ),
        10
      )
    );

    final MockConditionReceiver receiver = new MockConditionReceiver(false);

    evaluator.evalCondition("5 < 4", receiver);

    assertTrue(receiver.isResultReceived());
  }

  @Test
  public void evalInvalidResponseFormatCondition() {
    final String text = "ghi";

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      new AlwaysSameResponseTheRProcess(
        text,
        TheRProcessResponseType.RESPONSE,
        TextRange.allOf(text)
      ),
      new IllegalTheRFunctionDebuggerFactory(),
      new IllegalTheRFunctionDebuggerHandler(),
      new IllegalTheRFunctionResolver(),
      new IllegalTheRLoadableVarHandler(),
      new TheRLocation(
        new TheRFunction(
          Collections.singletonList("abc")
        ),
        10
      )
    );

    final MockConditionReceiver receiver = new MockConditionReceiver(false);

    evaluator.evalCondition("def", receiver);

    assertTrue(receiver.isResultReceived());
  }

  @Test
  public void evalUnexpectedResponseType() {
    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      new AlwaysSameResponseTheRProcess(
        "",
        TheRProcessResponseType.PLUS,
        TextRange.EMPTY_RANGE
      ),
      new IllegalTheRFunctionDebuggerFactory(),
      new IllegalTheRFunctionDebuggerHandler(),
      new IllegalTheRFunctionResolver(),
      new IllegalTheRLoadableVarHandler(),
      new TheRLocation(
        new TheRFunction(
          Collections.singletonList("abc")
        ),
        10
      )
    );

    final ErrorExpressionReceiver receiver = new ErrorExpressionReceiver();

    evaluator.evalExpression("def <- function() {", receiver);

    assertTrue(receiver.isErrorReceived());
  }

  @Test
  public void evalExceptionDuringExecution() {
    final IOExceptionTheRProcess process = new IOExceptionTheRProcess();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      process,
      new IllegalTheRFunctionDebuggerFactory(),
      new IllegalTheRFunctionDebuggerHandler(),
      new IllegalTheRFunctionResolver(),
      new IllegalTheRLoadableVarHandler(),
      new TheRLocation(
        new TheRFunction(
          Collections.singletonList("abc")
        ),
        10
      )
    );

    final ErrorExpressionReceiver receiver = new ErrorExpressionReceiver();

    evaluator.evalExpression("def", receiver);

    assertEquals(1, process.getExecuteCalled());
    assertTrue(receiver.isErrorReceived());
  }

  @Test
  public void evalFunction() {
    final String text = "[1] 1 2 3";

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      new AlwaysSameResponseTheRProcess(
        text,
        TheRProcessResponseType.RESPONSE,
        TextRange.allOf(text)
      ),
      new IllegalTheRFunctionDebuggerFactory(),
      new IllegalTheRFunctionDebuggerHandler(),
      new IllegalTheRFunctionResolver(),
      new IllegalTheRLoadableVarHandler(),
      new TheRLocation(
        new TheRFunction(
          Collections.singletonList("abc")
        ),
        10
      )
    );

    final MockExpressionReceiver receiver = new MockExpressionReceiver(text);

    evaluator.evalExpression("def(c(1:5))", receiver);

    assertTrue(receiver.isResultReceived());
  }

  @Test
  public void evalDebuggedFunction() {
    final String command = "def(c(1:5))";

    final DebuggedTheRFunctionDebuggerFactory debuggerFactory = new DebuggedTheRFunctionDebuggerFactory();

    final TheRDebuggerEvaluatorImpl evaluator = new TheRDebuggerEvaluatorImpl(
      new AlwaysSameResponseTheRProcess(
        "debugging in: " + command,
        TheRProcessResponseType.DEBUGGING_IN,
        TextRange.EMPTY_RANGE
      ),
      debuggerFactory,
      new IllegalTheRFunctionDebuggerHandler(),
      new IllegalTheRFunctionResolver(),
      new IllegalTheRLoadableVarHandler(),
      new TheRLocation(
        new TheRFunction(
          Collections.singletonList("abc")
        ),
        10
      )
    );

    final MockExpressionReceiver receiver = new MockExpressionReceiver("[1] 1 2 3");

    evaluator.evalExpression(command, receiver);

    assertEquals(1, debuggerFactory.getNotMainCalled());
    assertTrue(receiver.isResultReceived());
  }

  private static class IOExceptionTheRProcess extends TheRProcess {

    private int myExecuteCalled = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
      myExecuteCalled++;

      throw new IOException();
    }

    @Override
    public void stop() {
    }

    public int getExecuteCalled() {
      return myExecuteCalled;
    }
  }

  private static class DebuggedTheRFunctionDebuggerFactory implements TheRFunctionDebuggerFactory {

    private int myNotMainCalled = 0;

    @NotNull
    @Override
    public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                           @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                           @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                           @NotNull final TheRFunctionResolver functionResolver,
                                                           @NotNull final TheRLoadableVarHandler varHandler,
                                                           @NotNull final TheRLocation prevLocation)
      throws IOException, InterruptedException {
      myNotMainCalled++;

      return new TheRFunctionDebugger() {

        @NotNull
        @Override
        public TheRLocation getLocation() {
          throw new IllegalStateException("GetLocation shouldn't be called");
        }

        @NotNull
        @Override
        public List<TheRVar> getVars() {
          throw new IllegalStateException("GetVars shouldn't be called");
        }

        @Override
        public boolean hasNext() {
          return false;
        }

        @Override
        public void advance() throws IOException, InterruptedException {
          throw new IllegalStateException("Advance shouldn't be called");
        }

        @NotNull
        @Override
        public String getResult() {
          return "[1] 1 2 3";
        }
      };
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                        @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                        @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                        @NotNull final TheRFunctionResolver functionResolver,
                                                        @NotNull final TheRLoadableVarHandler varHandler,
                                                        @NotNull final TheRScriptReader scriptReader) {
      throw new IllegalStateException("GetMainFunctionDebugger shouldn't be called");
    }


    public int getNotMainCalled() {
      return myNotMainCalled;
    }
  }
}