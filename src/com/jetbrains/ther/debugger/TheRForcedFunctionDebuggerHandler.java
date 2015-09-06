package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheRForcedFunctionDebuggerHandler implements TheRFunctionDebuggerHandler {

  @NotNull
  private final List<TheRFunctionDebugger> myDebuggers;

  private int myDropFrames;

  @Nullable
  private String myResult;

  public TheRForcedFunctionDebuggerHandler(@NotNull final TheRExecutor executor,
                                           @NotNull final TheRFunctionDebuggerFactory factory,
                                           @NotNull final TheROutputReceiver receiver) throws TheRDebuggerException {
    myDebuggers = new ArrayList<TheRFunctionDebugger>();
    myDropFrames = 1;

    appendDebugger(
      factory.getFunctionDebugger(
        executor,
        this,
        receiver
      )
    );
  }

  public boolean advance() throws TheRDebuggerException {
    topDebugger().advance(); // Don't forget that advance could append new debugger

    while (!myDebuggers.isEmpty() && !topDebugger().hasNext()) {
      if (myDebuggers.size() == 1) {
        return false;
      }

      for (int i = 0; i < myDropFrames; i++) {
        popDebugger();
      }

      myDropFrames = 1;
    }

    return !myDebuggers.isEmpty();
  }

  @NotNull
  public String getResult() {
    if (myResult != null) {
      return myResult;
    }
    else {
      return topDebugger().getResult();
    }
  }

  @Override
  public void appendDebugger(@NotNull final TheRFunctionDebugger debugger) {
    myDebuggers.add(debugger);
  }

  @Override
  public void setReturnLineNumber(final int lineNumber) {
  }

  @Override
  public void setDropFrames(final int number) {
    myDropFrames = number;

    if (myDropFrames == myDebuggers.size()) {
      myResult = topDebugger().getResult();
    }
  }

  @NotNull
  private TheRFunctionDebugger topDebugger() {
    return myDebuggers.get(myDebuggers.size() - 1);
  }

  private void popDebugger() {
    myDebuggers.remove(myDebuggers.size() - 1);
  }
}
