package com.jetbrains.ther.debugger.executor;

import org.jetbrains.annotations.NotNull;

import java.io.File;
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
  public static List<String> getInitDeviceCommands(@NotNull final String libPath) {
    return Arrays.asList(
      LOAD_LIB_COMMAND + "(\"" + libPath + File.separator + DEVICE_LIB_NAME + "\")",
      DEFINE_DEVICE_FUNCTION_COMMAND,
      SETUP_DEVICE_COMMAND
    );
  }
}
