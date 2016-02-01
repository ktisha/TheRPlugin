package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public final class TheRInterpreterParameters {

  @NotNull
  public static final String NO_SAVE_PARAMETER = "--no-save";

  @NotNull
  public static final String QUIET_PARAMETER = "--quiet";

  @NotNull
  public static final String ARGS_PARAMETER = "--args";

  @NotNull
  public static final List<String> DEFAULT_PARAMETERS = Arrays.asList(NO_SAVE_PARAMETER, QUIET_PARAMETER);
}
