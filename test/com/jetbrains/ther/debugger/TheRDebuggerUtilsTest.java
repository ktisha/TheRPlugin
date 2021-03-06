package com.jetbrains.ther.debugger;

import org.junit.Test;

import static com.jetbrains.ther.debugger.data.TheRResponseConstants.ENVIRONMENT_PREFIX;
import static org.junit.Assert.assertEquals;

public class TheRDebuggerUtilsTest {

  @Test
  public void outerFunctionValueHandling() {
    assertEquals(
      "function(x) {\n" +
      "    x ^ 2\n" +
      "}",
      TheRDebuggerUtils.calculateRepresentation(
        "function(x) {\n" +
        "    x ^ 2\n" +
        "}"
      )
    );
  }

  @Test
  public void innerFunctionValueHandling() {
    assertEquals(
      "function(x) {\n" +
      "    x ^ 2\n" +
      "}",
      TheRDebuggerUtils.calculateRepresentation(
        "function(x) {\n" +
        "    x ^ 2\n" +
        "}\n" +
        ENVIRONMENT_PREFIX + "0xfffffff>"
      )
    );
  }
}