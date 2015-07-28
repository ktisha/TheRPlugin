package com.jetbrains.ther.debugger.interpreter;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.mock.AlwaysSameResponseTheRProcess;
import com.jetbrains.ther.debugger.mock.TheROutputErrorReceiver;
import org.junit.Test;

import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.PLUS;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.RESPONSE;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;
import static org.junit.Assert.assertEquals;

public class TheRProcessUtilsTest {

  @Test(expected = TheRDebuggerException.class)
  public void invalidCommandExecuting() throws TheRDebuggerException {
    final String output = "abc";
    final TheRProcessResponseType type = RESPONSE;
    final TextRange resultRange = TextRange.allOf(output);
    final String error = "";

    final TheRProcess process = new AlwaysSameResponseTheRProcess(output, type, resultRange, error);

    execute(process, "def", PLUS);
  }

  @Test
  public void correctCommandExecuting() throws TheRDebuggerException {
    final String output = "abc";
    final TheRProcessResponseType type = RESPONSE;
    final TextRange resultRange = TextRange.allOf(output);
    final String error = "";

    final TheRProcess process = new AlwaysSameResponseTheRProcess(output, type, resultRange, error);
    final TheRProcessResponse response = execute(process, "def", RESPONSE);

    assertEquals(output, response.getOutput());
    assertEquals(RESPONSE, response.getType());
    assertEquals(resultRange, response.getResultRange());
    assertEquals(error, response.getError());
  }

  @Test
  public void errorCommandExecuting() throws TheRDebuggerException {
    final String output = "abc";
    final TheRProcessResponseType type = RESPONSE;
    final TextRange resultRange = TextRange.allOf(output);
    final String error = "error";

    final TheRProcess process = new AlwaysSameResponseTheRProcess(output, type, resultRange, error);
    final TheROutputErrorReceiver receiver = new TheROutputErrorReceiver("error");

    execute(process, "def", RESPONSE, receiver);

    assertEquals(1, receiver.getErrorReceived());
  }
}