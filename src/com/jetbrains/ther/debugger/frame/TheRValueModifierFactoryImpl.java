package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import org.jetbrains.annotations.NotNull;

public class TheRValueModifierFactoryImpl implements TheRValueModifierFactory {

  @NotNull
  @Override
  public TheRValueModifier getModifier(@NotNull final TheRExecutor executor,
                                       @NotNull final TheRFunctionDebuggerFactory factory,
                                       @NotNull final TheROutputReceiver receiver,
                                       @NotNull final TheRValueModifierHandler handler,
                                       final int frameNumber) {
    return new TheRValueModifierImpl(executor, factory, receiver, handler, frameNumber);
  }
}
