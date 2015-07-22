package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

public interface TheRDebuggerEvaluatorFactory {

  @NotNull
  TheRDebuggerEvaluator getEvaluator(@NotNull final TheRProcess process,
                                     @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                     @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                     @NotNull final TheRLoadableVarHandler varHandler,
                                     @NotNull final TheRLocation location);
}
