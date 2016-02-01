package com.jetbrains.ther.run.graphics;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectCoreUtil;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.jetbrains.ther.debugger.data.TheRCommands;
import com.jetbrains.ther.run.configuration.TheRRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.SERVICE_FUNCTION_PREFIX;
import static java.lang.Boolean.parseBoolean;

public final class TheRGraphicsUtils {

  @NotNull
  private static final Logger LOGGER = Logger.getInstance(TheRGraphicsUtils.class);

  @NotNull
  private static final Map<String, TheRGraphicsState> GRAPHICS_STATES = new HashMap<String, TheRGraphicsState>();

  @NotNull
  private static final String DEVICE_ENV_KEY = "ther.debugger.device";

  @NotNull
  private static final String DEVICE_IS_DISABLED = "Device is disabled [script: %s]";

  @NotNull
  private static final String DEVICE_LIB_NAME = String.format("libtherplugin_device%s.so", SystemInfo.is32Bit ? "32" : "64");

  @NotNull
  private static final String DEVICE_FUNCTION_NAME = SERVICE_FUNCTION_PREFIX + "device_init";

  @NotNull
  private static final String SETUP_DEVICE_COMMAND = "options(device=\"" + DEVICE_FUNCTION_NAME + "\")";

  @NotNull
  private static final String LIB_DIR_NAME = "libs";

  @NotNull
  private static final String LIB_IS_NOT_FOUND = "Lib is not found [path: %s]";

  @NotNull
  private static final String LIB_IS_NOT_READABLE = "Lib is not readable [path: %s]";

  @NotNull
  private static final String PROJECT_DIR_IS_NOT_FOUND = "Project dir is not found [path: %s]";

  @NotNull
  private static final String SNAPSHOT_DIR_NAME = "snapshots";

  @NotNull
  private static final String SNAPSHOT_DIR_IS_NOT_FOUND = "Snapshot dir is not found [path: %s]";

  @NotNull
  private static final String SNAPSHOT_DIR_IS_FOUND = "Snapshot dir is found [path: %s]";

  @NotNull
  private static final String SNAPSHOT_DIR_IS_NOT_WRITABLE = "Snapshot dir is not writable [path: %s]";

  @NotNull
  private static final String SNAPSHOT_DIR_HAS_BEEN_CREATED = "Snapshot dir has been created [path: %s]";

  @NotNull
  public static List<String> calculateInitCommands(@NotNull final TheRRunConfiguration runConfiguration) {
    if (isDeviceEnabled(runConfiguration)) {
      final String libPath = getLibPath(DEVICE_LIB_NAME);

      if (libPath != null) {
        final VirtualFile snapshotDir = getSnapshotDir(runConfiguration.getProject());

        if (snapshotDir != null) {
          return Arrays.asList(
            TheRCommands.loadLibCommand(libPath),
            DEVICE_FUNCTION_NAME + " <- function() { .Call(\"" + DEVICE_FUNCTION_NAME + "\", \"" + snapshotDir.getPath() + "\") }",
            SETUP_DEVICE_COMMAND
          );
        }
      }
    }
    else {
      LOGGER.warn(
        String.format(DEVICE_IS_DISABLED, runConfiguration.getScriptPath())
      );
    }

    return Collections.emptyList();
  }

  @NotNull
  public static TheRGraphicsState getGraphicsState(@NotNull final Project project) {
    final VirtualFile snapshotDir = getSnapshotDir(project);

    if (snapshotDir == null) {
      return new TheREmptyGraphicsState();
    }

    final String snapshotDirPath = snapshotDir.getPath();

    if (!GRAPHICS_STATES.containsKey(snapshotDirPath)) {
      final TheRGraphicsStateImpl state = new TheRGraphicsStateImpl(snapshotDir);

      Disposer.register(project, state);
      Disposer.register(
        state,
        new Disposable() {
          @Override
          public void dispose() {
            GRAPHICS_STATES.remove(snapshotDirPath);
          }
        }
      );

      GRAPHICS_STATES.put(snapshotDirPath, state);
    }

    return GRAPHICS_STATES.get(snapshotDirPath);
  }

  private static boolean isDeviceEnabled(@NotNull final TheRRunConfiguration runConfiguration) {
    final Map<String, String> envs = runConfiguration.getEnvs();

    return !envs.containsKey(DEVICE_ENV_KEY) || parseBoolean(envs.get(DEVICE_ENV_KEY));
  }

  @Nullable
  private static String getLibPath(@NotNull final String libName) {
    final File pluginDir = new File(PathUtil.getJarPathForClass(TheRGraphicsUtils.class));
    final File libDir = new File(pluginDir, LIB_DIR_NAME);
    final File libFile = new File(libDir, libName);
    final String absolutePath = libFile.getAbsolutePath();

    if (!libFile.exists()) {
      LOGGER.warn(
        String.format(LIB_IS_NOT_FOUND, absolutePath)
      );

      return null;
    }

    if (!libFile.canRead()) {
      LOGGER.warn(
        String.format(LIB_IS_NOT_READABLE, absolutePath)
      );

      return null;
    }

    return absolutePath;
  }

  @Nullable
  private static VirtualFile getSnapshotDir(@NotNull final Project project) {
    final String projectDirName = ProjectCoreUtil.DIRECTORY_BASED_PROJECT_DIR;
    final VirtualFile dotIdeaDir = project.getBaseDir().findChild(projectDirName);

    if (dotIdeaDir != null) {
      return getSnapshotDir(dotIdeaDir);
    }
    else {
      LOGGER.warn(
        String.format(
          PROJECT_DIR_IS_NOT_FOUND,
          new File(project.getBasePath(), projectDirName).getAbsolutePath()
        )
      );

      return null;
    }
  }

  @Nullable
  private static VirtualFile getSnapshotDir(@NotNull final VirtualFile dotIdeaDir) {
    final VirtualFile snapshotDir = dotIdeaDir.findChild(SNAPSHOT_DIR_NAME);

    if (snapshotDir != null) {
      return checkSnapshotDir(snapshotDir);
    }
    else {
      LOGGER.info(
        String.format(
          SNAPSHOT_DIR_IS_NOT_FOUND,
          new File(dotIdeaDir.getPath(), SNAPSHOT_DIR_NAME).getAbsolutePath()
        )
      );

      return createSnapshotDir(dotIdeaDir);
    }
  }

  @Nullable
  private static VirtualFile checkSnapshotDir(@NotNull final VirtualFile snapshotDir) {
    final String snapshotDirPath = snapshotDir.getPath();

    if (snapshotDir.isWritable()) {
      LOGGER.info(
        String.format(SNAPSHOT_DIR_IS_FOUND, snapshotDirPath)
      );

      return snapshotDir;
    }
    else {
      LOGGER.warn(
        String.format(SNAPSHOT_DIR_IS_NOT_WRITABLE, snapshotDirPath)
      );

      return null;
    }
  }

  @Nullable
  private static VirtualFile createSnapshotDir(@NotNull final VirtualFile dotIdeaDir) {
    try {
      final VirtualFile snapshotDir = dotIdeaDir.createChildDirectory(new TheRGraphicsUtils(), SNAPSHOT_DIR_NAME);

      LOGGER.info(
        String.format(SNAPSHOT_DIR_HAS_BEEN_CREATED, snapshotDir.getPath())
      );

      return snapshotDir;
    }
    catch (final IOException e) {
      LOGGER.error(e);

      return null;
    }
  }
}
