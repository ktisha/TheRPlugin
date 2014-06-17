package com.jetbrains.ther;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NonNls;

import java.io.File;

public class TheRHelpersLocator {
  private static final Logger LOG = Logger.getInstance(TheRHelpersLocator.class.getName());

  private TheRHelpersLocator() {
  }

  public static File getHelpersRoot() {
    @NonNls String jarPath = PathUtil.getJarPathForClass(TheRHelpersLocator.class);
    if (jarPath.endsWith(".jar")) {
      final File jarFile = new File(jarPath);

      LOG.assertTrue(jarFile.exists(), "jar file cannot be null");
      return jarFile.getParentFile().getParentFile();
    }

    return new File(jarPath);
  }

  public static String getHelperPath(String resourceName) {
    return getHelperFile(resourceName).getAbsolutePath();
  }

  public static File getHelperFile(String resourceName) {
    return new File(getHelpersRoot(), resourceName);
  }

}
