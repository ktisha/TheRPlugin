package com.jetbrains.ther.debugger.frame;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.mock.AlwaysSameResponseTheRProcess;
import com.jetbrains.ther.debugger.mock.IllegalTheROutputReceiver;
import com.jetbrains.ther.debugger.mock.MockTheRProcess;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.RESPONSE;
import static org.junit.Assert.assertEquals;

public class TheRVarsLoaderImplTest {

  @Test
  public void empty() throws TheRDebuggerException {
    final String output = "character(0)";
    final AlwaysSameResponseTheRProcess process = new AlwaysSameResponseTheRProcess(output, RESPONSE, TextRange.allOf(output), "");

    assertEquals(
      0,
      new TheRVarsLoaderImpl(
        process,
        new IllegalTheROutputReceiver(),
        0
      ).load().size()
    );

    assertEquals(1, process.getCounter());
  }

  @Test
  public void ordinary() throws TheRDebuggerException {
    final List<TheRVar> actual = new TheRVarsLoaderImpl(
      new OrdinaryTheRProcess(),
      new IllegalTheROutputReceiver(),
      0
    ).load();

    final ArrayList<TheRVar> expected = new ArrayList<TheRVar>();
    expected.add(
      new TheRVar(
        "a",
        "[1] \"integer\"",
        "[1] 1 2 3"
      )
    );

    expected.add(
      new TheRVar(
        "b",
        FUNCTION_TYPE,
        "function(x) {\n" +
        "    x ^ 2\n" +
        "}"
      )
    );

    expected.add(
      new TheRVar(
        "c",
        FUNCTION_TYPE,
        "function(x) {\n" +
        "    x ^ 2\n" +
        "}"
      )
    );

    assertEquals(expected, actual);
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
          ""
        );
      }

      if (getCounter() == 2) { // type of a
        final String output = "[1] \"integer\"";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          ""
        );
      }

      if (getCounter() == 3) { // value of a
        final String output = "[1] 1 2 3";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          ""
        );
      }

      if (getCounter() == 4 || getCounter() == 6 || getCounter() == 8 || getCounter() == 9) { // type of b, c, d, e
        final String output = FUNCTION_TYPE;

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          ""
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
          ""
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
          ""
        );
      }

      throw new IllegalStateException("Unexpected command");
    }
  }
}