package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRScriptLine;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluatorFactory;
import com.jetbrains.ther.debugger.evaluator.TheRExpressionHandler;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.frame.*;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.mock.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.MAIN_FUNCTION_NAME;
import static org.junit.Assert.*;

public class TheRDebuggerTest {

  @Test
  public void stack1() throws TheRDebuggerException {
    // just `main`

    /*
    instruction1
    instruction2
    */

    final MockTheRExecutor executor = new MockTheRExecutor();
    final MockTheRFunctionDebugger functionDebugger = new MockTheRFunctionDebugger(MAIN_FUNCTION_NAME, 2);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, functionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      receiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, functionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(0, executor.getCounter());
    assertEquals(1, functionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(0, executor.getCounter());
    assertEquals(2, functionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertEquals(0, executor.getCounter());
    assertEquals(2, functionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
  }

  @Test
  public void stack2() throws TheRDebuggerException {
    // `main` and function

    /*
    instruction1
    abc() {
      instruction1
      instruction2
    }
    instruction2
    */

    final MockTheRExecutor executor = new MockTheRExecutor();
    final MockTheRFunctionDebugger secondFunctionDebugger = new MockTheRFunctionDebugger("abc", 2);
    final Stack21TheRFunctionDebugger firstFunctionDebugger = new Stack21TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      receiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(0, executor.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertEquals(1, executor.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());
  }

  @Test
  public void stack31() throws TheRDebuggerException {
    // `main`, function `a`, function `b` with `debug at` at the end

    /*
    instruction1
    abc() {
      instruction1
      def() {
        instruction1
        instruction2
      }
      instruction2
     }
     instruction2
     */

    final MockTheRExecutor executor = new MockTheRExecutor();
    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack313TheRFunctionDebugger();
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack312TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack311TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      receiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(0, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(1, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 5), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertEquals(2, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());
  }

  @Test
  public void stack32() throws TheRDebuggerException {
    // `main`, function `a` and function `b` with recursive return

    /*
    instruction1
    abc() {
      instruction1
      def() {
        instruction1
        instruction2
      }
    }
    instruction2
    */

    final MockTheRExecutor executor = new MockTheRExecutor();
    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack323TheRFunctionDebugger();
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack322TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack321TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      receiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(0, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(1, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertEquals(2, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());
  }

  @Test
  public void stack4() throws TheRDebuggerException {
    // `main`, function `a`, function `b`, function `c` - recursive return from `c` and `b` with `debug at` at the end

    /*
    instruction1
    abc() {
      instruction1
      def() {
        instruction1
        ghi() {
          instruction1
          instruction2
        }
      }
      instruction2
    }
    instruction2
    */

    final MockTheRExecutor executor = new MockTheRExecutor();
    final MockTheRFunctionDebugger fourthFunctionDebugger = new Stack44TheRFunctionDebugger();
    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack43TheRFunctionDebugger(fourthFunctionDebugger);
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack42TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack41TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      receiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(0, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.emptyList(), receiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(1, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error0"), receiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertEquals(2, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(1, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1"), receiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertEquals(3, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1", "error2"), receiver.getErrors());
    assertEquals(6, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(6, modifierHandler.myCounter);
    assertEquals(4, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());
    assertEquals(new TheRLocation("ghi", 0), debugger.getStack().get(3).getLocation());

    assertTrue(debugger.advance());

    assertEquals(3, executor.getCounter());
    assertEquals(1, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1", "error2"), receiver.getErrors());
    assertEquals(6, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(6, modifierHandler.myCounter);
    assertEquals(4, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());
    assertEquals(new TheRLocation("ghi", 1), debugger.getStack().get(3).getLocation());

    assertTrue(debugger.advance());

    assertEquals(3, executor.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1", "error2"), receiver.getErrors());
    assertEquals(9, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(9, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 5), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(3, executor.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1", "error2"), receiver.getErrors());
    assertEquals(9, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(9, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(3, executor.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1", "error2"), receiver.getErrors());
    assertEquals(9, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(9, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertEquals(3, executor.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error0", "error1", "error2"), receiver.getErrors());
    assertEquals(9, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(9, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());
  }

  private static class MockTheRExecutor extends com.jetbrains.ther.debugger.mock.MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      final int frameNumber = getCounter() - 1;

      return new TheRExecutionResult(
        "[1] " + frameNumber,
        TheRExecutionResultType.RESPONSE,
        TextRange.allOf("[1] " + frameNumber),
        "error" + frameNumber
      );
    }
  }

  private static class MockTheRVarsLoaderFactory implements TheRVarsLoaderFactory {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRVarsLoader getLoader(@NotNull final TheRValueModifier modifier,
                                    final int frameNumber) {
      myCounter += frameNumber;

      return new IllegalTheRVarsLoader();
    }
  }

  private static class MockTheRDebuggerEvaluatorFactory implements TheRDebuggerEvaluatorFactory {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRDebuggerEvaluator getEvaluator(@NotNull final TheRExecutor executor,
                                              @NotNull final TheRFunctionDebuggerFactory factory,
                                              @NotNull final TheROutputReceiver receiver,
                                              @NotNull final TheRExpressionHandler handler,
                                              final int frameNumber) {
      myCounter++;

      return new IllegalTheRDebuggerEvaluator();
    }
  }

  private static class MockTheRScriptReader implements TheRScriptReader {

    private boolean myIsClosed = false;

    @NotNull
    @Override
    public TheRScriptLine getCurrentLine() {
      throw new IllegalStateException("GetCurrentLine shouldn't be called");
    }

    @Override
    public void advance() throws IOException {
      throw new IllegalStateException("Advance shouldn't be called");
    }

    @Override
    public void close() throws IOException {
      myIsClosed = true;
    }
  }

  private static class MockTheRExpressionHandler extends IllegalTheRExpressionHandler {

    private int myCounter = 0;

    @Override
    public void setMaxFrameNumber(final int maxFrameNumber) {
      myCounter += maxFrameNumber;
    }
  }

  private static class MockTheRValueModifierFactory implements TheRValueModifierFactory {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRValueModifier getModifier(@NotNull final TheRExecutor executor,
                                         @NotNull final TheRFunctionDebuggerFactory factory,
                                         @NotNull final TheROutputReceiver receiver,
                                         @NotNull final TheRValueModifierHandler handler,
                                         final int frameNumber) {
      myCounter++;

      return new IllegalTheRValueModifier();
    }
  }

  private static class MockTheRValueModifierHandler extends IllegalTheRValueModifierHandler {

    private int myCounter = 0;

    @Override
    public void setMaxFrameNumber(final int maxFrameNumber) {
      myCounter += maxFrameNumber;
    }
  }

  private static class Stack21TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final TheRFunctionDebugger myNextFunctionDebugger;

    public Stack21TheRFunctionDebugger(@NotNull final TheRFunctionDebugger nextFunctionDebugger) {
      super(MAIN_FUNCTION_NAME, 3);

      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        getHandler().appendDebugger(myNextFunctionDebugger);
      }
    }
  }

  private static class Stack311TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack311TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super(MAIN_FUNCTION_NAME, 3);

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

  private static class Stack312TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack312TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super("abc", 3);

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

  private static class Stack313TheRFunctionDebugger extends MockTheRFunctionDebugger {

    public Stack313TheRFunctionDebugger() {
      super("def", 2);
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        getHandler().setReturnLineNumber(5);
      }
    }
  }

  private static class Stack321TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack321TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super(MAIN_FUNCTION_NAME, 3);

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

  private static class Stack322TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack322TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
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

  private static class Stack323TheRFunctionDebugger extends MockTheRFunctionDebugger {

    public Stack323TheRFunctionDebugger() {
      super("def", 2);
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

  private static class Stack41TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack41TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super(MAIN_FUNCTION_NAME, 3);

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

  private static class Stack42TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack42TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super("abc", 3);

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

  private static class Stack43TheRFunctionDebugger extends MockTheRFunctionDebugger {

    @NotNull
    private final MockTheRFunctionDebugger myNextFunctionDebugger;

    public Stack43TheRFunctionDebugger(@NotNull final MockTheRFunctionDebugger nextFunctionDebugger) {
      super("def", 2);

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

  private static class Stack44TheRFunctionDebugger extends MockTheRFunctionDebugger {

    public Stack44TheRFunctionDebugger() {
      super("ghi", 2);
    }

    @Override
    public void advance() throws TheRDebuggerException {
      super.advance();

      if (getCounter() == 2) {
        assert getHandler() != null;

        getHandler().setDropFrames(2);
        getHandler().setReturnLineNumber(5);
      }
    }
  }
}