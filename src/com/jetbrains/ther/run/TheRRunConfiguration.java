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
import java.util.Map;

public class TheRRunConfiguration extends AbstractRunConfiguration implements TheRRunConfigurationParams {

  @NotNull
  private static final String SCRIPT_PATH = "SCRIPT_PATH";

  @NotNull
  private static final String SCRIPT_PARAMETERS = "SCRIPT_PARAMETERS";

  @NotNull
  private static final String WORKING_DIRECTORY = "WORKING_DIRECTORY";

  @NotNull
  private static final String PARENT_ENVS = "PARENT_ENVS";

  @NotNull
  private String myScriptPath;

  @NotNull
  private String myScriptParameters;

  @NotNull
  private String myWorkingDirectory;

  protected TheRRunConfiguration(@NotNull final Project project, @NotNull final ConfigurationFactory configurationFactory) {
    super(project, configurationFactory);

    myScriptPath = "";
    myScriptParameters = "";
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
    final String extension = TheRFileType.INSTANCE.getDefaultExtension();
    final int dotIndex = name.length() - extension.length() - 1;

    if (StringUtil.endsWithIgnoreCase(name, extension) && name.charAt(dotIndex) == '.') {
      return name.substring(0, dotIndex);
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
  public String getScriptParameters() {
    return myScriptParameters;
  }

  @Override
  public void setScriptParameters(@NotNull final String scriptParameters) {
    myScriptParameters = scriptParameters;
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
  public Map<String, String> getEnvs() {
    return super.getEnvs();
  }

  @Override
  public void setEnvs(@NotNull final Map<String, String> envs) {
    super.setEnvs(envs);
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
    myScriptParameters = JDOMExternalizerUtil.readField(element, SCRIPT_PARAMETERS, "");
    myWorkingDirectory = JDOMExternalizerUtil.readField(element, WORKING_DIRECTORY, "");

    readEnvs(element);
  }

  @Override
  public void writeExternal(@NotNull final Element element) throws WriteExternalException {
    super.writeExternal(element);

    JDOMExternalizerUtil.writeField(element, SCRIPT_PATH, myScriptPath);
    JDOMExternalizerUtil.writeField(element, SCRIPT_PARAMETERS, myScriptParameters);
    JDOMExternalizerUtil.writeField(element, WORKING_DIRECTORY, myWorkingDirectory);

    writeEnvs(element);

    PathMacroManager.getInstance(getProject()).collapsePathsRecursively(element);
  }

  public static void copyParams(@NotNull final TheRRunConfigurationParams source, @NotNull final TheRRunConfigurationParams target) {
    target.setScriptPath(source.getScriptPath());
    target.setScriptParameters(source.getScriptParameters());
    target.setWorkingDirectory(source.getWorkingDirectory());
    target.setPassParentEnvs(source.isPassParentEnvs());
    target.setEnvs(new HashMap<String, String>(source.getEnvs()));
  }

  private void readEnvs(@NotNull final Element element) {
    setPassParentEnvs(
      Boolean.parseBoolean(
        JDOMExternalizerUtil.readField(element, PARENT_ENVS, "")
      )
    );

    EnvironmentVariablesComponent.readExternal(element, getEnvs());
  }

  private void writeEnvs(@NotNull final Element element) {
    JDOMExternalizerUtil.writeField(element, PARENT_ENVS, Boolean.toString(isPassParentEnvs()));

    EnvironmentVariablesComponent.writeExternal(element, getEnvs());
  }
}
