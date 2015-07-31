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

public class TheRNotMainBraceFunctionDebuggerTest {

  @Test
  public void ordinary() throws TheRDebuggerException {
    /*
    abc() {
      instruction1
      instruction2
    }
    */

    final OrdinaryTheRProcess process = new OrdinaryTheRProcess();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainBraceFunctionDebugger debugger = new TheRNotMainBraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheRFunctionDebuggerHandler(),
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, process.getCounter());
    assertEquals(Arrays.asList("error1", "error2"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 1), debugger.getLocation());
    assertEquals(4, process.getCounter());
    assertEquals(Arrays.asList("error3", "error2"), receiver.getErrors());
    assertEquals(Collections.singletonList("[1] 1 2 3"), receiver.getOutputs());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(5, process.getCounter());
    assertEquals(Collections.singletonList("error4"), receiver.getErrors());
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

    final FunctionTheRProcess process = new FunctionTheRProcess();
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(new IllegalTheRFunctionDebugger(), null);
    final FunctionTheRFunctionDebuggerHandler handler = new FunctionTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainBraceFunctionDebugger debugger = new TheRNotMainBraceFunctionDebugger(
      process,
      factory,
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, process.getCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, handler.getCounter());
    assertEquals(Arrays.asList("error1", "error2"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(3, process.getCounter());
    assertEquals(1, factory.getNotMainCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(1, handler.getCounter());
    assertEquals(Collections.singletonList("error3"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(4, process.getCounter());
    assertEquals(1, factory.getNotMainCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(1, handler.getCounter());
    assertEquals(Collections.singletonList("error4"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void recursiveReturn() throws TheRDebuggerException {
    /*
    abc() {
      `x + 1` with recursive return
    }
    */

    final RecursiveReturnTheRProcess process = new RecursiveReturnTheRProcess();
    final RecursiveReturnTheRFunctionDebuggerHandler handler = new RecursiveReturnTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainBraceFunctionDebugger debugger = new TheRNotMainBraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, process.getCounter());
    assertEquals(0, handler.getCounter());
    assertEquals(Arrays.asList("error1", "error2"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(3, process.getCounter());
    assertEquals(3, handler.getCounter());
    assertEquals(Collections.singletonList("error3"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void debugAt() throws TheRDebuggerException {
    /*
    abc() {
      `x + 1` with `debug at`
    }
    */

    final DebugAtTheRProcess process = new DebugAtTheRProcess();
    final DebugAtTheRFunctionDebuggerHandler handler = new DebugAtTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainBraceFunctionDebugger debugger = new TheRNotMainBraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, process.getCounter());
    assertEquals(0, handler.getCounter());
    assertEquals(Arrays.asList("error1", "error2"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(3, process.getCounter());
    assertEquals(3, handler.getCounter());
    assertEquals(Collections.singletonList("error3"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void recursiveReturnAndDebugAt() throws TheRDebuggerException {
    /*
    abc() {
      `x + 1` with recursive return and `debug at`
    */

    final RecursiveReturnDebugAtTheRProcess process = new RecursiveReturnDebugAtTheRProcess();
    final RecursiveReturnDebugAtTheRFunctionDebuggerHandler handler = new RecursiveReturnDebugAtTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainBraceFunctionDebugger debugger = new TheRNotMainBraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      handler,
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, process.getCounter());
    assertEquals(0, handler.getDroppedFramesCounter());
    assertEquals(0, handler.getReturnLinesCounter());
    assertEquals(Arrays.asList("error1", "error2"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(3, process.getCounter());
    assertEquals(3, handler.getDroppedFramesCounter());
    assertEquals(3, handler.getReturnLinesCounter());
    assertEquals(Collections.singletonList("error3"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void print() throws TheRDebuggerException {
    /*
    abc() {
      print(c(1:3))
    }
    */

    final PrintTheRProcess process = new PrintTheRProcess();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainBraceFunctionDebugger debugger = new TheRNotMainBraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheRFunctionDebuggerHandler(),
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, process.getCounter());
    assertEquals(Arrays.asList("error1", "error2"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(3, process.getCounter());
    assertEquals(Collections.singletonList("error3"), receiver.getErrors());
    assertEquals(Collections.singletonList("[1] 1 2 3"), receiver.getOutputs());
  }

  @Test
  public void continueTrace() throws TheRDebuggerException {
    /*
    abc() {
      `x + 1` with `continue trace`
    }
    */

    final ContinueTraceTheRProcess process = new ContinueTraceTheRProcess();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRNotMainBraceFunctionDebugger debugger = new TheRNotMainBraceFunctionDebugger(
      process,
      new MockTheRFunctionDebuggerFactory(null, null),
      new IllegalTheRFunctionDebuggerHandler(),
      receiver,
      "abc"
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(2, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error2", "error1"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation("abc", 0), debugger.getLocation());
    assertEquals(9, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Arrays.asList("error3", "error4", "error5", "error6", "error7", "error2", "error1"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 4 5 6", debugger.getResult());
    assertEquals(10, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error10"), receiver.getErrors());
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
          "error2"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "1: print(c(1))\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRProcessResponse(
          "[1] 1 2 3\n" +
          TheRDebugConstants.DEBUG_AT + "2: c(1) + 1\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUG_AT,
          new TextRange(0, 9),
          "error3"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 5) {
        return new TheRProcessResponse(
          TRACING + " abc() on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.END_TRACE,
          new TextRange(53, 62),
          "error4"
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
          "error2"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "1: def()\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUGGING_IN + ": def()\n" +
          "debug: {\n" +
          "    on.exit(.doTrace(" + SERVICE_FUNCTION_PREFIX + "def" + SERVICE_EXIT_FUNCTION_SUFFIX + "(), \"on exit\"))\n" +
          "    {\n" +
          "        .doTrace(" + SERVICE_FUNCTION_PREFIX + "def" + SERVICE_ENTER_FUNCTION_SUFFIX + "(), \"on entry\")\n" +
          "        {\n" +
          "            print(\"x\")\n" +
          "        }\n" +
          "    }\n" +
          "}\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUGGING_IN,
          TextRange.EMPTY_RANGE,
          "error3"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 4) {
        return new TheRProcessResponse(
          TRACING + " abc() on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.END_TRACE,
          new TextRange(53, 62),
          "error4"
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
          "error2"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "1: c(1)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRProcessResponse(
          TRACING + " ghi() on exit \n" +
          "[1] \"ghi\"\n" +
          EXITING_FROM + " ghi()\n" +
          TRACING + " def() on exit \n" +
          "[1] \"def\"\n" +
          EXITING_FROM + " def()\n" +
          TRACING + " abc() on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.RECURSIVE_END_TRACE,
          new TextRange(159, 168),
          "error3"
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
          "error2"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "1: c(1)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRProcessResponse(
          TRACING + " abc() on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          TheRDebugConstants.DEBUG_AT + "4: x <- c(1)\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.END_TRACE,
          new TextRange(53, 62),
          "error3"
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

  private static class RecursiveReturnDebugAtTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error2"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "1: c(1)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRProcessResponse(
          TRACING + " ghi() on exit \n" +
          "[1] \"ghi\"\n" +
          EXITING_FROM + " ghi()\n" +
          TRACING + " def() on exit \n" +
          "[1] \"def\"\n" +
          EXITING_FROM + " def()\n" +
          TRACING + " abc() on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          TheRDebugConstants.DEBUG_AT + "4: x <- c(1)\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.RECURSIVE_END_TRACE,
          new TextRange(159, 168),
          "error3"
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

  private static class PrintTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error2"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 1) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "1: c(1)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRProcessResponse(
          "[1] 1 2 3\n" +
          TRACING + " abc() on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc()\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.END_TRACE,
          new TextRange(0, 9),
          "error3"
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
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && (getCounter() == 1 || getCounter() == 8)) {
        return new TheRProcessResponse(
          TheRDebugConstants.DEBUG_AT + "1: c(1:3)\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error2"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 3) {
        return new TheRProcessResponse(
          TRACING + " abc() on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc()\n" +
          "[1] 1 2 3\n" +
          TheRDebugConstants.DEBUGGING_IN + ": abc()\n" +
          "debug: {\n" +
          "    on.exit(.doTrace(" + SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_EXIT_FUNCTION_SUFFIX + "(), \"on exit\"))\n" +
          "    {\n" +
          "        .doTrace(" + SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_ENTER_FUNCTION_SUFFIX + "(), \"on entry\")\n" +
          "        {\n" +
          "            c(1:3)\n" +
          "        }\n" +
          "    }\n" +
          "}\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.CONTINUE_TRACE,
          new TextRange(53, 62),
          "error3"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && 4 <= getCounter() && getCounter() <= 6) {
        return new TheRProcessResponse(
          "output",
          TheRProcessResponseType.RESPONSE,
          TextRange.EMPTY_RANGE,
          "error" + getCounter()
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 7) {
        return new TheRProcessResponse(
          TRACING + " abc() on entry \n" +
          "[1] \"abc\"\n" +
          "debug: {\n" +
          "    c(4:6)\n" +
          "}\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.START_TRACE_BRACE,
          TextRange.EMPTY_RANGE,
          "error7"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND) && getCounter() == 10) {
        return new TheRProcessResponse(
          TRACING + " abc() on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc()\n" +
          "[1] 4 5 6\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.END_TRACE,
          new TextRange(53, 62),
          "error10"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }
}