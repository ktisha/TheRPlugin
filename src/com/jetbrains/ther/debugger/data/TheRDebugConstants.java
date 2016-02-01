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
