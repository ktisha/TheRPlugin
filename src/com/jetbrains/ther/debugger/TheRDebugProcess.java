package com.jetbrains.ther.debugger;

import com.intellij.execution.ExecutionException;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TheRDebugProcess extends XDebugProcess {

  @NotNull
  private final TheRDebugger myDebugger;

  public TheRDebugProcess(@NotNull XDebugSession session, @NotNull String interpreterPath, @NotNull String filePath)
    throws ExecutionException {
    super(session);

    try {
      myDebugger = new TheRDebugger(interpreterPath, filePath);
    }
    catch (IOException | InterruptedException e) {
      throw new ExecutionException(e); // TODO
    }
  }

  @NotNull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return new TheRDebuggerEditorsProvider();
  }

  @Override
  public void startStepOver() {
    try {
      myDebugger.executeInstruction();
    }
    catch (IOException | InterruptedException e) {
      // TODO
    }
  }

  @Override
  public void startStepInto() {
    // TODO
  }

  @Override
  public void startStepOut() {
    // TODO
  }

  @Override
  public void resume() {
    try {
      while (myDebugger.executeInstruction()) {
      }
    }
    catch (IOException | InterruptedException e) {
      // TODO
    }
  }

  @Override
  public void runToPosition(@NotNull XSourcePosition position) {
    // TODO
  }

  @Override
  public void stop() {
    // TODO
  }
}
