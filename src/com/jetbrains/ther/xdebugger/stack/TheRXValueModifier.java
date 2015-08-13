package com.jetbrains.ther.xdebugger.stack;

import com.intellij.xdebugger.frame.XValueModifier;
import com.jetbrains.ther.debugger.frame.TheRValueModifier;
import com.jetbrains.ther.xdebugger.TheRXDebugRunner;
import org.jetbrains.annotations.NotNull;

// TODO [xdbg][test]
class TheRXValueModifier extends XValueModifier {

  @NotNull
  private final TheRValueModifier myModifier;

  @NotNull
  private final String myName;

  public TheRXValueModifier(@NotNull final TheRValueModifier modifier, @NotNull final String name) {
    myModifier = modifier;
    myName = name;
  }

  @Override
  public void setValue(@NotNull final String expression, @NotNull final XModificationCallback callback) {
    TheRXDebugRunner.SINGLE_EXECUTOR.execute(
      new Runnable() {
        @Override
        public void run() {
          myModifier.setValue(
            myName,
            expression,
            new Listener(callback)
          );
        }
      }
    );
  }

  private static class Listener implements TheRValueModifier.Listener {

    @NotNull
    private final XModificationCallback myCallback;

    public Listener(@NotNull final XModificationCallback callback) {
      myCallback = callback;
    }

    @Override
    public void onSuccess() {
      myCallback.valueModified();
    }

    @Override
    public void onError(@NotNull final String error) {
      myCallback.errorOccurred(error);
    }

    @Override
    public void onError(@NotNull final Exception e) {
      onError(e.getMessage());
    }
  }
}
