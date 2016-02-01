package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRResponseConstants;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.MAIN_FUNCTION_NAME;
import static com.jetbrains.ther.debugger.mock.MockTheRExecutor.LS_FUNCTIONS_ERROR;
import static org.junit.Assert.*;

public class TheRDebuggerTest {

  @Test
  public void empty() throws TheRDebuggerException {
    final EmptyTheRExecutor executor = new EmptyTheRExecutor();
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader scriptReader = new MockTheRScriptReader(0);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      loaderFactory,
      evaluatorFactory,
      scriptReader,
      outputReceiver,
      new IllegalTheRExpressionHandler(),
      modifierFactory,
      new IllegalTheRValueModifierHandler()
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(0, evaluatorFactory.myCounter);
    assertFalse(scriptReader.isClosed());
    assertEquals(0, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, modifierFactory.myCounter);
    assertEquals(0, debugger.getStack().size());

    assertFalse(debugger.advance());

    assertEquals(4, executor.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(0, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Arrays.asList("error1", "error_complete", LS_FUNCTIONS_ERROR, "error_body"), outputReceiver.getErrors());
    assertEquals(0, modifierFactory.myCounter);
    assertEquals(0, debugger.getStack().size());
  }

  @Test
  public void stack1() throws TheRDebuggerException {
    // just `main`

    /*
    instruction1
    instruction2
    */

    final int scriptLength = 2;

    final MockTheRExecutor executor = new MockTheRExecutor(scriptLength);
    final MockTheRFunctionDebugger functionDebugger = new MockTheRFunctionDebugger(MAIN_FUNCTION_NAME, scriptLength);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(functionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader scriptReader = new MockTheRScriptReader(scriptLength);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      scriptReader,
      outputReceiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, functionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(0, evaluatorFactory.myCounter);
    assertFalse(scriptReader.isClosed());
    assertEquals(0, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(0, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());

    assertTrue(debugger.advance());

    assertEquals(8, executor.getCounter());
    assertEquals(0, functionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(
      Arrays.asList("error1", "error2", "error3", "error_complete", LS_FUNCTIONS_ERROR, "error_body", "error_call", "error0"),
      outputReceiver.getErrors()
    );
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(8, executor.getCounter());
    assertEquals(1, functionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(8, executor.getCounter());
    assertEquals(2, functionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(-1, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(-1, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());
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

    final int scriptLength = 6;

    final MockTheRExecutor executor = new MockTheRExecutor(scriptLength);
    final MockTheRFunctionDebugger secondFunctionDebugger = new MockTheRFunctionDebugger("abc", 2);
    final Stack21TheRFunctionDebugger firstFunctionDebugger = new Stack21TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader scriptReader = new MockTheRScriptReader(scriptLength);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      scriptReader,
      outputReceiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(0, evaluatorFactory.myCounter);
    assertFalse(scriptReader.isClosed());
    assertEquals(0, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(0, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());

    assertTrue(debugger.advance());

    assertEquals(12, executor.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(
      Arrays.asList(
        "error1", "error2", "error3", "error4", "error5", "error6", "error7", "error_complete", LS_FUNCTIONS_ERROR, "error_body",
        "error_call", "error0"
      ),
      outputReceiver.getErrors()
    );
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(12, executor.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(13, executor.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.singletonList("error1"), outputReceiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(13, executor.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(13, executor.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(13, executor.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());
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

    final int scriptLength = 10;

    final MockTheRExecutor executor = new MockTheRExecutor(scriptLength);
    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack313TheRFunctionDebugger();
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack312TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack311TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader scriptReader = new MockTheRScriptReader(scriptLength);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      scriptReader,
      outputReceiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(0, evaluatorFactory.myCounter);
    assertFalse(scriptReader.isClosed());
    assertEquals(0, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(0, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());

    assertTrue(debugger.advance());

    assertEquals(16, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(
      Arrays.asList(
        "error1", "error2", "error3", "error4", "error5", "error6", "error7", "error8", "error9", "error10", "error11", "error_complete",
        LS_FUNCTIONS_ERROR, "error_body", "error_call", "error0"
      ),
      outputReceiver.getErrors()
    );
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(16, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(17, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.singletonList("error1"), outputReceiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(17, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(18, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.singletonList("error2"), outputReceiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(18, executor.getCounter());
    assertEquals(1, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertEquals(18, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 5), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(18, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(18, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());
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

    final int scriptLength = 9;

    final MockTheRExecutor executor = new MockTheRExecutor(scriptLength);
    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack323TheRFunctionDebugger();
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack322TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack321TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader scriptReader = new MockTheRScriptReader(scriptLength);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      scriptReader,
      outputReceiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(0, evaluatorFactory.myCounter);
    assertFalse(scriptReader.isClosed());
    assertEquals(0, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(0, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());

    assertTrue(debugger.advance());

    assertEquals(15, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(
      Arrays.asList(
        "error1", "error2", "error3", "error4", "error5", "error6", "error7", "error8", "error9", "error10", "error_complete",
        LS_FUNCTIONS_ERROR, "error_body", "error_call", "error0"
      ),
      outputReceiver.getErrors()
    );
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(15, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(16, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.singletonList("error1"), outputReceiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(16, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(17, executor.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.singletonList("error2"), outputReceiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(17, executor.getCounter());
    assertEquals(1, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertEquals(17, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(4, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(4, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(17, executor.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());
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

    final int scriptLength = 13;

    final MockTheRExecutor executor = new MockTheRExecutor(scriptLength);
    final MockTheRFunctionDebugger fourthFunctionDebugger = new Stack44TheRFunctionDebugger();
    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack43TheRFunctionDebugger(fourthFunctionDebugger);
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack42TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack41TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader scriptReader = new MockTheRScriptReader(scriptLength);
    final MockTheROutputReceiver outputReceiver = new MockTheROutputReceiver();
    final MockTheRExpressionHandler expressionHandler = new MockTheRExpressionHandler();
    final MockTheRValueModifierFactory modifierFactory = new MockTheRValueModifierFactory();
    final MockTheRValueModifierHandler modifierHandler = new MockTheRValueModifierHandler();

    final TheRDebugger debugger = new TheRDebugger(
      executor,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      scriptReader,
      outputReceiver,
      expressionHandler,
      modifierFactory,
      modifierHandler
    );

    assertEquals(0, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(0, evaluatorFactory.myCounter);
    assertFalse(scriptReader.isClosed());
    assertEquals(0, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(0, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());

    assertTrue(debugger.advance());

    assertEquals(19, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(
      Arrays.asList(
        "error1", "error2", "error3", "error4", "error5", "error6", "error7", "error8", "error9", "error10", "error11", "error12",
        "error13", "error14", "error_complete", LS_FUNCTIONS_ERROR, "error_body", "error_call", "error0"
      ),
      outputReceiver.getErrors()
    );
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(19, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(0, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(0, expressionHandler.myCounter);
    assertEquals(1, modifierFactory.myCounter);
    assertEquals(0, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertEquals(20, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.singletonList("error1"), outputReceiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(20, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(1, expressionHandler.myCounter);
    assertEquals(2, modifierFactory.myCounter);
    assertEquals(1, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(21, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.singletonList("error2"), outputReceiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(21, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(1, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(3, expressionHandler.myCounter);
    assertEquals(3, modifierFactory.myCounter);
    assertEquals(3, modifierHandler.myCounter);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertEquals(22, executor.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.singletonList("error3"), outputReceiver.getErrors());
    assertEquals(6, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(6, modifierHandler.myCounter);
    assertEquals(4, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());
    assertEquals(new TheRLocation("ghi", 0), debugger.getStack().get(3).getLocation());

    outputReceiver.reset();
    assertTrue(debugger.advance());

    assertEquals(22, executor.getCounter());
    assertEquals(1, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(6, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(6, modifierHandler.myCounter);
    assertEquals(4, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());
    assertEquals(new TheRLocation("ghi", 1), debugger.getStack().get(3).getLocation());

    assertTrue(debugger.advance());

    assertEquals(22, executor.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(9, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(9, modifierHandler.myCounter);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 5), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertEquals(22, executor.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(9, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(9, modifierHandler.myCounter);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertEquals(22, executor.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(1, debuggerFactory.getCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertTrue(scriptReader.isClosed());
    assertEquals(scriptLength + 1, scriptReader.getCounter());
    assertEquals(Collections.emptyList(), outputReceiver.getOutputs());
    assertEquals(Collections.emptyList(), outputReceiver.getErrors());
    assertEquals(8, expressionHandler.myCounter);
    assertEquals(4, modifierFactory.myCounter);
    assertEquals(8, modifierHandler.myCounter);
    assertEquals(0, debugger.getStack().size());
  }

  private static class MockTheRExecutor extends com.jetbrains.ther.debugger.mock.MockTheRExecutor {

    private final int myScriptLength;

    public MockTheRExecutor(final int scriptLength) {
      myScriptLength = scriptLength;
    }

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() < myScriptLength + 2) {
        return new TheRExecutionResult(
          TheRResponseConstants.PLUS_AND_SPACE,
          TheRExecutionResultType.PLUS,
          TextRange.EMPTY_RANGE,
          "error" + getCounter()
        );
      }

      if (getCounter() == myScriptLength + 2) {
        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.EMPTY,
          TextRange.EMPTY_RANGE,
          "error_complete"
        );
      }

      if (getCounter() == myScriptLength + 4) {
        return new TheRExecutionResult(
          " \n \n \n \n \n \n ",
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(" \n \n \n \n \n \n "),
          "error_body"
        );
      }

      if (getCounter() == myScriptLength + 5) {
        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.DEBUGGING_IN,
          TextRange.EMPTY_RANGE,
          "error_call"
        );
      }

      final int frameNumber = getCounter() - 1 - myScriptLength - 5;

      return new TheRExecutionResult(
        "[1] " + frameNumber,
        TheRExecutionResultType.RESPONSE,
        TextRange.allOf("[1] " + frameNumber),
        "error" + frameNumber
      );
    }
  }

  private static class EmptyTheRExecutor extends MockTheRExecutor {

    public EmptyTheRExecutor() {
      super(0);
    }

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() < 4) {
        return super.doExecute(command);
      }
      else if (getCounter() == 4) {
        return new TheRExecutionResult(
          " \n \n \n ",
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(" \n \n \n "),
          "error_body"
        );
      }
      else {
        throw new IllegalStateException("Unexpected command");
      }
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

  private static class MockTheRScriptReader extends BufferedReader {

    private int myCounter;
    private boolean myIsClosed;

    public MockTheRScriptReader(final int length) {
      super(new StringReader(calculateString(length)));

      myCounter = 0;
      myIsClosed = false;
    }

    @Override
    public String readLine() throws IOException {
      final String result = super.readLine();

      myCounter++;

      return result;
    }

    @Override
    public void close() throws IOException {
      super.close();

      myIsClosed = true;
    }

    public int getCounter() {
      return myCounter;
    }

    public boolean isClosed() {
      return myIsClosed;
    }

    @NotNull
    private static String calculateString(final int length) {
      final StringBuilder sb = new StringBuilder();

      for (int i = 0; i < length; i++) {
        sb.append(TheRDebugConstants.LINE_SEPARATOR);
      }

      return sb.toString();
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