package com.jetbrains.ther.packages;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.CatchingConsumer;
import com.intellij.webcore.packaging.InstalledPackage;
import com.intellij.webcore.packaging.RepoPackage;
import com.jetbrains.ther.TheRHelpersLocator;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.TheRUtils;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author avesloguzova
 */
public final class TheRPackagesUtil {

  public static final String R_INSTALLED_PACKAGES = "r-packages/r-packages-installed.r";
  public static final String R_ALL_PACKAGES = "r-packages/r-packages-all.r";
  public static final String R_INSTALL_PACKAGE = "r-packages/r-packages-install.r";
  public static final String R_UPDATE_PACKAGE = "r-packages/r-packages-update.r";
  public static final String ARGUMENT_DELIMETER = " ";
  public static final String R_PACKAGES_DEFAULT_REPOS = "r-packages/r-packages-default-repos.r";
  public static final String R_PACKAGES_DETAILS = "r-packages/r-packages-details.r";
  @NonNls public static final String CRAN_URL = "https://cran.r-project.org/web/packages/available_packages_by_name.html";

  private static final Pattern urlPattern = Pattern.compile("\".+\"");
  private static final Logger LOG = Logger.getInstance(TheRPackagesUtil.class.getName());

  private static final Set<String> basePackages = Sets.newHashSet("base", "utils", "stats", "datasets", "graphics",
      "grDevices", "grid", "methods", "tools", "parallel", "compiler", "splines", "tcltk", "stats4");
  private static TreeMap<String, String> namesToDetails;

  public static boolean isPackageBase(@NotNull final InstalledPackage pkg) {
    return basePackages.contains(pkg.getName());
  }

  public static String getHelperOutput(String helper) {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    if (StringUtil.isEmptyOrSpaces(path)) {
      LOG.info("Path to interpreter didn't set");
      return null;
    }
    final String helperPath = TheRHelpersLocator.getHelperPath(helper);
    try {
      final Process process = new GeneralCommandLine(path, "--slave", "-f", helperPath).createProcess();
      final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
      final ProcessOutput output = processHandler.runProcess(5 * TheRPsiUtils.MINUTE);
      if (output.getExitCode() != 0) {
        LOG.error("Failed to run script. Exit code: " + output.getExitCode());
        LOG.error(output.getStderrLines());
      }
      return output.getStdout();
    }
    catch (ExecutionException e) {
      LOG.error(e.getMessage());
    }
    return null;
  }

  public static List<InstalledPackage> getInstalledPackages() {
    final ArrayList<InstalledPackage> installedPackages = Lists.newArrayList();
    final String stdout = getHelperOutput(R_INSTALLED_PACKAGES);
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
    Collections.sort(installedPackages, new Comparator<InstalledPackage>() {
      @Override
      public int compare(InstalledPackage o1, InstalledPackage o2) {
        return StringUtil.compare(o1.getName(), o2.getName(), true);
      }
    });
    return installedPackages;
  }

  public static Map<String, String> getPackages() {
    return TheRPackageService.getInstance().allPackages;
  }

  public static List<RepoPackage> getOrLoadPackages() {
    Map<String, String> nameVersionMap = getPackages();
    if (nameVersionMap.isEmpty()) {
      loadAvailablePackages();
      nameVersionMap = getPackages();
    }
    return versionMapToPackageList(nameVersionMap);
  }

  private static List<RepoPackage> versionMapToPackageList(@NotNull final Map<String, String> packageToVersionMap) {
    final List<RepoPackage> packages = new ArrayList<RepoPackage>();
    for (Map.Entry<String, String> entry : packageToVersionMap.entrySet()) {
      final String[] splitted = entry.getValue().split(ARGUMENT_DELIMETER);
      packages.add(new RepoPackage(entry.getKey(), splitted[1], splitted[0]));
    }
    return packages;
  }

  public static void setRepositories(@NotNull final List<String> defaultRepositories,
                                     @NotNull final List<String> userRepositories) {
    TheRPackageService service = TheRPackageService.getInstance();
    service.enabledRepositories.clear();
    service.enabledRepositories.addAll(defaultRepositories);
    service.userRepositories.clear();
    service.userRepositories.addAll(userRepositories);
  }

  @NotNull
  public static List<TheRDefaultRepository> getDefaultRepositories() {
    final String output = getHelperOutput(R_PACKAGES_DEFAULT_REPOS);
    if (output != null) {
      return toDefaultPackages((output));
    }
    return Lists.newArrayList();
  }

  private static List<TheRDefaultRepository> toDefaultPackages(@NotNull final String output) {
    final List<String> urls = getURLs(output);
    final List<TheRDefaultRepository> repos = Lists.newArrayList();
    for (int i = 0; i < urls.size(); i++) {
      repos.add(new TheRDefaultRepository(urls.get(i), i + 1));
    }
    return repos;
  }

