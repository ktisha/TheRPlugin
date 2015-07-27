package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

public class TheRStackFrame {

  @NotNull
  private final TheRLocation myLocation;

  @NotNull
  private final TheRVarsLoader myLoader;

  @NotNull
  private final TheRDebuggerEvaluator myEvaluator;

  public TheRStackFrame(@NotNull final TheRLocation location,
                        @NotNull final TheRVarsLoader loader,
                        @NotNull final TheRDebuggerEvaluator evaluator,
                        final boolean last) {
    myLocation = location;
    myLoader = loader;
    myEvaluator = evaluator;

    if (last) {
      myLoader.markAsLast();
    }
    else {
      myLoader.markAsNotLast();
    }
  }

  @NotNull
  public TheRLocation getLocation() {
    return myLocation;
  }

  @NotNull
  public TheRVarsLoader getLoader() {
    return myLoader;
  }

  @NotNull
  public TheRDebuggerEvaluator getEvaluator() {
    return myEvaluator;
  }

  public void markAsLast() {
    myLoader.markAsLast();
  }

  public void markAsNotLast() {
    myLoader.markAsNotLast();
  }
}
