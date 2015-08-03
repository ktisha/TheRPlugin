package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRScriptLine;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluatorFactory;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRVarsLoader;
import com.jetbrains.ther.debugger.frame.TheRVarsLoaderFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import com.jetbrains.ther.debugger.mock.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

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

    final MockTheRProcess process = new MockTheRProcess();
    final MockTheRFunctionDebugger functionDebugger = new MockTheRFunctionDebugger(MAIN_FUNCTION_NAME, 2);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, functionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();

    final TheRDebugger debugger = new TheRDebugger(
      process,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      new IllegalTheROutputReceiver()
    );

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(0, functionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(1, functionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(2, functionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(2, functionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
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

    final MockTheRProcess process = new MockTheRProcess();
    final MockTheRFunctionDebugger secondFunctionDebugger = new MockTheRFunctionDebugger("abc", 2);
    final Stack21TheRFunctionDebugger firstFunctionDebugger = new Stack21TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();

    final TheRDebugger debugger = new TheRDebugger(
      process,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      new IllegalTheROutputReceiver()
    );

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
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

    final MockTheRProcess process = new MockTheRProcess();
    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack313TheRFunctionDebugger();
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack312TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack311TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();

    final TheRDebugger debugger = new TheRDebugger(
      process,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      new IllegalTheROutputReceiver()
    );

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(1, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 5), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
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

    final MockTheRProcess process = new MockTheRProcess();
    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack323TheRFunctionDebugger();
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack322TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack321TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();

    final TheRDebugger debugger = new TheRDebugger(
      process,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      new IllegalTheROutputReceiver()
    );

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(1, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
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

    final MockTheRProcess process = new MockTheRProcess();
    final MockTheRFunctionDebugger fourthFunctionDebugger = new Stack44TheRFunctionDebugger();
    final MockTheRFunctionDebugger thirdFunctionDebugger = new Stack43TheRFunctionDebugger(fourthFunctionDebugger);
    final MockTheRFunctionDebugger secondFunctionDebugger = new Stack42TheRFunctionDebugger(thirdFunctionDebugger);
    final MockTheRFunctionDebugger firstFunctionDebugger = new Stack41TheRFunctionDebugger(secondFunctionDebugger);
    final MockTheRFunctionDebuggerFactory debuggerFactory = new MockTheRFunctionDebuggerFactory(null, firstFunctionDebugger);
    final MockTheRVarsLoaderFactory loaderFactory = new MockTheRVarsLoaderFactory();
    final MockTheRDebuggerEvaluatorFactory evaluatorFactory = new MockTheRDebuggerEvaluatorFactory();
    final MockTheRScriptReader reader = new MockTheRScriptReader();

    final TheRDebugger debugger = new TheRDebugger(
      process,
      debuggerFactory,
      loaderFactory,
      evaluatorFactory,
      reader,
      new IllegalTheROutputReceiver()
    );

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(0, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, process.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(1, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(0, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, process.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(1, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(0, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(3, process.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(1, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(6, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(4, process.getCounter());
    assertEquals(0, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(10, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(4, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());
    assertEquals(new TheRLocation("ghi", 0), debugger.getStack().get(3).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(4, process.getCounter());
    assertEquals(1, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(10, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(4, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());
    assertEquals(new TheRLocation("ghi", 1), debugger.getStack().get(3).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(4, process.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(2, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(10, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 5), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(4, process.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(2, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(10, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(4, process.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(10, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(4, process.getCounter());
    assertEquals(2, fourthFunctionDebugger.getCounter());
    assertEquals(2, thirdFunctionDebugger.getCounter());
    assertEquals(3, secondFunctionDebugger.getCounter());
    assertEquals(3, firstFunctionDebugger.getCounter());
    assertEquals(0, debuggerFactory.getNotMainCounter());
    assertEquals(1, debuggerFactory.getMainCounter());
    assertEquals(10, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());
  }

  private static class MockTheRProcess extends com.jetbrains.ther.debugger.mock.MockTheRProcess {

    private boolean myIsClosed = false;

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      return new TheRProcessResponse(
        "[1] " + getCounter(),
        TheRProcessResponseType.RESPONSE,
        TextRange.allOf("[1] " + getCounter()),
        ""
      );
    }

    @Override
    public void stop() {
      myIsClosed = true;
    }
  }

  private static class MockTheRVarsLoaderFactory implements TheRVarsLoaderFactory {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRVarsLoader getLoader(final int frameNumber) {
      myCounter += frameNumber;

      return new IllegalTheRVarsLoader();
    }
  }

  private static class MockTheRDebuggerEvaluatorFactory implements TheRDebuggerEvaluatorFactory {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRDebuggerEvaluator getEvaluator(@NotNull final TheRProcess process,
                                              @NotNull final TheRFunctionDebuggerFactory factory,
                                              @NotNull final TheROutputReceiver receiver) {
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