package com.jetbrains.ther.debugger.frame;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.mock.AlwaysSameResponseTheRProcess;
import com.jetbrains.ther.debugger.mock.IllegalTheRValueModifier;
import com.jetbrains.ther.debugger.mock.MockTheROutputReceiver;
import com.jetbrains.ther.debugger.mock.MockTheRProcess;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.DEBUG_AT;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TheRVarsLoaderImplTest {

  @Test
  public void empty() throws TheRDebuggerException {
    final String output = "character(0)";
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(output, RESPONSE, TextRange.allOf(output), "error");
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    assertEquals(
      0,
      new TheRVarsLoaderImpl(
        process,
        receiver,
        new IllegalTheRValueModifier(),
        0
      ).load().size()
    );

    assertEquals(1, process.getCounter());
    assertEquals(Collections.singletonList("error"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void ordinary() throws TheRDebuggerException {
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final List<TheRVar> actual = new TheRVarsLoaderImpl(
      new OrdinaryTheRProcess(),
      receiver,
      new IllegalTheRValueModifier(),
      0
    ).load();

    assertEquals(3, actual.size());

    assertEquals("a", actual.get(0).getName());
    assertEquals("[1] \"integer\"", actual.get(0).getType());
    assertEquals("[1] 1 2 3", actual.get(0).getValue());

    assertEquals("b", actual.get(1).getName());
    assertEquals(FUNCTION_TYPE, actual.get(1).getType());
    assertEquals(
      "function(x) {\n" +
      "    x ^ 2\n" +
      "}",
      actual.get(1).getValue()
    );

    assertEquals("c", actual.get(2).getName());
    assertEquals(FUNCTION_TYPE, actual.get(2).getType());
    assertEquals(
      "function(x) {\n" +
      "    x ^ 2\n" +
      "}",
      actual.get(2).getValue()
    );

    assertEquals(
      Arrays.asList("error_ls", "error_ta", "error_va", "error_t4", "error_vb", "error_t6", "error_vc", "error_t8"),
      receiver.getErrors()
    );
    assertTrue(receiver.getOutputs().isEmpty());
  }

  @Test
  public void inDebug() throws TheRDebuggerException {
    final MockTheROutputReceiver receiver = new MockTheROutputReceiver();

    final List<TheRVar> actual = new TheRVarsLoaderImpl(
      new InDebugTheRProcess(),
      receiver,
      new IllegalTheRValueModifier(),
      0
    ).load();

    assertEquals(1, actual.size());
    assertEquals("a", actual.get(0).getName());
    assertEquals("[1] \"integer\"", actual.get(0).getType());
    assertEquals("[1] 1 2 3", actual.get(0).getValue());

    assertEquals(Arrays.asList("error_ls", "error_ta", "error_dbg_at", "error_va"), receiver.getErrors());
    assertTrue(receiver.getOutputs().isEmpty());
  }

  private static class OrdinaryTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        final String output = "[1] \"a\" \"b\" \"c\"\n" +
                              "[4] " +
                              "\"" + SERVICE_FUNCTION_PREFIX + "d" + SERVICE_ENTER_FUNCTION_SUFFIX + "\"";
        // list, function, inner function and service functions

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          "error_ls"
        );
      }

      if (getCounter() == 2) { // type of a
        final String output = "[1] \"integer\"";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          "error_ta"
        );
      }

      if (getCounter() == 3) { // value of a
        final String output = "[1] 1 2 3";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          "error_va"
        );
      }

      if (getCounter() == 4 || getCounter() == 6 || getCounter() == 8 || getCounter() == 9) { // type of b, c, d, e
        final String output = FUNCTION_TYPE;

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          "error_t" + getCounter()
        );
      }

      if (getCounter() == 5) { // value of b
        final String output = "function(x) {\n" +
                              "    x ^ 2\n" +
                              "}";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          "error_vb"
        );
      }

      if (getCounter() == 7) { // value of c
        final String output = "function(x) {\n" +
                              "    x ^ 2\n" +
                              "}\n" +
                              "<" + ENVIRONMENT + ": 0xfffffff>";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          "error_vc"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }

  private static class InDebugTheRProcess extends MockTheRProcess {

    @NotNull
    @Override
    protected TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException {
      if (getCounter() == 1) {
        final String output = "[1] \"a\"";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          "error_ls"
        );
      }

      if (getCounter() == 2) {
        final String output = "[1] \"integer\"";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          "error_ta"
        );
      }

      if (getCounter() == 3) {
        final String output = TheRDebugConstants.DEBUG_AT + "2: print(" + SYS_FRAME_COMMAND + "(0)$a";

        return new TheRProcessResponse(
          output,
          DEBUG_AT,
          TextRange.EMPTY_RANGE,
          "error_dbg_at"
        );
      }

      if (getCounter() == 4) {
        final String output = "[1] 1 2 3";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          "error_va"
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }
}