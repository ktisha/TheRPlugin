package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import org.jetbrains.annotations.NotNull;

public interface TheRValueModifierFactory {

  @NotNull
  TheRValueModifier getModifier(@NotNull final TheRExecutor executor,
                                @NotNull final TheRFunctionDebuggerFactory factory,
                                @NotNull final TheROutputReceiver receiver,
                                @NotNull final TheRValueModifierHandler handler,
                                final int frameNumber);
}
