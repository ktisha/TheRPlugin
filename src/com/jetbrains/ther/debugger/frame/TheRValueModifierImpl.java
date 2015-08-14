package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.TheRForcedFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedResponseException;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;

class TheRValueModifierImpl implements TheRValueModifier {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheRFunctionDebuggerFactory myFactory;

  @NotNull
  private final TheROutputReceiver myReceiver;

  @NotNull
  private final TheRValueModifierHandler myHandler;

  private final int myFrameNumber;

  public TheRValueModifierImpl(@NotNull final TheRProcess process,
                               @NotNull final TheRFunctionDebuggerFactory factory,
                               @NotNull final TheROutputReceiver receiver,
                               @NotNull final TheRValueModifierHandler handler,
                               final int frameNumber) {
    myProcess = process;
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
    final TheRProcessResponse response = execute(myProcess, name + " <- " + value, myReceiver);

    switch (response.getType()) {
      case EMPTY:
        if (response.getError().isEmpty()) {
          listener.onSuccess();
        }
        else {
          listener.onError(response.getError());
        }

        return;
      case DEBUGGING_IN:
        runFunction();

        listener.onSuccess();

        return;
      case DEBUG_AT:
        execute(myProcess, EXECUTE_AND_STEP_COMMAND, RESPONSE, myReceiver);

        listener.onSuccess();

        return;
      default:
        throw new TheRUnexpectedResponseException(
          "Actual response type is not the same as expected: " +
          "[" +
          "actual: " + response.getType() + ", " +
          "expected: " +
          "[" + DEBUGGING_IN + ", " + EMPTY + ", " + DEBUG_AT + "]" +
          "]"
        );
    }
  }

  private void runFunction() throws TheRDebuggerException {
    final TheRForcedFunctionDebuggerHandler handler = new TheRForcedFunctionDebuggerHandler(
      myProcess,
      myFactory,
      myReceiver
    );

    //noinspection StatementWithEmptyBody
    while (handler.advance()) {
    }
  }
}
