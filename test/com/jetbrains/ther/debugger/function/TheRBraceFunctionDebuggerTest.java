package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.mock.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtilsTest.LS_FUNCTIONS_COMMAND;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtilsTest.NO_FUNCTIONS_RESULT;
import static org.junit.Assert.*;

public class TheRBraceFunctionDebuggerTest {

  @Test
  public void ordinary() throws TheRDebuggerException {
    /*
    abc() {
      instruction1
      instruction2
    }
    */

    final OrdinaryTheRExecutor executor = new OrdinaryTheRExecutor();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRBraceFunctionDebugger debugger = new TheRBraceFunctionDebugger(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      new IllegalTheRFunctionDebuggerHandler(),
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, executor.getCounter());
    assertEquals(Arrays.asList("error_dbg_at_1", "error_ls"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 1), debugger.getLocation());
    assertEquals(4, executor.getCounter());
    assertEquals(Arrays.asList("error_dbg_at_2", "error_ls"), receiver.getErrors());
    assertEquals(Collections.singletonList("[1] 1 2 3"), receiver.getOutputs());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(5, executor.getCounter());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void function() throws TheRDebuggerException {
    /*
    abc() {
      def()
      instruction2
    }
    */

    final FunctionTheRExecutor executor = new FunctionTheRExecutor();
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(new IllegalTheRFunctionDebugger());
    final FunctionTheRFunctionDebuggerHandler handler = new FunctionTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRBraceFunctionDebugger debugger = new TheRBraceFunctionDebugger(
      executor,
      factory,
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, executor.getCounter());
    assertEquals(0, factory.getCounter());
    assertEquals(0, handler.getCounter());
    assertEquals(Arrays.asList("error_dbg_at_1", "error_ls"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(3, executor.getCounter());
    assertEquals(1, factory.getCounter());
    assertEquals(1, handler.getCounter());
    assertEquals(Collections.singletonList("error_debugging"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(4, executor.getCounter());
    assertEquals(1, factory.getCounter());
    assertEquals(1, handler.getCounter());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void recursiveReturn() throws TheRDebuggerException {
    /*
    abc() {
      `x + 1` with recursive return
    }
    */

    final RecursiveReturnTheRExecutor executor = new RecursiveReturnTheRExecutor();
    final RecursiveReturnTheRFunctionDebuggerHandler handler = new RecursiveReturnTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRBraceFunctionDebugger debugger = new TheRBraceFunctionDebugger(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, executor.getCounter());
    assertEquals(0, handler.getCounter());
    assertEquals(Arrays.asList("error_dbg_at", "error_ls"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(3, executor.getCounter());
    assertEquals(3, handler.getCounter());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void debugAt() throws TheRDebuggerException {
    /*
    abc() {
      `x + 1` with `debug at`
    }
    */

    final DebugAtTheRExecutor executor = new DebugAtTheRExecutor();
    final DebugAtTheRFunctionDebuggerHandler handler = new DebugAtTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRBraceFunctionDebugger debugger = new TheRBraceFunctionDebugger(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, executor.getCounter());
    assertEquals(0, handler.getCounter());
    assertEquals(Arrays.asList("error_dbg_at", "error_ls"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(3, executor.getCounter());
    assertEquals(3, handler.getCounter());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void recursiveReturnAndDebugAt() throws TheRDebuggerException {
    /*
    abc() {
      `x + 1` with recursive return and `debug at`
    */

    final RecursiveReturnDebugAtTheRExecutor executor = new RecursiveReturnDebugAtTheRExecutor();
    final RecursiveReturnDebugAtTheRFunctionDebuggerHandler handler = new RecursiveReturnDebugAtTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRBraceFunctionDebugger debugger = new TheRBraceFunctionDebugger(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, executor.getCounter());
    assertEquals(0, handler.getDroppedFramesCounter());
    assertEquals(0, handler.getReturnLinesCounter());
    assertEquals(Arrays.asList("error_dbg_at", "error_ls"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(3, executor.getCounter());
    assertEquals(3, handler.getDroppedFramesCounter());
    assertEquals(3, handler.getReturnLinesCounter());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void print() throws TheRDebuggerException {
    /*
    abc() {
      print(c(1:3))
    }
    */

    final PrintTheRExecutor executor = new PrintTheRExecutor();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRBraceFunctionDebugger debugger = new TheRBraceFunctionDebugger(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      new IllegalTheRFunctionDebuggerHandler(),
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, executor.getCounter());
    assertEquals(Arrays.asList("error_dbg_at", "error_ls"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(3, executor.getCounter());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
    assertEquals(Collections.singletonList("[1] 1 2 3"), receiver.getOutputs());
  }

  @Test
  public void continueTrace() throws TheRDebuggerException {
    /*
    abc() {
      `x + 1` with `continue trace`
    }
    */

    final ContinueTraceTheRExecutor executor = new ContinueTraceTheRExecutor();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRBraceFunctionDebugger debugger = new TheRBraceFunctionDebugger(
      executor,
      new MockTheRFunctionDebuggerFactory(null),
      new IllegalTheRFunctionDebuggerHandler(),
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, executor.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error_dbg_at", "error_ls"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(7, executor.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(
      Arrays.asList("error_continue", "error_entry", "error_entry", "error_dbg_at", "error_ls"),
      receiver.getErrors()
    );

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 4 5 6", debugger.getResult());
    assertEquals(8, executor.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
  }

  private static class OrdinaryTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT + "1: print(c(1))\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at_1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRExecutionResult(
          "[1] 1 2 3\n" +
          TheRDebugConstants.DEBUG_AT + "2: c(1) + 1\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.DEBUG_AT,
          new TextRange(0, 9),
          "error_dbg_at_2"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 5) {
        return new TheRExecutionResult(
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRExecutionResultType.EXITING_FROM,
          new TextRange(20, 29),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class FunctionTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT + "1: def()\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at_1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUGGING_IN + ": def()\n" +
          "debug: {\n" +
          "    .doTrace(" + SERVICE_FUNCTION_PREFIX + "def" + SERVICE_ENTER_FUNCTION_SUFFIX + "(), \"on entry\")\n" +
          "    {\n" +
          "        print(\"x\")\n" +
          "    }\n" +
          "}\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.DEBUGGING_IN,
          TextRange.EMPTY_RANGE,
          "error_debugging"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 4) {
        return new TheRExecutionResult(
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRExecutionResultType.EXITING_FROM,
          new TextRange(20, 29),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class FunctionTheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myCounter = 0;

    @Override
    public void appendDebugger(@NotNull final TheRFunctionDebugger debugger) {
      myCounter++;
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class RecursiveReturnTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT + "1: c(1)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRExecutionResult(
          EXITING_FROM + " ghi()\n" +
          EXITING_FROM + " def()\n" +
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRExecutionResultType.RECURSIVE_EXITING_FROM,
          new TextRange(60, 69),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class RecursiveReturnTheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myCounter = 0;

    @Override
    public void setDropFrames(final int number) {
      myCounter += number;
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class DebugAtTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT + "1: c(1)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRExecutionResult(
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          TheRDebugConstants.DEBUG_AT + "4: x <- c(1)\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRExecutionResultType.EXITING_FROM,
          new TextRange(20, 29),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class DebugAtTheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myCounter = 0;

    @Override
    public void setReturnLineNumber(final int number) {
      myCounter += number;
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class RecursiveReturnDebugAtTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT + "1: c(1)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRExecutionResult(
          EXITING_FROM + " ghi()\n" +
          EXITING_FROM + " def()\n" +
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          TheRDebugConstants.DEBUG_AT + "4: x <- c(1)\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRExecutionResultType.RECURSIVE_EXITING_FROM,
          new TextRange(60, 69),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class RecursiveReturnDebugAtTheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myReturnLinesCounter = 0;
    private int myDroppedFramesCounter = 0;

    @Override
    public void setReturnLineNumber(final int number) {
      myReturnLinesCounter += number;
    }

    @Override
    public void setDropFrames(final int number) {
      myDroppedFramesCounter += number;
    }

    public int getReturnLinesCounter() {
      return myReturnLinesCounter;
    }

    public int getDroppedFramesCounter() {
      return myDroppedFramesCounter;
    }
  }

  private static class PrintTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT + "1: c(1)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRExecutionResult(
          "[1] 1 2 3\n" +
          EXITING_FROM + " abc()\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRExecutionResultType.EXITING_FROM,
          new TextRange(0, 9),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class ContinueTraceTheRExecutor extends MockTheRExecutor {

    @NotNull
    @Override
    protected TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && (getCounter() == 1 || getCounter() == 6)) {
        return new TheRExecutionResult(
          TheRDebugConstants.DEBUG_AT + "1: c(1:3)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRExecutionResult(
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          TheRDebugConstants.DEBUGGING_IN + ": abc()\n" +
          "debug: {\n" +
          "    .doTrace(" + SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_ENTER_FUNCTION_SUFFIX + "(), \"on entry\")\n" +
          "    {\n" +
          "        c(1:3)\n" +
          "    }\n" +
          "}\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.CONTINUE_TRACE,
          new TextRange(20, 29),
          "error_continue"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 4) {
        return new TheRExecutionResult(
          "output",
          TheRExecutionResultType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_entry"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 5) {
        return new TheRExecutionResult(
          TRACING + " abc() on entry \n" +
          "[1] \"abc\"\n" +
          "debug: {\n" +
          "    c(4:6)\n" +
          "}\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRExecutionResultType.START_TRACE_BRACE,
          TextRange.EMPTY_RANGE,
          "error_entry"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 8) {
        return new TheRExecutionResult(
          EXITING_FROM + " abc()\n" +
          "[1] 4 5 6\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRExecutionResultType.EXITING_FROM,
          new TextRange(20, 29),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }
}