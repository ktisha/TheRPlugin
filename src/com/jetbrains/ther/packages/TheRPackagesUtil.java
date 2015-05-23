package com.jetbrains.ther.packages;

import com.google.common.collect.Lists;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.text.DateFormatUtil;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.RepoPackage;
import com.jetbrains.ther.TheRHelpersLocator;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * @author avesloguzova
 */
public final class TheRPackagesUtil {

  public static final String R_INSTALLED_PACKAGES = "r-packages-installed.r";
  public static final String R_ALL_PACKAGES = "r-packages-all.r";
  public static final String R_INSTALL_PACKAGE = "r-packages-install.r";
  public static final String ARGUMENT_DELIMETER = " ";

  private static final Logger LOG = Logger.getInstance(TheRPackagesUtil.class.getName());

  private static final Set<String> basePackages = new HashSet<String>(Arrays.asList(
    new String[]{"base", "utils", "stats", "datasets", "graphics",
      "grDevices", "grid", "methods", "tools", "parallel", "compiler", "splines", "tcltk", "stats4"}));

  public static boolean isPackageBase(InstalledPackage pkg) {
    return basePackages.contains(pkg.getName());
  }

  public static String getHelperSuccsessOutput(String helper) throws IOException {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    if (StringUtil.isEmptyOrSpaces(path)) {
      return null;
    }
    final String helperPath = TheRHelpersLocator.getHelperPath(helper);
    final Process process = Runtime.getRuntime().exec(path + " --slave -f " + helperPath);
    final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
    final ProcessOutput output = processHandler.runProcess((int)(5 * DateFormatUtil.MINUTE));
    if (output.getExitCode() != 0) {
      LOG.error("Failed to run script. Exit code: " + output.getExitCode());
      LOG.error(output.getStderrLines());
    }
    return output.getStdout();
  }

  public static List<InstalledPackage> getInstalledPackages() throws IOException {
    final ArrayList<InstalledPackage> installedPackages = Lists.newArrayList();
    final String stdout = getHelperSuccsessOutput(R_INSTALLED_PACKAGES);
    if (stdout == null) {
      return installedPackages;
    }
    final String[] splittedOutput = StringUtil.splitByLines(stdout);
    for (String line : splittedOutput) {
      final List<String> packageAttributes = StringUtil.split(line, ARGUMENT_DELIMETER);
      if (packageAttributes.size() == 4) {
        final InstalledPackage theRPackage =
          new InstalledPackage(packageAttributes.get(1).replace("\"", ""), packageAttributes.get(2).replace("\"", ""));
        installedPackages.add(theRPackage);
      }
    }
    return installedPackages;
  }

  public static Map<String, String> getPackages() {
    return TheRPackageService.getInstance().allPackages;
  }

  public static List<RepoPackage> getOrLoadPackages() throws IOException {
    Map<String, String> nameVersionMap = getPackages();
    if (nameVersionMap.isEmpty()) {
      getAvailablePackages();
      nameVersionMap = getPackages();
    }
    return versionMapToPackageList(nameVersionMap);
  }

  private static List<RepoPackage> versionMapToPackageList(Map<String, String> packageToVersionMap) {

    List<RepoPackage> packages = new ArrayList<RepoPackage>();
    for (Map.Entry<String, String> entry : packageToVersionMap.entrySet()) {
      packages.add(new RepoPackage(entry.getKey(), "", entry.getValue()));
    }
    return packages;
  }

  public static void getAvailablePackages() throws IOException {
    final String stdout = getHelperSuccsessOutput(R_ALL_PACKAGES);
    if (StringUtil.isEmptyOrSpaces(stdout)) {
      return;
    }
    Map<String, String> packages = getPackages();
    packages.clear();
    final String[] splittedOutput = StringUtil.splitByLines(stdout);
    for (String line : splittedOutput) {
      final List<String> packageAttributes = StringUtil.split(line, ARGUMENT_DELIMETER);
      if (packageAttributes.size() == 3) {
        packages.put(packageAttributes.get(1).replace("\"", ""), packageAttributes.get(2).replace("\"", ""));
      }
    }
  }

