package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRFunction;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TheRDebuggedFunctionsTest {

  @NotNull
  private static final TheRFunction F1 = new TheRFunction(Collections.singletonList("f1"));

  @NotNull
  private static final TheRFunction F1_A = new TheRFunction(Arrays.asList("f1", "a"));

  @NotNull
  private static final TheRFunction F1_A_B = new TheRFunction(Arrays.asList("f1", "a", "b"));

  @Test
  public void addAndRemoveTopLevel() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1, false, false);

    debuggedFunctions.add(F1);

    checkFunction(debuggedFunctions, F1, true, false);

    debuggedFunctions.remove(F1);

    checkFunction(debuggedFunctions, F1, false, false);
  }

  @Test
  public void addAndRemoveInner() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1, false, false);

    debuggedFunctions.add(F1_A);

    checkFunction(debuggedFunctions, F1_A, true, false);
    checkFunction(debuggedFunctions, F1, false, true);

    debuggedFunctions.remove(F1_A);

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1, false, false);
  }

  @Test
  public void addInnerAndRemoveTopLevel() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1, false, false);

    debuggedFunctions.add(F1_A);

    checkFunction(debuggedFunctions, F1_A, true, false);
    checkFunction(debuggedFunctions, F1, false, true);

    debuggedFunctions.remove(F1);

    checkFunction(debuggedFunctions, F1_A, true, false);
    checkFunction(debuggedFunctions, F1, false, true);
  }

  @Test
  public void addTopLevelAndRemoveInner() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1, false, false);

    debuggedFunctions.add(F1);

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1, true, false);

    debuggedFunctions.remove(F1_A);

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1, true, false);
  }

  @Test
  public void addAndRemoveHierarchy() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1, false, false);
    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_A_B, false, false);

    debuggedFunctions.add(F1);

    checkFunction(debuggedFunctions, F1, true, false);
    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_A_B, false, false);

    debuggedFunctions.add(F1_A);

    checkFunction(debuggedFunctions, F1, true, true);
    checkFunction(debuggedFunctions, F1_A, true, false);
    checkFunction(debuggedFunctions, F1_A_B, false, false);

    debuggedFunctions.add(F1_A_B);

    checkFunction(debuggedFunctions, F1, true, true);
    checkFunction(debuggedFunctions, F1_A, true, true);
    checkFunction(debuggedFunctions, F1_A_B, true, false);

    debuggedFunctions.remove(F1);

    checkFunction(debuggedFunctions, F1, false, true);
    checkFunction(debuggedFunctions, F1_A, true, true);
    checkFunction(debuggedFunctions, F1_A_B, true, false);

    debuggedFunctions.remove(F1_A);

    checkFunction(debuggedFunctions, F1, false, true);
    checkFunction(debuggedFunctions, F1_A, false, true);
    checkFunction(debuggedFunctions, F1_A_B, true, false);

    debuggedFunctions.remove(F1_A_B);

    checkFunction(debuggedFunctions, F1, false, false);
    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_A_B, false, false);
  }

  private void checkFunction(@NotNull final TheRDebuggedFunctions debuggedFunctions,
                             @NotNull final TheRFunction function,
                             final boolean isDebugged,
                             final boolean hasDebuggedInner) {
    assertEquals(isDebugged, debuggedFunctions.isDebugged(function));
    assertEquals(hasDebuggedInner, debuggedFunctions.hasDebuggedInner(function));
  }
}