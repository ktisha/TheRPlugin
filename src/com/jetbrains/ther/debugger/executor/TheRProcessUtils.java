package com.jetbrains.ther.debugger.executor;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRCommands.BROWSER_COMMAND;
import static com.jetbrains.ther.debugger.data.TheRCommands.optionsCommands;

public final class TheRProcessUtils {

  @NotNull
  public static List<String> getInitCommands() {
    return Arrays.asList(BROWSER_COMMAND, optionsCommands("keep.source", "TRUE"));
  }
}
