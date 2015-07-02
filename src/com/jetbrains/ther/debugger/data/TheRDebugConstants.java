package com.jetbrains.ther.debugger.data;

import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;

public final class TheRDebugConstants {

  @NotNull
  public static final String LINE_SEPARATOR = LineSeparator.getSystemLineSeparator().getSeparatorString();

  public static final long INITIAL_SLEEP = 2;
  public static final int DEFAULT_BUFFER = 2048;

  @NotNull
  public static final String SERVICE_FUNCTION_PREFIX = "intellij_ther_";

  @NotNull
  public static final String SERVICE_ENTER_FUNCTION_SUFFIX = "_enter";

  @NotNull
  public static final String SERVICE_EXIT_FUNCTION_SUFFIX = "_exit";

  @NotNull
  public static final String MAIN_FUNCTION_NAME = "intellij_ther_main";

  // interpreter parameters

  @NotNull
  public static final String NO_SAVE_PARAMETER = "--no-save";

  @NotNull
  public static final String QUIET_PARAMETER = "--quiet";

  // commands

  @NotNull
  public static final String BROWSER_COMMAND = "browser()";

  @NotNull
  public static final String KEEP_SOURCE_COMMAND = "options(keep.source=TRUE)";

  @NotNull
  public static final String NOP_COMMAND = "#";

  @NotNull
  public static final String LS_COMMAND = "ls()";

  @NotNull
  public static final String TYPEOF_COMMAND = "typeof";

  @NotNull
  public static final String TRACE_COMMAND = "trace";

  @NotNull
  public static final String DEBUG_COMMAND = "debug";

  @NotNull
  public static final String EXECUTE_AND_STEP_COMMAND = "n";

  // responses

  @NotNull
  public static final String PLUS_AND_SPACE = "+ ";

  @NotNull
  public static final String BROWSE_PREFIX = "Browse[";

  @NotNull
  public static final String BROWSE_SUFFIX = "]> ";

  @NotNull
  public static final String DEBUGGING_IN = "debugging in";

  @NotNull
  public static final String DEBUG_AT = "debug at #";

  @NotNull
  public static final String TRACING = "Tracing";

  // language

  public static final char COMMENT_SYMBOL = '#';

  @NotNull
  public static final String FUNCTION_TYPE = "[1] \"closure\"";
}
