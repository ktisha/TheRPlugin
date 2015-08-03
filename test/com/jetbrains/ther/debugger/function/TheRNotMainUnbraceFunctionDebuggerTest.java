package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import com.jetbrains.ther.debugger.mock.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.LS_FUNCTIONS_COMMAND;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.NO_FUNCTIONS_RESPONSE;
import static org.junit.Assert.*;

public class TheRNotMainUnbraceFunctionDebuggerTest {

  @Test
  public void ordinary() throws TheRDebuggerException {
    /*
    `x + 1`
    */

    final OrdinaryTheRProcess process = new OrdinaryTheRProcess();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainUnbraceFunctionDebugger debugger = new TheRNotMainUnbraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheRFunctionDebuggerHandler(),
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(1, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_ls"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
  }

  @Test
  public void function() throws TheRDebuggerException {
    /*
    f()
    */

    final FunctionTheRProcess process = new FunctionTheRProcess();
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(new IllegalTheRFunctionDebugger(), null);
    final FunctionTheRFunctionDebuggerHandler handler = new FunctionTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainUnbraceFunctionDebugger debugger = new TheRNotMainUnbraceFunctionDebugger(
      process,
      factory,
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(1, process.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(0, handler.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_ls"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals(2, process.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(1, factory.getNotMainCounter());
    assertEquals(1, handler.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_debugging"), receiver.getErrors());
  }

  @Test
  public void recursiveReturn() throws TheRDebuggerException {
    /*
    `x + 1` with recursive return
    */

    final RecursiveReturnTheRProcess process = new RecursiveReturnTheRProcess();
    final RecursiveReturnTheRFunctionDebuggerHandler handler = new RecursiveReturnTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainUnbraceFunctionDebugger debugger = new TheRNotMainUnbraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(1, process.getCounter());
    assertEquals(0, handler.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_ls"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertEquals(3, handler.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
  }

  @Test
  public void debugAt() throws TheRDebuggerException {
    /*
    `x + 1` with `debug at` at the end
    */

    final DebugAtTheRProcess process = new DebugAtTheRProcess();
    final DebugAtTheRFunctionDebuggerHandler handler = new DebugAtTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainUnbraceFunctionDebugger debugger = new TheRNotMainUnbraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(1, process.getCounter());
    assertEquals(0, handler.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_ls"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertEquals(5, handler.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
  }

  @Test
  public void recursiveReturnAndDebugAt() throws TheRDebuggerException {
    /*
    `x + 1` with recursive return and `debug at` at the end
    */

    final RecursiveReturnAndDebugAtTheRProcess process = new RecursiveReturnAndDebugAtTheRProcess();
    final RecursiveReturnAndDebugAtTheRFunctionDebuggerHandler handler = new RecursiveReturnAndDebugAtTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainUnbraceFunctionDebugger debugger = new TheRNotMainUnbraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(1, process.getCounter());
    assertEquals(0, handler.getDroppedFramesCounter());
    assertEquals(0, handler.getReturnLineNumberCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_ls"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertEquals(3, handler.getDroppedFramesCounter());
    assertEquals(5, handler.getReturnLineNumberCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
  }

  @Test
  public void print() throws TheRDebuggerException {
    /*
    `print(x + 1)`
    */

    final PrintTheRProcess process = new PrintTheRProcess();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainUnbraceFunctionDebugger debugger = new TheRNotMainUnbraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheRFunctionDebuggerHandler(),
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(1, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_ls"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertEquals(Collections.singletonList("[1] 1 2 3"), receiver.getOutputs());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
  }

  @Test
  public void continueTrace() throws TheRDebuggerException {
    /*
    `x + 1` with `continue trace`
    */

    final ContinueTraceTheRProcess process = new ContinueTraceTheRProcess();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainUnbraceFunctionDebugger debugger = new TheRNotMainUnbraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheRFunctionDebuggerHandler(),
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(1, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_ls"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(5, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error_continue", "error3", "error_entry", "error_ls"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 4 5 6", debugger.getResult());
    assertEquals(6, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error_exit"), receiver.getErrors());
  }

  private static class OrdinaryTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          EXITING_FROM + " abc(c(1:10))\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.EXITING_FROM,
          new TextRange(27, 36),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class FunctionTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          DEBUGGING_IN + ": abc(c(1:10))\n" +
          "debug: {\n" +
          "    .doTrace(" + SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_ENTER_FUNCTION_SUFFIX + "(), \"on entry\")\n" +
          "    {\n" +
          "        x + 1\n" +
          "    }\n" +
          "}\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUGGING_IN,
          TextRange.EMPTY_RANGE,
          "error_debugging"
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

  private static class RecursiveReturnTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
          EXITING_FROM + " def()\n" +
          EXITING_FROM + " abc(1:10)\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.RECURSIVE_EXITING_FROM,
          new TextRange(86, 95),
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

  private static class DebugAtTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          EXITING_FROM + " abc(c(1:10))\n" +
          "[1] 1 2 3\n" +
          DEBUG_AT + "6: x <- c(1)\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.EXITING_FROM,
          new TextRange(27, 36),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class DebugAtTheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myCounter = 0;

    @Override
    public void setReturnLineNumber(final int lineNumber) {
      myCounter += lineNumber;
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class RecursiveReturnAndDebugAtTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
          EXITING_FROM + " def()\n" +
          EXITING_FROM + " abc(1:10)\n" +
          "[1] 1 2 3\n" +
          DEBUG_AT + "6: x <- c(1)" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.RECURSIVE_EXITING_FROM,
          new TextRange(86, 95),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class RecursiveReturnAndDebugAtTheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myReturnLineNumberCounter = 0;
    private int myDroppedFramesCounter = 0;

    @Override
    public void setDropFrames(final int number) {
      myDroppedFramesCounter += number;
    }

    @Override
    public void setReturnLineNumber(final int lineNumber) {
      myReturnLineNumberCounter += lineNumber;
    }

    public int getReturnLineNumberCounter() {
      return myReturnLineNumberCounter;
    }

    public int getDroppedFramesCounter() {
      return myDroppedFramesCounter;
    }
  }

  private static class PrintTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          "[1] 1 2 3\n" +
          EXITING_FROM + " abc(c(1:10))\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.EXITING_FROM,
          new TextRange(0, 9),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class ContinueTraceTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(TheRTraceAndDebugUtils.LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error_ls"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 2) {
        return new TheRProcessResponse(
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
          TheRProcessResponseType.CONTINUE_TRACE,
          new TextRange(20, 29),
          "error_continue"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRProcessResponse(
          "output",
          TheRProcessResponseType.RESPONSE,
          TextRange.EMPTY_RANGE,
          "error3"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 4) {
        return new TheRProcessResponse(
          TRACING + " abc() on entry \n" +
          "[1] \"abc\"\n" +
          "debug: c(4:6)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.START_TRACE_UNBRACE,
          TextRange.EMPTY_RANGE,
          "error_entry"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 6) {
        return new TheRProcessResponse(
          EXITING_FROM + " abc()\n" +
          "[1] 4 5 6\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.EXITING_FROM,
          new TextRange(20, 29),
          "error_exit"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }
}