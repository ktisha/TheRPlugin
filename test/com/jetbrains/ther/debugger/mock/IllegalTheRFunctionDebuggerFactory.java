package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.*;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class IllegalTheRFunctionDebuggerFactory implements TheRFunctionDebuggerFactory {

  @NotNull
  @Override
  public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                         @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                         @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                         @NotNull final TheRFunctionResolver functionResolver,
                                                         @NotNull final TheRLoadableVarHandler varHandler,
                                                         @NotNull final TheRLocation prevLocation)
    throws IOException, InterruptedException {
    throw new IllegalStateException("GetNotMainFunctionDebugger shouldn't be called");
  }

  @NotNull
  @Override
  public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                      @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                      @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                      @NotNull final TheRFunctionResolver functionResolver,
                                                      @NotNull final TheRLoadableVarHandler varHandler,
                                                      @NotNull final TheRScriptReader scriptReader) {
    throw new IllegalStateException("GetMainFunctionDebugger shouldn't be called");
  }
}
