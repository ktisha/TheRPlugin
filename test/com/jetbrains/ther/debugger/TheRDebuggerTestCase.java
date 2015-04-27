package com.jetbrains.ther.debugger;

import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

public abstract class TheRDebuggerTestCase extends TestCase {

  // TODO implement tests for list, unlist, dataframes, etc.

  @NotNull
  protected final TheRDebugger myDebugger;

  @NotNull
  protected final Map<String, String> myVarToRepresentation;

  @NotNull
  protected final Map<String, String> myVarToType;

  public TheRDebuggerTestCase(@NotNull String name) throws IOException, InterruptedException {
    myDebugger = new TheRDebugger("R", "./testData/debugger/" + name + ".r");

    myVarToRepresentation = myDebugger.getVarRepresentations();
    myVarToType = myDebugger.getVarTypes();
  }

  protected void checkSize(int size) {
    assertEquals(size, myVarToRepresentation.size());
    assertEquals(size, myVarToType.size());
  }

  protected void checkVariable(@NotNull String var,
                               @NotNull String expectedValue,
                               @NotNull String expectedType) {
    assertTrue(myVarToRepresentation.containsKey(var));
    assertTrue(myVarToType.containsKey(var));

    assertEquals(expectedValue, myVarToRepresentation.get(var));
    assertEquals(expectedType, myVarToType.get(var));
  }
}
