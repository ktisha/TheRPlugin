package com.jetbrains.ther.debugger.executor;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;

public final class TheRProcessUtils {

  @NotNull
  public static List<String> getStartOptions() {
    return Arrays.asList(NO_SAVE_PARAMETER, QUIET_PARAMETER);
  }

  @NotNull
  public static List<String> getInitCommands() {
    return Arrays.asList(BROWSER_COMMAND, KEEP_SOURCE_COMMAND);
  }

  @NotNull
  public static List<String> getInitDeviceCommands(@NotNull final String libPath, @NotNull final String snapshotDirPath) {
    return Arrays.asList(
      LOAD_LIB_COMMAND + "(\"" + libPath + "\")",
      DEVICE_FUNCTION_NAME + " <- function() { .Call(\"" + DEVICE_FUNCTION_NAME + "\", \"" + snapshotDirPath + "\") }",
      SETUP_DEVICE_COMMAND
    );
  }
}
