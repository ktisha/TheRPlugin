package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

public class TheRVarsLoaderFactoryImpl implements TheRVarsLoaderFactory {

  @NotNull
  private final TheRExecutor myExecutor;

  @NotNull
  private final TheROutputReceiver myReceiver;

  public TheRVarsLoaderFactoryImpl(@NotNull final TheRExecutor executor, @NotNull final TheROutputReceiver receiver) {
    myExecutor = executor;
    myReceiver = receiver;
  }

  @NotNull
  @Override
  public TheRVarsLoader getLoader(@NotNull final TheRValueModifier modifier,
                                  final int frameNumber) {
    return new TheRVarsLoaderImpl(myExecutor, myReceiver, modifier, frameNumber);
  }
}
