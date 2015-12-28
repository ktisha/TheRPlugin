package com.jetbrains.ther.io.graphics;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectCoreUtil;
import com.intellij.util.PathUtil;
import com.jetbrains.ther.run.TheRRunConfigurationParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static java.lang.Boolean.parseBoolean;

public class TheRGraphicsUtils {

  @NotNull
  private static final String DEVICE_KEY = "ther.debugger.device";

  @NotNull
  private static final String LIB_DIR_NAME = "libs";

  @NotNull
  private static final String DEVICE_LIB_NAME = "libtherplugin_device.so";

  @NotNull
  private static final String SNAPSHOT_DIR_NAME = "snapshots";

  @NotNull
  public static List<String> calculateInitCommands(@NotNull final Project project,
                                                   @NotNull final TheRRunConfigurationParams runConfigurationParams) {
    if (isDeviceEnabled(runConfigurationParams)) {
      final String libPath = getLibPath(DEVICE_LIB_NAME);

      if (libPath != null) {
        final String snapshotDirPath = getSnapshotDirPath(project);

        if (snapshotDirPath != null) {
          return Arrays.asList(
            LOAD_LIB_COMMAND + "(\"" + libPath + "\")",
            DEVICE_FUNCTION_NAME + " <- function() { .Call(\"" + DEVICE_FUNCTION_NAME + "\", \"" + snapshotDirPath + "\") }",
            SETUP_DEVICE_COMMAND
          );
        }
      }
    }

    return Collections.emptyList();
  }

  @Nullable
  public static String getSnapshotDirPath(@NotNull final Project project) {
    final File dotIdeaDir = new File(project.getBasePath(), ProjectCoreUtil.DIRECTORY_BASED_PROJECT_DIR);
    final File snapshotDir = new File(dotIdeaDir, SNAPSHOT_DIR_NAME);

    if (!(snapshotDir.exists() || snapshotDir.mkdirs())) {
      return null;
    }

    if (!snapshotDir.canWrite()) {
      return null;
    }

    return snapshotDir.getAbsolutePath();
  }

  private static boolean isDeviceEnabled(@NotNull final TheRRunConfigurationParams runConfigurationParams) {
    final Map<String, String> envs = runConfigurationParams.getEnvs();

    return !envs.containsKey(DEVICE_KEY) || parseBoolean(envs.get(DEVICE_KEY));
  }

  @Nullable
  private static String getLibPath(@NotNull final String libName) {
    final File pluginDir = new File(PathUtil.getJarPathForClass(TheRGraphicsUtils.class));
    final File libDir = new File(pluginDir, LIB_DIR_NAME);
    final File libFile = new File(libDir, libName);

    if (!libFile.canRead()) {
      return null;
    }

    return libFile.getAbsolutePath();
  }
}
