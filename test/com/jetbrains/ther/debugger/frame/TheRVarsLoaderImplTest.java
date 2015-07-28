package com.jetbrains.ther.debugger.frame;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.mock.AlwaysSameResponseTheRProcess;
import com.jetbrains.ther.debugger.mock.IllegalTheROutputReceiver;
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

    assertEquals(1, process.getExecuteCalled());
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

  private static class OrdinaryTheRProcess implements TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
      if (myCounter == 0) {
        myCounter++;

        final String output = "[1] \"a\" \"b\" \"c\"\n" +
                              "[4] " +
                              "\"" + SERVICE_FUNCTION_PREFIX + "d" + SERVICE_ENTER_FUNCTION_SUFFIX + "\" " +
                              "\"" + SERVICE_FUNCTION_PREFIX + "e" + SERVICE_EXIT_FUNCTION_SUFFIX + "\"";
        // list, function, inner function and service functions

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          ""
        );
      }

      if (myCounter == 1) { // type of a
        myCounter++;

        final String output = "[1] \"integer\"";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          ""
        );
      }

      if (myCounter == 2) { // value of a
        myCounter++;

        final String output = "[1] 1 2 3";

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          ""
        );
      }

      if (myCounter == 3 || myCounter == 5 || myCounter == 7 || myCounter == 8) { // type of b, c, d, e
        myCounter++;

        final String output = FUNCTION_TYPE;

        return new TheRProcessResponse(
          output,
          RESPONSE,
          TextRange.allOf(output),
          ""
        );
      }

      if (myCounter == 4) { // value of b
        myCounter++;

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

      if (myCounter == 6) { // value of c
        myCounter++;

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

    @Override
    public void stop() {
    }
  }
}