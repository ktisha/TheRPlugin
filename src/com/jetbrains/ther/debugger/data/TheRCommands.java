package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;

public class TheRCommands {

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
}