  public static void installPackage(@NotNull RepoPackage repoPackage)
    throws ExecutionException {
    TheRRunResult result = null;
    try {
      result = runHelperWithArgs(R_INSTALL_PACKAGE, repoPackage.getName());
    }
    catch (IOException e) {
      throw new ExecutionException("Some I/O errors occurs while installing");
    }
    if (result == null) {
      throw new ExecutionException("Path to interpreter didn't set");//TODO Fix
    }
    final String stderr = result.getStdErr();
    if (!stderr.contains(String.format("DONE (%s)", repoPackage.getName()))) {

      throw new TheRExecutionException("Some error during the installation", result.getCommand(), result.getStdOut(), result.getStdErr(),
                                       result.getExitCode());
    }
  }

  public static void uninstallPackage(List<InstalledPackage> repoPackage) throws ExecutionException {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    if (StringUtil.isEmptyOrSpaces(path)) {
      throw new ExecutionException("Path to interpreter didn't set");
    }
    StringBuilder commandBuilder = getCommand(path, repoPackage);
    String command = commandBuilder.toString();
    Process process = null;
    try {
      process = Runtime.getRuntime().exec(command);
    }
    catch (IOException e) {
      throw new ExecutionException("Some I/O errors occurs while installing");
    }
    final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
    final ProcessOutput output = processHandler.runProcess((int)(5 * DateFormatUtil.MINUTE));
    if (output.getExitCode() != 0) {
      throw new TheRExecutionException("Can't remove package", command, output.getStdout(), output.getStderr(), output.getExitCode());
    }
  }

  private static StringBuilder getCommand(String path, List<InstalledPackage> repoPackage) {
    StringBuilder commandBuilder = new StringBuilder();
    commandBuilder.append(path).append(" CMD REMOVE");
    for (InstalledPackage aRepoPackage : repoPackage) {
      commandBuilder.append(" ").append(aRepoPackage.getName());
    }
    return commandBuilder;
  }

  @Nullable
  public static TheRRunResult runHelperWithArgs(@NotNull String helper, String... args) throws IOException {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    if (StringUtil.isEmptyOrSpaces(path)) {
      return null;
    }
    String command = getCommand(helper, path, args);
    Process process;
    process = Runtime.getRuntime().exec(command);
    final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
    final ProcessOutput output = processHandler.runProcess((int)(5 * DateFormatUtil.MINUTE));
    if (output.getExitCode() != 0) {
      LOG.error("Failed to run script. Exit code: " + output.getExitCode());
      LOG.error(output.getStderrLines());
    }
    return new TheRRunResult(command, output);
  }

  @NotNull
  private static String getCommand(@NotNull String helper, String path, String[] args) {
    final String helperPath = TheRHelpersLocator.getHelperPath(helper);
    StringBuilder execStr = new StringBuilder();
    execStr.append(path).append(" --slave -f ").append(helperPath).append(" --args");
    for (String arg : args) {
      execStr.append(ARGUMENT_DELIMETER).append(arg);
    }
    return execStr.toString();
  }

  public static void updatePackage(InstalledPackage installedPackage) {

  }

  public static class TheRRunResult {
    private final String myCommand;
    private final String myStdOut;
    private final String myStdErr;
    private int myExitCode;

    public TheRRunResult(@NotNull String command, @NotNull ProcessOutput output) {
      this.myCommand = command;
      this.myExitCode = output.getExitCode();
      this.myStdOut = output.getStdout();
      this.myStdErr = output.getStderr();
    }


    public String getCommand() {
      return myCommand;
    }

    public String getStdOut() {
      return myStdOut;
    }

    public String getStdErr() {
      return myStdErr;
    }

    public int getExitCode() {
      return myExitCode;
    }
  }
}
