package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

public interface TheRValueModifierFactory {

  @NotNull
  TheRValueModifier getModifier(@NotNull final TheRProcess process,
                                @NotNull final TheRFunctionDebuggerFactory factory,
                                @NotNull final TheROutputReceiver receiver,
                                @NotNull final TheRValueModifierHandler handler,
                                final int frameNumber);
}
