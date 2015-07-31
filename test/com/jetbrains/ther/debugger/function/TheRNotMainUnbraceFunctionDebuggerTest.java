package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import com.jetbrains.ther.debugger.mock.IllegalTheRFunctionDebugger;
import com.jetbrains.ther.debugger.mock.IllegalTheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.mock.MockTheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.LS_FUNCTIONS_COMMAND;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtils.NO_FUNCTIONS_RESPONSE;
import static org.junit.Assert.*;

public class TheRNotMainUnbraceFunctionDebuggerTest {

  @Test
  public void ordinary1() throws TheRDebuggerException {
    /*
    `x + 1`
    */

    final Ordinary1TheRProcess process = new Ordinary1TheRProcess();
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
    assertEquals(Collections.singletonList("error1"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error2"), receiver.getErrors());
  }

  @Test
  public void ordinary2() throws TheRDebuggerException {
    /*
    f()
    */

    final Ordinary2TheRProcess process = new Ordinary2TheRProcess();
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(new IllegalTheRFunctionDebugger(), null);
    final Ordinary2TheRFunctionDebuggerHandler handler = new Ordinary2TheRFunctionDebuggerHandler();
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
    assertEquals(Collections.singletonList("error1"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals(2, process.getCounter());
    assertEquals(0, factory.getMainCounter());
    assertEquals(1, factory.getNotMainCounter());
    assertEquals(1, handler.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error2"), receiver.getErrors());
  }

  @Test
  public void ordinary3() throws TheRDebuggerException {
    /*
    `x + 1` with recursive return
    */

    final Ordinary3TheRProcess process = new Ordinary3TheRProcess();
    final Ordinary3TheRFunctionDebuggerHandler handler = new Ordinary3TheRFunctionDebuggerHandler();
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
    assertEquals(Collections.singletonList("error1"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertEquals(3, handler.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error2"), receiver.getErrors());
  }

  @Test
  public void ordinary4() throws TheRDebuggerException {
    /*
    `x + 1` with `debug at` at the end
    */

    final Ordinary4TheRProcess process = new Ordinary4TheRProcess();
    final Ordinary4TheRFunctionDebuggerHandler handler = new Ordinary4TheRFunctionDebuggerHandler();
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
    assertEquals(Collections.singletonList("error1"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertEquals(5, handler.getCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error2"), receiver.getErrors());
  }

  @Test
  public void ordinary5() throws TheRDebuggerException {
    /*
    `x + 1` with recursive return and `debug at` at the end
    */

    final Ordinary5TheRProcess process = new Ordinary5TheRProcess();
    final Ordinary5TheRFunctionDebuggerHandler handler = new Ordinary5TheRFunctionDebuggerHandler();
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
    assertEquals(Collections.singletonList("error1"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertEquals(3, handler.getDroppedFramesCounter());
    assertEquals(5, handler.getReturnLineNumberCounter());
    assertTrue(receiver.getOutputs().isEmpty());
    assertEquals(Collections.singletonList("error2"), receiver.getErrors());
  }

  @Test
  public void ordinary6() throws TheRDebuggerException {
    /*
    `print(x + 1)`
    */

    final Ordinary6TheRProcess process = new Ordinary6TheRProcess();
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
    assertEquals(Collections.singletonList("error1"), receiver.getErrors());

    receiver.reset();
    debugger.advance();

    assertFalse(debugger.hasNext());
    assertEquals(new TheRLocation("abc", -1), debugger.getLocation());
    assertEquals("[1] 1 2 3", debugger.getResult());
    assertEquals(2, process.getCounter());
    assertEquals(Collections.singletonList("[1] 1 2 3"), receiver.getOutputs());
    assertEquals(Collections.singletonList("error2"), receiver.getErrors());
  }

  private static class Ordinary1TheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      myCounter++;

      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          TRACING + " abc(c(1:10)) on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc(c(1:10))\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.END_TRACE,
          new TextRange(67, 76),
          "error2"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class Ordinary2TheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      myCounter++;

      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          DEBUGGING_IN + ": abc(c(1:10))\n" +
          "debug: {\n" +
          "    on.exit(.doTrace(" + SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_EXIT_FUNCTION_SUFFIX + "(), \"on exit\"))\n" +
          "    {\n" +
          "        .doTrace(" + SERVICE_FUNCTION_PREFIX + "abc" + SERVICE_ENTER_FUNCTION_SUFFIX + "(), \"on entry\")\n" +
          "        {\n" +
          "            x + 1\n" +
          "        }\n" +
          "    }\n" +
          "}\n" +
          BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
          TheRProcessResponseType.DEBUGGING_IN,
          TextRange.EMPTY_RANGE,
          "error2"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class Ordinary2TheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myCounter = 0;

    @Override
    public void appendDebugger(@NotNull final TheRFunctionDebugger debugger) {
      myCounter++;
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class Ordinary3TheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      myCounter++;

      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          TRACING + " ghi() on exit \n" +
          "[1] \"ghi\"\n" +
          EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
          TRACING + " def() on exit \n" +
          "[1] \"def\"\n" +
          EXITING_FROM + " def()\n" +
          TRACING + " abc(1:10) on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc(1:10)\n" +
          "[1] 1 2 3\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.RECURSIVE_END_TRACE,
          new TextRange(189, 198),
          "error2"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class Ordinary3TheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myCounter = 0;

    @Override
    public void setDropFrames(final int number) {
      myCounter += number;
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class Ordinary4TheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      myCounter++;

      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          TRACING + " abc(c(1:10)) on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc(c(1:10))\n" +
          "[1] 1 2 3\n" +
          DEBUG_AT + "6: x <- c(1)\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.END_TRACE,
          new TextRange(67, 76),
          "error2"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class Ordinary4TheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myCounter = 0;

    @Override
    public void setReturnLineNumber(final int lineNumber) {
      myCounter += lineNumber;
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class Ordinary5TheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      myCounter++;

      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          TRACING + " ghi() on exit \n" +
          "[1] \"ghi\"\n" +
          EXITING_FROM + " FUN(c(-1, 0, 1)[[3L]], ...)\n" +
          TRACING + " def() on exit \n" +
          "[1] \"def\"\n" +
          EXITING_FROM + " def()\n" +
          TRACING + " abc(1:10) on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc(1:10)\n" +
          "[1] 1 2 3\n" +
          DEBUG_AT + "6: x <- c(1)" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.RECURSIVE_END_TRACE,
          new TextRange(189, 198),
          "error2"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }

    public int getCounter() {
      return myCounter;
    }
  }

  private static class Ordinary5TheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

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

  private static class Ordinary6TheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      myCounter++;

      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        return new TheRProcessResponse(
          NO_FUNCTIONS_RESPONSE,
          TheRProcessResponseType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESPONSE),
          "error1"
        );
      }

      if (command.equals(EXECUTE_AND_STEP_COMMAND)) {
        return new TheRProcessResponse(
          "[1] 1 2 3\n" +
          TRACING + " abc(c(1:10)) on exit \n" +
          "[1] \"abc\"\n" +
          EXITING_FROM + " abc(c(1:10))\n" +
          BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
          TheRProcessResponseType.END_TRACE,
          new TextRange(0, 9),
          "error2"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }

    @Override
    public void stop() {
    }

    public int getCounter() {
      return myCounter;
    }
  }
}