  @NotNull
  public static List<String> getCRANMirrors() {
    final ProcessOutput output = TheRUtils.getProcessOutput("getCRANmirrors()[,\"URL\"]");
    if (output != null && output.getExitCode() == 0) {
      return getURLs(output.getStdout());
    }
    return Lists.newArrayList();
  }

  @NotNull
  private static List<String> getURLs(@NotNull String stdout) {
    final List<String> reposURL = Lists.newArrayList();
    final Matcher matcher = urlPattern.matcher(stdout);
    while (matcher.find()) {
      reposURL.add(matcher.group().replace('\"', ' ').trim());
    }
    return reposURL;
  }

  @Nullable
  public static List<RepoPackage> loadAvailablePackages() {
    final List<String> args = getHelperRepositoryArguments();
    final TheRRunResult result = runHelperWithArgs(R_ALL_PACKAGES, args.toArray(new String[args.size()]));
    if (result == null || result.getExitCode() != 0) {
      return null;
    }
    TheRPackageService.getInstance().allPackages.clear();
    final List<RepoPackage> packageList = Lists.newArrayList();
    final String[] splittedOutput = StringUtil.splitByLines(result.getStdOut());
    for (String line : splittedOutput) {
      final List<String> packageAttributes = StringUtil.split(line, ARGUMENT_DELIMETER);
      if (packageAttributes.size() >= 3) {
        RepoPackage repoPackage = new RepoPackage(packageAttributes.get(1).replace("\"", ""), packageAttributes.get(3).replace("\"", ""),
                                                  packageAttributes.get(2).replace("\"", ""));
        TheRPackageService.getInstance().allPackages.put(repoPackage.getName(), repoPackage.getLatestVersion()
                                                                                + ARGUMENT_DELIMETER + repoPackage.getRepoUrl());
        packageList.add(repoPackage);
      }
    }
    try {
      getPackageDetails();
    }
    catch (IOException e) {
      LOG.warn("Couldn't get package details");
    }
    return packageList;
  }

  public static void installPackage(@NotNull RepoPackage repoPackage)
    throws ExecutionException {
    List<String> args = getHelperRepositoryArguments();
    args.add(0, repoPackage.getName());
    final TheRRunResult result = runHelperWithArgs(R_INSTALL_PACKAGE, args.toArray(new String[args.size()]));
    if (result == null) {
      throw new ExecutionException("Please, specify path to the R executable.");
    }
    final String stderr = result.getStdErr();
    if (!stderr.contains(String.format("DONE (%s)", repoPackage.getName()))) {
      throw new TheRExecutionException("Some error during the installation", result.getCommand(), result.getStdOut(), result.getStdErr(),
                                       result.getExitCode());
    }
  }

  public static void updatePackage(@NotNull RepoPackage repoPackage)
    throws ExecutionException {
    List<String> args = getHelperRepositoryArguments();
    args.add(0, repoPackage.getName());
    final TheRRunResult result = runHelperWithArgs(R_UPDATE_PACKAGE, args.toArray(new String[args.size()]));
    if (result == null) {
      throw new ExecutionException("Please, specify path to the R executable.");
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
      throw new ExecutionException("Please, specify path to the R executable.");
    }
    final ArrayList<String> arguments = Lists.newArrayList(path, "CMD", "REMOVE");
    for (InstalledPackage aRepoPackage : repoPackage) {
      arguments.add(aRepoPackage.getName());
    }
    final Process process = new GeneralCommandLine(arguments).createProcess();

    final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
    final ProcessOutput output = processHandler.runProcess(5 * TheRPsiUtils.MINUTE);
    if (output.getExitCode() != 0) {
      throw new TheRExecutionException("Can't remove package", StringUtil.join(arguments, " "), output.getStdout(),
                                       output.getStderr(), output.getExitCode());
    }
  }

