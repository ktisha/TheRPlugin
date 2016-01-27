package com.jetbrains.ther.run.debug.resolve;

import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.ther.debugger.data.TheRLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TheRResolvingSession {

  @Nullable
  XSourcePosition resolveNext(@NotNull final TheRLocation nextLocation);

  @Nullable
  XSourcePosition resolveCurrent(final int line);

  void dropLast(final int number);
}
