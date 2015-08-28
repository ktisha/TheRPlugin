package com.jetbrains.ther.debugger.function;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRScriptLine;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.mock.IllegalTheRFunctionDebugger;
import com.jetbrains.ther.debugger.mock.IllegalTheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.mock.MockTheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtilsTest.LS_FUNCTIONS_COMMAND;
import static com.jetbrains.ther.debugger.function.TheRTraceAndDebugUtilsTest.NO_FUNCTIONS_RESULT;
import static org.junit.Assert.*;

public class TheRMainFunctionDebuggerTest {

  @Test
  public void ordinary() throws TheRDebuggerException {
    final MockTheRExecutor executor = new MockTheRExecutor();
    final MockTheRFunctionDebuggerFactory factory = new MockTheRFunctionDebuggerFactory(new IllegalTheRFunctionDebugger(), null);
    final MockTheRFunctionDebuggerHandler handler = new MockTheRFunctionDebuggerHandler();
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final TheRMainFunctionDebugger debugger = new TheRMainFunctionDebugger(
      executor,
      factory,
      handler,
      receiver,
      new MockTheRScriptReader()
    );

    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 0), debugger.getLocation());

    executor.reset();
    receiver.reset();
    debugger.advance();

    assertTrue(executor.check0());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(0, handler.myCounter);
    assertTrue(receiver.getErrors().isEmpty());
    assertTrue(receiver.getOutputs().isEmpty());
    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 1), debugger.getLocation());

    executor.reset();
    receiver.reset();
    debugger.advance();

    assertTrue(executor.check1());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(0, handler.myCounter);
    assertEquals(Arrays.asList("error1", "error_ls_fun"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 2), debugger.getLocation());

    executor.reset();
    receiver.reset();
    debugger.advance();

    assertTrue(executor.check2());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(0, handler.myCounter);
    assertEquals(Arrays.asList("error_f1", "error_f2", "error_f3", "error_f4", "error_f5", "error_ls_fun"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 7), debugger.getLocation());

    executor.reset();
    receiver.reset();
    debugger.advance();

    assertTrue(executor.check3());
    assertEquals(0, factory.getMainCounter());
    assertEquals(0, factory.getNotMainCounter());
    assertEquals(0, handler.myCounter);
    assertEquals(Arrays.asList("error_ls_all", "error_ls_fun"), receiver.getErrors());
    assertEquals(Collections.singletonList("character(0)"), receiver.getOutputs());
    assertTrue(debugger.hasNext());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, 10), debugger.getLocation());

    executor.reset();
    receiver.reset();
    debugger.advance();

    assertTrue(executor.check4());
    assertEquals(0, factory.getMainCounter());
    assertEquals(1, factory.getNotMainCounter());
    assertEquals(1, handler.myCounter);
    assertEquals(Collections.singletonList("error_f_call"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
    assertFalse(debugger.hasNext());
    assertEquals("", debugger.getResult());
    assertEquals(new TheRLocation(MAIN_FUNCTION_NAME, -1), debugger.getLocation());
  }

  private static class MockTheRExecutor implements TheRExecutor {

    private int myExecuted = 0;
    private int myTraceAndDebugExecuted = 0;

    private boolean my1Executed = false;

    private boolean my20Executed = false;
    private boolean my21Executed = false;
    private boolean my22Executed = false;
    private boolean my23Executed = false;
    private boolean my24Executed = false;

    private boolean my3Executed = false;

    private boolean my4Executed = false;

    public void reset() {
      myExecuted = 0;
      myTraceAndDebugExecuted = 0;

      my1Executed = false;

      my20Executed = false;
      my21Executed = false;
      my22Executed = false;
      my23Executed = false;
      my24Executed = false;

      my3Executed = false;

      my4Executed = false;
    }

    public boolean check0() {
      return myExecuted == 0;
    }

    public boolean check1() {
      return myExecuted == 2 && my1Executed && myTraceAndDebugExecuted == 1;
    }

    public boolean check2() {
      return myExecuted == 6 &&
             my20Executed &&
             my21Executed &&
             my22Executed &&
             my23Executed &&
             my24Executed &&
             myTraceAndDebugExecuted == 1;
    }

    public boolean check3() {
      return myExecuted == 2 && my3Executed && myTraceAndDebugExecuted == 1;
    }

    public boolean check4() {
      return myExecuted == 1 && my4Executed;
    }

    @NotNull
    @Override
    public TheRExecutionResult execute(@NotNull final String command) throws TheRDebuggerException {
      myExecuted++;

      if (command.equals("x <- c(1:10)")) {
        my1Executed = true;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.EMPTY,
          TextRange.EMPTY_RANGE,
          "error1"
        );
      }

      if (command.equals(LS_FUNCTIONS_COMMAND)) {
        myTraceAndDebugExecuted++;

        return new TheRExecutionResult(
          NO_FUNCTIONS_RESULT,
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf(NO_FUNCTIONS_RESULT),
          "error_ls_fun"
        );
      }

      if (command.equals("f <- function(x) {")) {
        my20Executed = true;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.PLUS,
          TextRange.EMPTY_RANGE,
          "error_f1"
        );
      }

      if (command.equals("# comment in function")) {
        my21Executed = true;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.PLUS,
          TextRange.EMPTY_RANGE,
          "error_f2"
        );
      }

      if (command.equals(" ")) {
        my22Executed = true;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.PLUS,
          TextRange.EMPTY_RANGE,
          "error_f3"
        );
      }

      if (command.equals("x + 1")) {
        my23Executed = true;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.PLUS,
          TextRange.EMPTY_RANGE,
          "error_f4"
        );
      }

      if (command.equals("}")) {
        my24Executed = true;

        return new TheRExecutionResult(
          "",
          TheRExecutionResultType.EMPTY,
          TextRange.EMPTY_RANGE,
          "error_f5"
        );
      }

      if (command.equals(LS_COMMAND + "()")) {
        my3Executed = true;

        return new TheRExecutionResult(
          "character(0)",
          TheRExecutionResultType.RESPONSE,
          TextRange.allOf("character(0)"),
          "error_ls_all"
        );
      }

      if (command.equals("f(x)")) {
        my4Executed = true;

        return new TheRExecutionResult(
          DEBUGGING_IN + ": f(x)\n" +
          "debug: {\n" +
          "    # comment in function\n" +
          "     \n" +
          "    x + 1\n" +
          "}",
          TheRExecutionResultType.DEBUGGING_IN,
          TextRange.EMPTY_RANGE,
          "error_f_call"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class MockTheRFunctionDebuggerHandler extends IllegalTheRFunctionDebuggerHandler {

    private int myCounter = 0;

    @Override
    public void appendDebugger(@NotNull final TheRFunctionDebugger debugger) {
      myCounter++;
    }
  }

  private static class MockTheRScriptReader implements TheRScriptReader {

    @NotNull
    private final String[] myCommands = new String[]{
      NOP_COMMAND,
      "x <- c(1:10)",
      "f <- function(x) {",
      "# comment in function",
      " ", // empty line in function
      "x + 1",
      "}",
      "ls()",
      "# comment in script",
      "  ", // empty line in script
      "f(x)",
    };

    private int myCurrentNumber = 0;

    @NotNull
    @Override
    public TheRScriptLine getCurrentLine() {
      if (myCurrentNumber > myCommands.length - 1) {
        myCurrentNumber = -1;
      }

      if (myCurrentNumber == -1) {
        return new TheRScriptLine(null, -1);
      }

      return new TheRScriptLine(myCommands[myCurrentNumber], myCurrentNumber);
    }

    @Override
    public void advance() throws IOException {
      if (myCurrentNumber > myCommands.length - 1) {
        myCurrentNumber = -1;
      }

      if (myCurrentNumber == -1) {
        return;
      }

      myCurrentNumber++;
    }

    @Override
    public void close() throws IOException {
    }
  }
}