  public static void fetchPackageDetails(@NotNull final String packageName, @NotNull final CatchingConsumer<String, Exception> consumer) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          final String details = loadPackageDetails(packageName);
          consumer.consume(formatDetails(packageName, details));
        }
        catch (ExecutionException e) {
          consumer.consume(e);
        }
      }
    });
  }

  private static String formatDetails(@NotNull final String packageName, @NotNull final String details) {
    final String[] splittedString = details.split("\t");
    StringBuilder builder = new StringBuilder("<html><head>    <style type=\"text/css\">        " +
                                              "p {            font-family: Arial,serif; font-size: 12pt; margin: 2px 2px        }    " +
                                              "</style></head><body style=\"font-family: Arial,serif; font-size: 12pt; margin: 5px 5px;\">");
    if (namesToDetails.containsKey(packageName)) {
      builder.append(namesToDetails.get(packageName));
      builder.append("<br/>");
    }
    if (splittedString.length == 3) {
      builder.append("<h4>Version</h4>");
      builder.append(splittedString[0]);
      builder.append("<br/>");
      builder.append("<h4>Depends</h4>");
      builder.append(splittedString[1]);
      builder.append("<br/>");
      builder.append("<h4>Repository</h4>");
      builder.append(splittedString[2]);
    }
    return builder.toString();
  }

  private static String loadPackageDetails(@NotNull final String packageName) throws ExecutionException {
    final List<String> args = getHelperRepositoryArguments();
    args.add(0, packageName);

    final TheRRunResult result = runHelperWithArgs(R_PACKAGES_DETAILS, args.toArray(new String[args.size()]));
    if (result != null && result.getExitCode() == 0) {
      return result.getStdOut();
    }
    else {
      if (result == null) {
        throw new ExecutionException("Can't fetch package details.");
      }
      throw new TheRExecutionException("Can't fetch package details.", result.getCommand(), result.getStdOut(), result.getStdErr(),
                                       result.getExitCode());
    }
  }

  @Nullable
  private static TheRRunResult runHelperWithArgs(@NotNull final String helper, @NotNull final String... args) {
    final String interpreterPath = TheRInterpreterService.getInstance().getInterpreterPath();
    if (StringUtil.isEmptyOrSpaces(interpreterPath)) {
      LOG.info("Path to interpreter didn't set");
      return null;
    }
    final ArrayList<String> command = Lists.newArrayList(interpreterPath, " --slave", "-f ", TheRHelpersLocator.getHelperPath(helper),
                                                         " --args");
    Collections.addAll(command, args);
    try {
      final Process process = new GeneralCommandLine(command).createProcess();
      final CapturingProcessHandler processHandler = new CapturingProcessHandler(process, null, StringUtil.join(command, " "));
      final ProcessOutput output = processHandler.runProcess(5 * TheRPsiUtils.MINUTE);
      if (output.getExitCode() != 0) {
        LOG.error("Failed to run script. Exit code: " + output.getExitCode());
        LOG.error(output.getStderrLines());
      }
      return new TheRRunResult(StringUtil.join(command, " "), output);
    }
    catch (ExecutionException e) {
      LOG.error(e.getMessage());
    }
    return null;
  }

  @NotNull
  private static List<String> getHelperRepositoryArguments() {
    final TheRPackageService service = TheRPackageService.getInstance();
    final List<String> args = Lists.newArrayList();
    args.add(String.valueOf(service.CRANMirror + 1));
    if (service.enabledRepositories.size() > 0) {
      args.add(String.valueOf(service.enabledRepositories.size()));
      for (String repository : service.enabledRepositories) {
        for (TheRDefaultRepository defaultRepository : getDefaultRepositories()) {
          if (defaultRepository.getUrl().equals(repository))
            args.add(String.valueOf(defaultRepository.getIndex()));
        }
      }
    }
    else {
      args.add(String.valueOf(1));
      args.add(String.valueOf(1));
    }
    args.addAll(service.userRepositories);
    return args;
  }

  public static Map<String, String> getPackageDetails() throws IOException {
    if (namesToDetails != null) return namesToDetails;
    namesToDetails = new TreeMap<String, String>();
    final HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
      public boolean inTable;
      HTML.Tag myTag;
      String myPackageName;

      @Override
      public void handleStartTag(HTML.Tag tag,
                                 MutableAttributeSet set,
                                 int i) {
        myTag = tag;
        if ("table".equals(myTag.toString())) {
          inTable = true;
        }
      }

      @Override
      public void handleText(char[] data, int pos) {
        if (myTag != null && "a".equals(myTag.toString()) && inTable && myPackageName == null) {
          myPackageName = String.valueOf(data);
        }
        else if (myTag != null && "td".equals(myTag.toString())) {
          namesToDetails.put(myPackageName, String.valueOf(data));
          myPackageName = null;
        }
      }
    };

    try {
      final URL repositoryUrl = new URL(CRAN_URL);
      final InputStream is = repositoryUrl.openStream();
      final Reader reader = new InputStreamReader(is);
      try {
        new ParserDelegator().parse(reader, callback, true);
      }
      catch (IOException e) {
        LOG.warn(e);
      }
      finally {
        reader.close();
      }
    }
    catch (MalformedURLException e) {
      LOG.warn(e);
    }

    return namesToDetails;
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

    @NotNull
    public String getCommand() {
      return myCommand;
    }

    @NotNull
    public String getStdOut() {
      return myStdOut;
    }

    @NotNull
    public String getStdErr() {
      return myStdErr;
    }

    public int getExitCode() {
      return myExitCode;
    }
  }
}
