package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

public interface TheRFunctionDebuggerFactory {

  @NotNull
  TheRFunctionDebugger getFunctionDebugger(@NotNull final TheRExecutor executor,
                                           @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                           @NotNull final TheROutputReceiver outputReceiver) throws TheRDebuggerException;
}
