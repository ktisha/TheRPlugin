package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

public class TheRValueModifierFactoryImpl implements TheRValueModifierFactory {

  @NotNull
  @Override
  public TheRValueModifier getModifier(@NotNull final TheRProcess process,
                                       @NotNull final TheRFunctionDebuggerFactory factory,
                                       @NotNull final TheROutputReceiver receiver,
                                       @NotNull final TheRValueModifierHandler handler,
                                       final int frameNumber) {
    return new TheRValueModifierImpl(process, factory, receiver, handler, frameNumber);
  }
}
