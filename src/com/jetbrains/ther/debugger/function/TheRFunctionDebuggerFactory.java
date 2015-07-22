package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

public interface TheRFunctionDebuggerFactory {

  @NotNull
  TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                  @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                  @NotNull final TheRLoadableVarHandler varHandler,
                                                  @NotNull final TheRLocation prevLocation) throws TheRDebuggerException;

  @NotNull
  TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                               @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                               @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                               @NotNull final TheRLoadableVarHandler varHandler,
                                               @NotNull final TheRScriptReader scriptReader);
}
