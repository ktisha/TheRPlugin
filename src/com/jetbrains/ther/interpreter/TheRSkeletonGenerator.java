package com.jetbrains.ther.interpreter;

import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.TheRHelpersLocator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class TheRSkeletonGenerator {
  protected static final Logger LOG = Logger.getInstance("#" + TheRSkeletonGenerator.class.getName());

  protected static final String R_GENERATOR = "r-generator.r";

  public static final String SKELETON_DIR_NAME = "r_skeletons";

  protected static final int MINUTE = 60 * 1000;

  public static String getSkeletonsPath(@NotNull final String interpreterHome) {
    final String basePath = PathManager.getSystemPath();
    return getSkeletonsRootPath(basePath) + File.separator + FileUtil.toSystemIndependentName(interpreterHome).hashCode() + File.separator;
  }

  public static String getSkeletonsRootPath(@NotNull final String basePath) {
    return basePath + File.separator + SKELETON_DIR_NAME;
  }

  public static void runSkeletonGeneration() {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    if (StringUtil.isEmptyOrSpaces(path)) return;
    final String helperPath = TheRHelpersLocator.getHelperPath(R_GENERATOR);
    try {
      final String skeletonsPath = getSkeletonsPath(path);
      final File skeletonsDir = new File(skeletonsPath);
      if (!skeletonsDir.exists() && !skeletonsDir.mkdirs()) {
        LOG.error("Can't create skeleton dir " + String.valueOf(skeletonsPath));
      }
      final Process process = Runtime.getRuntime().exec(path + " --slave -f " + helperPath + " --args " + skeletonsPath);
      final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
      final ProcessOutput output = processHandler.runProcess(MINUTE * 5);
      if (output.getExitCode() != 0) {
        LOG.error("Failed to generate skeletons. Exit code: " + output.getExitCode());
        LOG.error(output.getStderrLines());
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }

}
