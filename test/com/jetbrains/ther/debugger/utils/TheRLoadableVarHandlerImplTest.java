package com.jetbrains.ther.debugger.utils;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.mock.IllegalTheRProcess;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TheRLoadableVarHandlerImplTest {

  @Test
  public void functionType() throws IOException, InterruptedException {
    assertEquals(
      TheRDebugConstants.FUNCTION_TYPE,
      new TheRLoadableVarHandlerImpl().handleType(
        new TraceAndDebugTheRProcess(),
        "abc",
        TheRDebugConstants.FUNCTION_TYPE
      )
    );
  }

  @Test
  public void serviceFunctionType() throws IOException, InterruptedException {
    assertNull(
      new TheRLoadableVarHandlerImpl().handleType(
        new IllegalTheRProcess(),
        TheRDebugConstants.SERVICE_FUNCTION_PREFIX + "abc",
        TheRDebugConstants.FUNCTION_TYPE
      )
    );
  }

  @Test
  public void ordinaryType() throws IOException, InterruptedException {
    assertEquals(
      "typeAbc",
      new TheRLoadableVarHandlerImpl().handleType(
        new IllegalTheRProcess(),
        "abc",
        "typeAbc"
      )
    );
  }

  @Test
  public void functionValue() {
    assertEquals(
      "function(x) {\n" +
      "    x ^ 2\n" +
      "}",
      new TheRLoadableVarHandlerImpl().handleValue(
        "abc",
        TheRDebugConstants.FUNCTION_TYPE,
        "Object with tracing code, class \"functionWithTrace\"\n" +
        "Original definition:\n" +
        "function(x) {\n" +
        "    x ^ 2\n" +
        "}\n" +
        "\n" +
        "## (to see the tracing code, look at body(object))"
      )
    );
  }

  @Test
  public void ordinaryValue() {
    assertEquals(
      "valueAbc",
      new TheRLoadableVarHandlerImpl().handleValue(
        "abc",
        "typeAbc",
        "valueAbc"
      )
    );
  }

  private static class TraceAndDebugTheRProcess extends TheRProcess {

    private int myCounter = 0;

    @NotNull
    @Override
    public TheRProcessResponse execute(@NotNull final String command) throws IOException, InterruptedException {
      if (myCounter > 3) {
        throw new IllegalStateException("Unexpected command");
      }

      if (myCounter == 2) {
        myCounter++;

        return new TheRProcessResponse(
          "text",
          TheRProcessResponseType.RESPONSE,
          TextRange.EMPTY_RANGE
        );
      }

      myCounter++;

      return new TheRProcessResponse(
        "text",
        TheRProcessResponseType.EMPTY,
        TextRange.EMPTY_RANGE
      );
    }

    @Override
    public void stop() {
    }
  }
}