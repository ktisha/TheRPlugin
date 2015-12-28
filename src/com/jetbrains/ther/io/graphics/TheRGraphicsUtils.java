package com.jetbrains.ther.io.graphics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectCoreUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.jetbrains.ther.run.TheRRunConfigurationParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static java.lang.Boolean.parseBoolean;

public class TheRGraphicsUtils {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsUtils.class);

  @NotNull
  private static final String DEVICE_KEY = "ther.debugger.device";

  @NotNull
  private static final String DEVICE_IS_DISABLED = "Device is disabled";

  @NotNull
  private static final String LIB_DIR_NAME = "libs";

  @NotNull
  private static final String DEVICE_LIB_NAME = "libtherplugin_device.so";

  @NotNull
  private static final String LIB_IS_NOT_FOUND = "Lib is not found";

  @NotNull
  private static final String LIB_IS_NOT_READABLE = "Lib is not readable";

  @NotNull
  private static final String DOT_IDEA_DIR_IS_NOT_FOUND = ProjectCoreUtil.DIRECTORY_BASED_PROJECT_DIR + " is not found";

  @NotNull
  private static final String SNAPSHOT_DIR_NAME = "snapshots";

  @NotNull
  private static final String SNAPSHOT_DIR_IS_NOT_FOUND = "Snapshot dir is not found";

  @NotNull
  private static final String SNAPSHOT_DIR_IS_FOUND = "Snapshot dir is found";

  @NotNull
  private static final String SNAPSHOT_DIR_IS_NOT_WRITABLE = "Snapshot dir is not writable";

  @NotNull
  private static final String SNAPSHOT_DIR_HAS_BEEN_CREATED = "Snapshot dir has been created";

  @NotNull
  private static final Pattern SNAPSHOT_NAME_PATTERN = Pattern.compile("^snapshot_(\\d+)\\.png$");

  @NotNull
  private static final String SNAPSHOT_NAME_FORMAT = "snapshot_%d.png";

  @NotNull
  public static List<String> calculateInitCommands(@NotNull final Project project,
                                                   @NotNull final TheRRunConfigurationParams runConfigurationParams) {
    if (isDeviceEnabled(runConfigurationParams)) {
      final String libPath = getLibPath(DEVICE_LIB_NAME);

      if (libPath != null) {
        final VirtualFile snapshotDir = getOrCreateSnapshotDir(project);

        if (snapshotDir != null) {
          return Arrays.asList(
            LOAD_LIB_COMMAND + "(\"" + libPath + "\")",
            DEVICE_FUNCTION_NAME + " <- function() { .Call(\"" + DEVICE_FUNCTION_NAME + "\", \"" + snapshotDir.getPath() + "\") }",
            SETUP_DEVICE_COMMAND
          );
        }
      }
    }
    else {
      LOGGER.warn(DEVICE_IS_DISABLED);
    }

    return Collections.emptyList();
  }

  @Nullable
  public static VirtualFile getOrCreateSnapshotDir(@NotNull final Project project) {
    final VirtualFile dotIdeaDir = project.getBaseDir().findChild(ProjectCoreUtil.DIRECTORY_BASED_PROJECT_DIR);

    if (dotIdeaDir != null) {
      return getOrCreateSnapshotDir(dotIdeaDir);
    }
    else {
      LOGGER.warn(DOT_IDEA_DIR_IS_NOT_FOUND);

      return null;
    }
  }

  public static boolean isSnapshotName(@NotNull final String name) {
    return SNAPSHOT_NAME_PATTERN.matcher(name).matches();
  }

  public static int calculateSnapshotId(@NotNull final String snapshotName) {
    final Matcher matcher = SNAPSHOT_NAME_PATTERN.matcher(snapshotName);

    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }
    else {
      throw new IllegalArgumentException(); // TODO [ui][msg]
    }
  }

  @NotNull
  public static String calculateSnapshotName(final int snapshotId) {
    return String.format(SNAPSHOT_NAME_FORMAT, snapshotId);
  }

  public static void removeSnapshots() {
    // TODO [ui][impl]
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

    if (!libFile.exists()) {
      LOGGER.warn(LIB_IS_NOT_FOUND + ": " + libFile.getPath());

      return null;
    }

    if (!libFile.canRead()) {
      LOGGER.warn(LIB_IS_NOT_READABLE + ": " + libFile.getPath());

      return null;
    }

    return libFile.getAbsolutePath();
  }

  @Nullable
  private static VirtualFile getOrCreateSnapshotDir(@NotNull final VirtualFile dotIdeaDir) {
    final VirtualFile snapshotDir = dotIdeaDir.findChild(SNAPSHOT_DIR_NAME);

    if (snapshotDir != null) {
      return checkSnapshotDir(snapshotDir);
    }
    else {
      LOGGER.info(SNAPSHOT_DIR_IS_NOT_FOUND);

      return createSnapshotDir(dotIdeaDir);
    }
  }

  @Nullable
  private static VirtualFile checkSnapshotDir(@NotNull final VirtualFile snapshotDir) {
    final String snapshotDirPath = snapshotDir.getPath();

    if (snapshotDir.isWritable()) {
      LOGGER.info(SNAPSHOT_DIR_IS_FOUND + ": " + snapshotDirPath);

      return snapshotDir;
    }
    else {
      LOGGER.warn(SNAPSHOT_DIR_IS_NOT_WRITABLE + ": " + snapshotDirPath);

      return null;
    }
  }

  @Nullable
  private static VirtualFile createSnapshotDir(@NotNull final VirtualFile dotIdeaDir) {
    try {
      final VirtualFile snapshotDir = dotIdeaDir.createChildDirectory(new TheRGraphicsUtils(), SNAPSHOT_DIR_NAME);

      LOGGER.info(SNAPSHOT_DIR_HAS_BEEN_CREATED + ": " + snapshotDir.getPath());

      return snapshotDir;
    }
    catch (final IOException e) {
      LOGGER.error(e);

      return null;
    }
  }
}
