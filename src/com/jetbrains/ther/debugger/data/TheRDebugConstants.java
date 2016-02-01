package com.jetbrains.ther.debugger.data;

import com.intellij.util.LineSeparator;
import org.jetbrains.annotations.NotNull;

public final class TheRDebugConstants {

  @NotNull
  public static final String LINE_SEPARATOR = LineSeparator.getSystemLineSeparator().getSeparatorString();

  @NotNull
  public static final String SERVICE_FUNCTION_PREFIX = "jetbrains_ther_";

  @NotNull
  public static final String SERVICE_ENTER_FUNCTION_SUFFIX = "_enter";

  @NotNull
  public static final String MAIN_FUNCTION_NAME = SERVICE_FUNCTION_PREFIX + "main";

  // commands

  @NotNull
  public static final String BROWSER_COMMAND = "browser()";

  @NotNull
  public static final String KEEP_SOURCE_COMMAND = "options(keep.source=TRUE)";

  @NotNull
  public static final String LOAD_LIB_COMMAND = "dyn.load";

  @NotNull
  public static final String LS_COMMAND = "ls";

  @NotNull
  public static final String TYPEOF_COMMAND = "typeof";

  @NotNull
  public static final String TRACE_COMMAND = "trace";

  @NotNull
  public static final String DEBUG_COMMAND = "debug";

  @NotNull
  public static final String ATTR_COMMAND = "attr";

  @NotNull
  public static final String EXECUTE_AND_STEP_COMMAND = "n";

  @NotNull
  public static final String SYS_FRAME_COMMAND = "sys.frame";

  @NotNull
  public static final String SYS_NFRAME_COMMAND = "sys.nframe()";

  @NotNull
  public static final String IS_DEBUGGED_COMMAND = "isdebugged";

  @NotNull
  public static final String EAPPLY_COMMAND = "eapply";

  @NotNull
  public static final String FILTER_COMMAND = "Filter";

  @NotNull
  public static final String BODY_COMMAND = "body";

  @NotNull
  public static final String SOURCE_COMMAND = "source";

  @NotNull
  public static final String QUIT_COMMAND = "q()";

  // responses

  @NotNull
  public static final String PROMPT = "> ";

  @NotNull
  public static final String PLUS_AND_SPACE = "+ ";

  @NotNull
  public static final String BROWSE_PREFIX = "Browse[";

  @NotNull
  public static final String BROWSE_SUFFIX = "]" + PROMPT;

  @NotNull
  public static final String DEBUGGING_IN = "debugging in";

  @NotNull
  public static final String DEBUG_AT = "debug at #";

  @NotNull
  public static final String DEBUG = "debug";

  @NotNull
  public static final String TRACING = "Tracing";

  @NotNull
  public static final String EXITING_FROM = "exiting from:";

  @NotNull
  public static final String ENVIRONMENT = "environment";

  // language

  @NotNull
  public static final String CLOSURE = "closure";

  @NotNull
  public static final String FUNCTION_TYPE = "[1] \"" + CLOSURE + "\"";

  @NotNull
  public static final String FOR_LOOP_PREFIX = "for";

  @NotNull
  public static final String WHILE_LOOP_PREFIX = "while";
}
