package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.TheRDebuggerUtils;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultTypeException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.data.TheRCommands.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;

class TheRValueModifierImpl implements TheRValueModifier {

  @NotNull
  private final TheRExecutor myExecutor;

  @NotNull
  private final TheRFunctionDebuggerFactory myFactory;

  @NotNull
  private final TheROutputReceiver myReceiver;

  @NotNull
  private final TheRValueModifierHandler myHandler;

  private final int myFrameNumber;

  public TheRValueModifierImpl(@NotNull final TheRExecutor executor,
                               @NotNull final TheRFunctionDebuggerFactory factory,
                               @NotNull final TheROutputReceiver receiver,
                               @NotNull final TheRValueModifierHandler handler,
                               final int frameNumber) {
    myExecutor = executor;
    myFactory = factory;
    myReceiver = receiver;
    myHandler = handler;
    myFrameNumber = frameNumber;
  }

  @Override
  public boolean isEnabled() {
    return myHandler.isModificationAvailable(myFrameNumber);
  }

  @Override
  public void setValue(@NotNull final String name, @NotNull final String value, @NotNull final Listener listener) {
    if (!isEnabled()) {
      throw new IllegalStateException("SetValue could be called only if isEnabled returns true");
    }

    try {
      doSetValue(name, value, listener);
    }
    catch (final TheRDebuggerException e) {
      listener.onError(e);
    }
  }

  private void doSetValue(@NotNull final String name, @NotNull final String value, @NotNull final Listener listener)
    throws TheRDebuggerException {
    final TheRExecutionResult result = execute(myExecutor, name + " <- " + value, myReceiver);

    switch (result.getType()) {
      case EMPTY:
        if (result.getError().isEmpty()) {
          listener.onSuccess();
        }
        else {
          listener.onError(result.getError());
        }

        return;
      case DEBUGGING_IN:
        TheRDebuggerUtils.forciblyEvaluateFunction(myExecutor, myFactory, myReceiver);

        listener.onSuccess();

        return;
      case DEBUG_AT:
        execute(myExecutor, EXECUTE_AND_STEP_COMMAND, RESPONSE, myReceiver);

        listener.onSuccess();

        return;
      default:
        throw new TheRUnexpectedExecutionResultTypeException(
          "Actual type is not the same as expected: " +
          "[" +
          "actual: " + result.getType() + ", " +
          "expected: " +
          "[" + DEBUGGING_IN + ", " + EMPTY + ", " + DEBUG_AT + "]" +
          "]"
        );
    }
  }
}
