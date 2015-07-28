package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

public class TheRVarsLoaderFactoryImpl implements TheRVarsLoaderFactory {

  @NotNull
  private final TheRProcess myProcess;

  @NotNull
  private final TheROutputReceiver myReceiver;

  public TheRVarsLoaderFactoryImpl(@NotNull final TheRProcess process, @NotNull final TheROutputReceiver receiver) {
    myProcess = process;
    myReceiver = receiver;
  }

  @NotNull
  @Override
  public TheRVarsLoader getLoader(final int frameNumber) {
    return new TheRVarsLoaderImpl(myProcess, myReceiver, frameNumber);
  }
}
