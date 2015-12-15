package com.jetbrains.ther.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.AbstractRunConfiguration;
import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.TheRFileType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

public class TheRRunConfiguration extends AbstractRunConfiguration implements TheRRunConfigurationParams {

  @NotNull
  private static final String SCRIPT_PATH = "SCRIPT_PATH";

  @NotNull
  private static final String SCRIPT_ARGS = "SCRIPT_ARGS";

  @NotNull
  private static final String WORKING_DIRECTORY = "WORKING_DIRECTORY";

  @NotNull
  private static final String PASS_PARENT_ENVS = "PASS_PARENT_ENVS";

  @NotNull
  private String myScriptPath;

  @NotNull
  private String myScriptArgs;

  @NotNull
  private String myWorkingDirectory;

  protected TheRRunConfiguration(@NotNull final Project project, @NotNull final ConfigurationFactory configurationFactory) {
    super(project, configurationFactory);

    myScriptPath = "";
    myScriptArgs = "";
    myWorkingDirectory = "";
  }

  @Override
  public RunProfileState getState(@NotNull final Executor executor, @NotNull final ExecutionEnvironment env) throws ExecutionException {
    return new TheRCommandLineState(this, env);
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new TheRRunConfigurationEditor(this);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    super.checkConfiguration();

    if (StringUtil.isEmptyOrSpaces(myScriptPath)) {
      throw new RuntimeConfigurationException("No script specified");
    }
  }

  @Nullable
  @Override
  public String suggestedName() {
    if (StringUtil.isEmptyOrSpaces(myScriptPath)) {
      return null;
    }

    final String name = new File(myScriptPath).getName();
    final String dotAndExtension = "." + TheRFileType.INSTANCE.getDefaultExtension();

    if (name.length() > dotAndExtension.length() && StringUtil.endsWithIgnoreCase(name, dotAndExtension)) {
      return name.substring(0, name.length() - dotAndExtension.length());
    }

    return name;
  }

  @NotNull
  @Override
  public String getScriptPath() {
    return myScriptPath;
  }

  @Override
  public void setScriptPath(@NotNull final String scriptPath) {
    myScriptPath = scriptPath;
  }

  @NotNull
  @Override
  public String getScriptArgs() {
    return myScriptArgs;
  }

  @Override
  public void setScriptArgs(@NotNull final String scriptArgs) {
    myScriptArgs = scriptArgs;
  }

  @NotNull
  @Override
  public String getWorkingDirectory() {
    return myWorkingDirectory;
  }

  @Override
  public void setWorkingDirectory(@NotNull final String workingDirectory) {
    myWorkingDirectory = workingDirectory;
  }

  @NotNull
  @Override
  public Collection<Module> getValidModules() {
    return Arrays.asList(ModuleManager.getInstance(getProject()).getModules());
  }

  @Override
  public void readExternal(@NotNull final Element element) throws InvalidDataException {
    PathMacroManager.getInstance(getProject()).expandPaths(element);

    super.readExternal(element);

    myScriptPath = JDOMExternalizerUtil.readField(element, SCRIPT_PATH, "");
    myScriptArgs = JDOMExternalizerUtil.readField(element, SCRIPT_ARGS, "");
    myWorkingDirectory = JDOMExternalizerUtil.readField(element, WORKING_DIRECTORY, "");

    readEnvs(element);
  }

  @Override
  public void writeExternal(@NotNull final Element element) throws WriteExternalException {
    super.writeExternal(element);

    JDOMExternalizerUtil.writeField(element, SCRIPT_PATH, myScriptPath);
    JDOMExternalizerUtil.writeField(element, SCRIPT_ARGS, myScriptArgs);
    JDOMExternalizerUtil.writeField(element, WORKING_DIRECTORY, myWorkingDirectory);

    writeEnvs(element);

    PathMacroManager.getInstance(getProject()).collapsePathsRecursively(element);
  }

  public static void copyParams(@NotNull final TheRRunConfigurationParams source, @NotNull final TheRRunConfigurationParams target) {
    target.setScriptPath(source.getScriptPath());
    target.setScriptArgs(source.getScriptArgs());
    target.setWorkingDirectory(source.getWorkingDirectory());
    target.setPassParentEnvs(source.isPassParentEnvs());
    target.setEnvs(new HashMap<String, String>(source.getEnvs()));
  }

  private void readEnvs(@NotNull final Element element) {
    setPassParentEnvs(
      Boolean.parseBoolean(
        JDOMExternalizerUtil.readField(element, PASS_PARENT_ENVS, "")
      )
    );

    EnvironmentVariablesComponent.readExternal(element, getEnvs());
  }

  private void writeEnvs(@NotNull final Element element) {
    JDOMExternalizerUtil.writeField(element, PASS_PARENT_ENVS, Boolean.toString(isPassParentEnvs()));

    EnvironmentVariablesComponent.writeExternal(element, getEnvs());
  }
}
