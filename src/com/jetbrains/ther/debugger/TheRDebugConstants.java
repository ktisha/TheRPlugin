package com.jetbrains.ther.debugger;

import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;

public final class TheRDebugConstants {

  @NotNull
  public static final String LINE_SEPARATOR = LineSeparator.getSystemLineSeparator().getSeparatorString();

  public static final long INITIAL_SLEEP = 50;
  public static final int DEFAULT_BUFFER = 1024;

  @NotNull
  public static final String SERVICE_FUNCTION_PREFIX = "intellij_ther_";

  @NotNull
  public static final String SERVICE_ENTER_FUNCTION_SUFFIX = "_enter";

  @NotNull
  public static final String SERVICE_EXIT_FUNCTION_SUFFIX = "_exit";

  // interpreter parameters

  @NotNull
  public static final String NO_SAVE_PARAMETER = "--no-save";

  @NotNull
  public static final String QUIET_PARAMETER = "--quiet";

  // commands

  @NotNull
  public static final String BROWSER_COMMAND = "browser()";

  public static final char PING_COMMAND = '#';

  @NotNull
  public static final String LS_COMMAND = "ls()";

  @NotNull
  public static final String TYPEOF_COMMAND = "typeof";

  @NotNull
  public static final String TRACE_COMMAND = "trace";

  @NotNull
  public static final String DEBUG_COMMAND = "debug";

  // language

  public static final char COMMENT_SYMBOL = '#';

  @NotNull
  public static final String FUNCTION_TYPE = "[1] \"closure\"";
}
