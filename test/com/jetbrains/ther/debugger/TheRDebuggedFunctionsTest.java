package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TheRDebuggedFunctionsTest {

  @NotNull
  private static final String F1 = "f1";

  @NotNull
  private static final List<String> F1_AS_LIST = Collections.singletonList(F1);

  @NotNull
  private static final List<String> F1_A = Arrays.asList("f1", "a");

  @NotNull
  private static final List<String> F1_A_B = Arrays.asList("f1", "a", "b");

  @Test
  public void addAndRemoveTopLevel() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);

    debuggedFunctions.add(F1);

    checkFunction(debuggedFunctions, F1_AS_LIST, true, false);

    debuggedFunctions.remove(F1);

    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);
  }

  @Test
  public void addAndRemoveTopLevelAsList() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);

    debuggedFunctions.add(F1_AS_LIST);

    checkFunction(debuggedFunctions, F1_AS_LIST, true, false);

    debuggedFunctions.remove(F1_AS_LIST);

    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);
  }

  @Test
  public void addAndRemoveInner() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);

    debuggedFunctions.add(F1_A);

    checkFunction(debuggedFunctions, F1_A, true, false);
    checkFunction(debuggedFunctions, F1_AS_LIST, false, true);

    debuggedFunctions.remove(F1_A);

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);
  }

  @Test
  public void addInnerAndRemoveTopLevel() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);

    debuggedFunctions.add(F1_A);

    checkFunction(debuggedFunctions, F1_A, true, false);
    checkFunction(debuggedFunctions, F1_AS_LIST, false, true);

    debuggedFunctions.remove(F1);

    checkFunction(debuggedFunctions, F1_A, true, false);
    checkFunction(debuggedFunctions, F1_AS_LIST, false, true);
  }

  @Test
  public void addTopLevelAndRemoveInner() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);

    debuggedFunctions.add(F1);

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_AS_LIST, true, false);

    debuggedFunctions.remove(F1_A);

    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_AS_LIST, true, false);
  }

  @Test
  public void addAndRemoveHierarchy() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);
    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_A_B, false, false);

    debuggedFunctions.add(F1);

    checkFunction(debuggedFunctions, F1_AS_LIST, true, false);
    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_A_B, false, false);

    debuggedFunctions.add(F1_A);

    checkFunction(debuggedFunctions, F1_AS_LIST, true, true);
    checkFunction(debuggedFunctions, F1_A, true, false);
    checkFunction(debuggedFunctions, F1_A_B, false, false);

    debuggedFunctions.add(F1_A_B);

    checkFunction(debuggedFunctions, F1_AS_LIST, true, true);
    checkFunction(debuggedFunctions, F1_A, true, true);
    checkFunction(debuggedFunctions, F1_A_B, true, false);

    debuggedFunctions.remove(F1);

    checkFunction(debuggedFunctions, F1_AS_LIST, false, true);
    checkFunction(debuggedFunctions, F1_A, true, true);
    checkFunction(debuggedFunctions, F1_A_B, true, false);

    debuggedFunctions.remove(F1_A);

    checkFunction(debuggedFunctions, F1_AS_LIST, false, true);
    checkFunction(debuggedFunctions, F1_A, false, true);
    checkFunction(debuggedFunctions, F1_A_B, true, false);

    debuggedFunctions.remove(F1_A_B);

    checkFunction(debuggedFunctions, F1_AS_LIST, false, false);
    checkFunction(debuggedFunctions, F1_A, false, false);
    checkFunction(debuggedFunctions, F1_A_B, false, false);
  }

  @Test
  public void addAndRemoveEmpty() {
    final TheRDebuggedFunctions debuggedFunctions = new TheRDebuggedFunctions();

    checkFunction(debuggedFunctions, Collections.<String>emptyList(), false, false);

    debuggedFunctions.add(Collections.<String>emptyList());

    checkFunction(debuggedFunctions, Collections.<String>emptyList(), true, false);

    debuggedFunctions.remove(Collections.<String>emptyList());

    checkFunction(debuggedFunctions, Collections.<String>emptyList(), false, false);
  }

  private void checkFunction(@NotNull final TheRDebuggedFunctions debuggedFunctions,
                             @NotNull final List<String> function,
                             final boolean isDebugged,
                             final boolean hasDebuggedInner) {
    if (function.size() == 1) {
      final String f = function.get(0);

      assertEquals(isDebugged, debuggedFunctions.isDebugged(f));
      assertEquals(hasDebuggedInner, debuggedFunctions.hasDebuggedInner(f));
    }

    assertEquals(isDebugged, debuggedFunctions.isDebugged(function));
    assertEquals(hasDebuggedInner, debuggedFunctions.hasDebuggedInner(function));
  }
}