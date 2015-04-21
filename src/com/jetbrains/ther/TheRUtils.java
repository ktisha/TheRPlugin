package com.jetbrains.ther;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.DocumentUtil;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.packages.TheRPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TheRUtils {
  private static final Logger LOG = Logger.getInstance(TheRUtils.class.getName());

  private TheRUtils() {
  }

  public static Set<String> getLibraryPathes(@NotNull final String intepreterPath) {
    final Set<String> libPaths = Sets.newHashSet();
    final ProcessOutput output = getProcessOutput("installed.packages()[,\"LibPath\"]", intepreterPath);
    if (output == null) return libPaths;

    String stdout = output.getStdout();
    final String[] splitOutput = StringUtil.splitByLines(stdout);
    for (String s : splitOutput) {
      if (StringUtil.isQuotedString(s)) {
        libPaths.add(StringUtil.unquoteString(s));
      }
    }
    return libPaths;
  }

  public static Set<String> getSearchPaths(@NotNull final String intepreterPath) {
    final Set<String> libPaths = Sets.newHashSet();
    final ProcessOutput output = getSuccessProcessOutput("search()", intepreterPath);
    if (output == null) return libPaths;

    final String stdout = output.getStdout();
    final String[] splitOutput = StringUtil.splitByLines(stdout);
    Collections.addAll(libPaths, splitOutput);
    return libPaths;
  }

  public static List<TheRPackage> getInstalledPackages() {
    final ArrayList<TheRPackage> installedPackages = Lists.newArrayList();
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    // .packages(all = TRUE)
    final ProcessOutput output = getSuccessProcessOutput("installed.packages()[,c(\"LibPath\", \"Version\")]", path);
    if (output == null) return installedPackages;

    String stdout = output.getStdout();
    final String[] splittedOutput = StringUtil.splitByLines(stdout);
    for (String line : splittedOutput) {
      final List<String> packageAttributes = StringUtil.split(line, "\t");
      if (packageAttributes.size() == 3) {
        final TheRPackage theRPackage = new TheRPackage(packageAttributes.get(0), packageAttributes.get(1), packageAttributes.get(2));
        installedPackages.add(theRPackage);
      }
    }
    return installedPackages;
  }

  @Nullable
  public static ProcessOutput getProcessOutput(@NotNull final String scriptText) {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    return getProcessOutput(scriptText, path);
  }

  @Nullable
  public static ProcessOutput getSuccessProcessOutput(@NotNull final String scriptText, @Nullable final String path) {
    final ProcessOutput output = getProcessOutput(scriptText, path);
    if (output == null) return null;

    if (output.getExitCode() != 0) {
      LOG.info("Failed because of: " + output.getStderr());
      return null;
    }
    if (output.isTimeout()) {
      LOG.info("Failed because of timeout.");
      return null;
    }
    return output;
  }

  @Nullable
  public static ProcessOutput getProcessOutput(@NotNull final String scriptText, @Nullable final String path) {
    if (path == null) {
      return null;
    }
    try {
      final Process process = Runtime.getRuntime().exec(path + " --slave -e " + scriptText);
      final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
      return processHandler.runProcess(5000);
    }
    catch (IOException e) {
      LOG.info("Failed to run R executable: \n" +
               "Interpreter path " + path + "\n" +
               "Exception occurred: " + e.getMessage());
    }
    return null;
  }

  public static void appendToDocument(@NotNull final Document document, final String text) {
    DocumentUtil.writeInRunUndoTransparentAction(new Runnable() {
      @Override
      public void run() {
        document.insertString(document.getTextLength(), text);
      }
    });
  }

  public static void saveDocument(@NotNull final Document document) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        FileDocumentManager.getInstance().saveDocument(document);
      }
    });
  }
}
