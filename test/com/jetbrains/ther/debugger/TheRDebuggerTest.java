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
import com.jetbrains.ther.debugger.mock.EmptyTheRVarsLoader;
import com.jetbrains.ther.debugger.mock.IllegalTheRDebuggerEvaluator;
import com.jetbrains.ther.debugger.mock.IllegalTheROutputReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.MAIN_FUNCTION_NAME;
import static org.junit.Assert.*;

public class TheRDebuggerTest {

  @Test
  public void stack1() throws TheRDebuggerException {
    // just `main`

    final MockTheRProcess process = new MockTheRProcess();
    final Stack1TheRFunctionDebugger functionDebugger = new Stack1TheRFunctionDebugger();
    final Stack1TheRFunctionDebuggerFactory debuggerFactory = new Stack1TheRFunctionDebuggerFactory(functionDebugger);
    final Stack1TheRVarsLoaderFactory loaderFactory = new Stack1TheRVarsLoaderFactory();
    final Stack1TheRDebuggerEvaluatorFactory evaluatorFactory = new Stack1TheRDebuggerEvaluatorFactory();
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
  public void stack2() {
    // `main` and function
    fail();
  }

  @Test
  public void stack31() {
    // `main`, function `a`, function `b` with `debug at` at the end
    fail();
  }

  @Test
  public void stack32() {
    // `main`, function `a` and function `b` with recursive return
    fail();
  }

  @Test
  public void stack4() {
    // `main`, function `a`, function `b`, function `c` - recursive return from `c` and `b` with `debug at` at the end
    fail();
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
    private final TheRFunctionDebugger myDebugger;

    private int myCounter = 0;

    public Stack1TheRFunctionDebuggerFactory(@NotNull final TheRFunctionDebugger debugger) {
      myDebugger = debugger;
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

      return myDebugger;
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

  private static class Stack1TheRVarsLoaderFactory implements TheRVarsLoaderFactory {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRVarsLoader getLoader(final int frameNumber) {
      myCounter++;

      return new EmptyTheRVarsLoader();
    }
  }

  private static class Stack1TheRDebuggerEvaluatorFactory implements TheRDebuggerEvaluatorFactory {

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
}