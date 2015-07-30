package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRScriptLine;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluatorFactory;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRVarsLoader;
import com.jetbrains.ther.debugger.frame.TheRVarsLoaderFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.mock.IllegalTheRDebuggerEvaluator;
import com.jetbrains.ther.debugger.mock.IllegalTheROutputReceiver;
import com.jetbrains.ther.debugger.mock.IllegalTheRVarsLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    final Stack1TheRFunctionDebugger functionDebugger = new Stack1TheRFunctionDebugger();
    final Stack1TheRFunctionDebuggerFactory debuggerFactory = new Stack1TheRFunctionDebuggerFactory(functionDebugger);
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
    assertEquals(0, functionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, functionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, functionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(2, functionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
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
    final Stack22TheRFunctionDebugger notMainFunctionDebugger = new Stack22TheRFunctionDebugger();
    final Stack21TheRFunctionDebugger mainFunctionDebugger = new Stack21TheRFunctionDebugger(notMainFunctionDebugger);
    final Stack2TheRFunctionDebuggerFactory debuggerFactory =
      new Stack2TheRFunctionDebuggerFactory(mainFunctionDebugger);
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
    assertEquals(0, notMainFunctionDebugger.myCounter);
    assertEquals(0, mainFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, notMainFunctionDebugger.myCounter);
    assertEquals(1, mainFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, notMainFunctionDebugger.myCounter);
    assertEquals(2, mainFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, notMainFunctionDebugger.myCounter);
    assertEquals(2, mainFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, notMainFunctionDebugger.myCounter);
    assertEquals(2, mainFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, notMainFunctionDebugger.myCounter);
    assertEquals(3, mainFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(2, notMainFunctionDebugger.myCounter);
    assertEquals(3, mainFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
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
    final Stack313TheRFunctionDebugger thirdFunctionDebugger = new Stack313TheRFunctionDebugger();
    final Stack312TheRFunctionDebugger secondFunctionDebugger = new Stack312TheRFunctionDebugger(thirdFunctionDebugger);
    final Stack311TheRFunctionDebugger firstFunctionDebugger = new Stack311TheRFunctionDebugger(secondFunctionDebugger);
    final Stack31TheRFunctionDebuggerFactory debuggerFactory = new Stack31TheRFunctionDebuggerFactory(firstFunctionDebugger);
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
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(0, secondFunctionDebugger.myCounter);
    assertEquals(0, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(0, secondFunctionDebugger.myCounter);
    assertEquals(1, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(0, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(1, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 5), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(3, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(3, secondFunctionDebugger.myCounter);
    assertEquals(3, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(3, secondFunctionDebugger.myCounter);
    assertEquals(3, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
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
    final Stack323TheRFunctionDebugger thirdFunctionDebugger = new Stack323TheRFunctionDebugger();
    final Stack322TheRFunctionDebugger secondFunctionDebugger = new Stack322TheRFunctionDebugger(thirdFunctionDebugger);
    final Stack321TheRFunctionDebugger firstFunctionDebugger = new Stack321TheRFunctionDebugger(secondFunctionDebugger);
    final Stack32TheRFunctionDebuggerFactory debuggerFactory = new Stack32TheRFunctionDebuggerFactory(firstFunctionDebugger);
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
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(0, secondFunctionDebugger.myCounter);
    assertEquals(0, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(0, secondFunctionDebugger.myCounter);
    assertEquals(1, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(0, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(1, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(3, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(3, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
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
    final Stack44TheRFunctionDebugger fourthFunctionDebugger = new Stack44TheRFunctionDebugger();
    final Stack43TheRFunctionDebugger thirdFunctionDebugger = new Stack43TheRFunctionDebugger(fourthFunctionDebugger);
    final Stack42TheRFunctionDebugger secondFunctionDebugger = new Stack42TheRFunctionDebugger(thirdFunctionDebugger);
    final Stack41TheRFunctionDebugger firstFunctionDebugger = new Stack41TheRFunctionDebugger(secondFunctionDebugger);
    final Stack4TheRFunctionDebuggerFactory debuggerFactory = new Stack4TheRFunctionDebuggerFactory(firstFunctionDebugger);
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
    assertEquals(0, fourthFunctionDebugger.myCounter);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(0, secondFunctionDebugger.myCounter);
    assertEquals(0, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, fourthFunctionDebugger.myCounter);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(0, secondFunctionDebugger.myCounter);
    assertEquals(1, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(1, loaderFactory.myCounter);
    assertEquals(1, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, fourthFunctionDebugger.myCounter);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(0, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 0), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, fourthFunctionDebugger.myCounter);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(1, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(2, loaderFactory.myCounter);
    assertEquals(2, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, fourthFunctionDebugger.myCounter);
    assertEquals(0, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 0), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, fourthFunctionDebugger.myCounter);
    assertEquals(1, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(3, loaderFactory.myCounter);
    assertEquals(3, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(3, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(0, fourthFunctionDebugger.myCounter);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(4, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(4, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());
    assertEquals(new TheRLocation("ghi", 0), debugger.getStack().get(3).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(1, fourthFunctionDebugger.myCounter);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(4, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(4, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 1), debugger.getStack().get(1).getLocation());
    assertEquals(new TheRLocation("def", 1), debugger.getStack().get(2).getLocation());
    assertEquals(new TheRLocation("ghi", 1), debugger.getStack().get(3).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, fourthFunctionDebugger.myCounter);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(2, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(4, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(2, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getStack().get(0).getLocation());
    assertEquals(new TheRLocation("abc", 5), debugger.getStack().get(1).getLocation());

    assertTrue(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, fourthFunctionDebugger.myCounter);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(3, secondFunctionDebugger.myCounter);
    assertEquals(2, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(4, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    assertFalse(debugger.advance());

    assertFalse(process.myIsClosed);
    assertEquals(2, fourthFunctionDebugger.myCounter);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(3, secondFunctionDebugger.myCounter);
    assertEquals(3, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(4, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertFalse(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());

    debugger.stop();

    assertTrue(process.myIsClosed);
    assertEquals(2, fourthFunctionDebugger.myCounter);
    assertEquals(2, thirdFunctionDebugger.myCounter);
    assertEquals(3, secondFunctionDebugger.myCounter);
    assertEquals(3, firstFunctionDebugger.myCounter);
    assertEquals(1, debuggerFactory.myCounter);
    assertEquals(4, loaderFactory.myCounter);
    assertEquals(4, evaluatorFactory.myCounter);
    assertTrue(reader.myIsClosed);
    assertEquals(1, debugger.getStack().size());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getStack().get(0).getLocation());
  }

  private static class MockTheRProcess implements TheRProcess {

    private boolean myIsClosed = false;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      throw new IllegalStateException("Execute shouldn't be called");
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
      myCounter++;

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

  private static class Stack1TheRFunctionDebuggerFactory implements TheRFunctionDebuggerFactory {

    @NotNull
    private final TheRFunctionDebugger myFirstFunctionDebugger;

    private int myCounter = 0;

    public Stack1TheRFunctionDebuggerFactory(@NotNull final TheRFunctionDebugger firstFunctionDebugger) {
      myFirstFunctionDebugger = firstFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                           @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                           @NotNull final TheROutputReceiver outputReceiver) throws TheRDebuggerException {
      throw new IllegalStateException("GetNotMainFunctionDebugger shouldn't be called");
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                        @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                        @NotNull final TheROutputReceiver outputReceiver,
                                                        @NotNull final TheRScriptReader scriptReader) {
      myCounter++;

      return myFirstFunctionDebugger;
    }
  }

  private static class Stack1TheRFunctionDebugger implements TheRFunctionDebugger {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation(MAIN_FUNCTION_NAME, myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 2;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }
  }

  private static class Stack2TheRFunctionDebuggerFactory implements TheRFunctionDebuggerFactory {

    @NotNull
    private final Stack21TheRFunctionDebugger myFirstFunctionDebugger;

    private int myCounter = 0;

    public Stack2TheRFunctionDebuggerFactory(@NotNull final Stack21TheRFunctionDebugger firstFunctionDebugger) {
      myFirstFunctionDebugger = firstFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                           @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                           @NotNull final TheROutputReceiver outputReceiver) throws TheRDebuggerException {
      throw new IllegalStateException("GetNotMainFunctionDebugger shouldn't be called");
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                        @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                        @NotNull final TheROutputReceiver outputReceiver,
                                                        @NotNull final TheRScriptReader scriptReader) {
      myCounter++;

      myFirstFunctionDebugger.setHandler(debuggerHandler);

      return myFirstFunctionDebugger;
    }
  }

  private static class Stack21TheRFunctionDebugger implements TheRFunctionDebugger {

    @NotNull
    private final TheRFunctionDebugger myNextFunctionDebugger;

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    public Stack21TheRFunctionDebugger(@NotNull final TheRFunctionDebugger nextFunctionDebugger) {
      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation(MAIN_FUNCTION_NAME, myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 3;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myHandler.appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@NotNull final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack22TheRFunctionDebugger implements TheRFunctionDebugger {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation("abc", myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 2;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }
  }

  private static class Stack31TheRFunctionDebuggerFactory implements TheRFunctionDebuggerFactory {

    @NotNull
    private final Stack311TheRFunctionDebugger myFirstFunctionDebugger;

    private int myCounter = 0;

    public Stack31TheRFunctionDebuggerFactory(@NotNull final Stack311TheRFunctionDebugger firstFunctionDebugger) {
      myFirstFunctionDebugger = firstFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                           @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                           @NotNull final TheROutputReceiver outputReceiver) throws TheRDebuggerException {
      throw new IllegalStateException("GetNotMainFunctionDebugger shouldn't be called");
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                        @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                        @NotNull final TheROutputReceiver outputReceiver,
                                                        @NotNull final TheRScriptReader scriptReader) {
      myCounter++;

      myFirstFunctionDebugger.setHandler(debuggerHandler);

      return myFirstFunctionDebugger;
    }
  }

  private static class Stack311TheRFunctionDebugger implements TheRFunctionDebugger {

    @NotNull
    private final Stack312TheRFunctionDebugger myNextFunctionDebugger;

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    public Stack311TheRFunctionDebugger(@NotNull final Stack312TheRFunctionDebugger nextFunctionDebugger) {
      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation(MAIN_FUNCTION_NAME, myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 3;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myNextFunctionDebugger.setHandler(myHandler);
        myHandler.appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@NotNull final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack312TheRFunctionDebugger implements TheRFunctionDebugger {

    @NotNull
    private final Stack313TheRFunctionDebugger myNextFunctionDebugger;

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    public Stack312TheRFunctionDebugger(@NotNull final Stack313TheRFunctionDebugger nextFunctionDebugger) {
      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation("abc", myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 3;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myNextFunctionDebugger.setHandler(myHandler);
        myHandler.appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@NotNull final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack313TheRFunctionDebugger implements TheRFunctionDebugger {

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation("def", myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 2;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myHandler.setReturnLineNumber(5);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@Nullable final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack32TheRFunctionDebuggerFactory implements TheRFunctionDebuggerFactory {

    @NotNull
    private final Stack321TheRFunctionDebugger myFirstFunctionDebugger;

    private int myCounter = 0;

    public Stack32TheRFunctionDebuggerFactory(@NotNull final Stack321TheRFunctionDebugger firstFunctionDebugger) {
      myFirstFunctionDebugger = firstFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                           @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                           @NotNull final TheROutputReceiver outputReceiver) throws TheRDebuggerException {
      throw new IllegalStateException("GetNotMainFunctionDebugger shouldn't be called");
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                        @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                        @NotNull final TheROutputReceiver outputReceiver,
                                                        @NotNull final TheRScriptReader scriptReader) {
      myCounter++;

      myFirstFunctionDebugger.setHandler(debuggerHandler);

      return myFirstFunctionDebugger;
    }
  }

  private static class Stack321TheRFunctionDebugger implements TheRFunctionDebugger {

    @NotNull
    private final Stack322TheRFunctionDebugger myNextFunctionDebugger;

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    public Stack321TheRFunctionDebugger(@NotNull final Stack322TheRFunctionDebugger nextFunctionDebugger) {
      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation(MAIN_FUNCTION_NAME, myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 3;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myNextFunctionDebugger.setHandler(myHandler);
        myHandler.appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@Nullable final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack322TheRFunctionDebugger implements TheRFunctionDebugger {

    @NotNull
    private final Stack323TheRFunctionDebugger myNextFunctionDebugger;

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    public Stack322TheRFunctionDebugger(@NotNull final Stack323TheRFunctionDebugger nextFunctionDebugger) {
      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation("abc", myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 2;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myNextFunctionDebugger.setHandler(myHandler);
        myHandler.appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@NotNull final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack323TheRFunctionDebugger implements TheRFunctionDebugger {

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation("def", myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 2;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myHandler.setDropFrames(2);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@Nullable final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack4TheRFunctionDebuggerFactory implements TheRFunctionDebuggerFactory {

    @NotNull
    private final Stack41TheRFunctionDebugger myFirstFunctionDebugger;

    private int myCounter = 0;

    public Stack4TheRFunctionDebuggerFactory(@NotNull final Stack41TheRFunctionDebugger firstFunctionDebugger) {
      myFirstFunctionDebugger = firstFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                           @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                           @NotNull final TheROutputReceiver outputReceiver) throws TheRDebuggerException {
      throw new IllegalStateException("GetNotMainFunctionDebugger shouldn't be called");
    }

    @NotNull
    @Override
    public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                        @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                        @NotNull final TheROutputReceiver outputReceiver,
                                                        @NotNull final TheRScriptReader scriptReader) {
      myCounter++;

      myFirstFunctionDebugger.setHandler(debuggerHandler);

      return myFirstFunctionDebugger;
    }
  }

  private static class Stack41TheRFunctionDebugger implements TheRFunctionDebugger {

    @NotNull
    private final Stack42TheRFunctionDebugger myNextFunctionDebugger;

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    public Stack41TheRFunctionDebugger(@NotNull final Stack42TheRFunctionDebugger nextFunctionDebugger) {
      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation(MAIN_FUNCTION_NAME, myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 3;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myNextFunctionDebugger.setHandler(myHandler);
        myHandler.appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@Nullable final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack42TheRFunctionDebugger implements TheRFunctionDebugger {

    @NotNull
    private final Stack43TheRFunctionDebugger myNextFunctionDebugger;

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    public Stack42TheRFunctionDebugger(@NotNull final Stack43TheRFunctionDebugger nextFunctionDebugger) {
      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation("abc", myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 3;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myNextFunctionDebugger.setHandler(myHandler);
        myHandler.appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@NotNull final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack43TheRFunctionDebugger implements TheRFunctionDebugger {

    @NotNull
    private final Stack44TheRFunctionDebugger myNextFunctionDebugger;

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    public Stack43TheRFunctionDebugger(@NotNull final Stack44TheRFunctionDebugger nextFunctionDebugger) {
      myNextFunctionDebugger = nextFunctionDebugger;
    }

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation("def", myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 2;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myNextFunctionDebugger.setHandler(myHandler);
        myHandler.appendDebugger(myNextFunctionDebugger);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@NotNull final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }

  private static class Stack44TheRFunctionDebugger implements TheRFunctionDebugger {

    private int myCounter = 0;

    @Nullable
    private TheRFunctionDebuggerHandler myHandler;

    @NotNull
    @Override
    public TheRLocation getLocation() {
      return new TheRLocation("ghi", myCounter);
    }

    @Override
    public boolean hasNext() {
      return myCounter < 2;
    }

    @Override
    public void advance() throws TheRDebuggerException {
      myCounter++;

      if (myCounter == 2) {
        assert myHandler != null;

        myHandler.setDropFrames(2);
        myHandler.setReturnLineNumber(5);
      }
    }

    @NotNull
    @Override
    public String getResult() {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    public void setHandler(@NotNull final TheRFunctionDebuggerHandler handler) {
      myHandler = handler;
    }
  }
}