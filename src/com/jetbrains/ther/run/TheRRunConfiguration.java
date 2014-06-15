package com.jetbrains.ther.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configuration.AbstractRunConfiguration;
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
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class TheRRunConfiguration extends AbstractRunConfiguration implements TheRRunConfigurationParams {
  public static final String SCRIPT_NAME = "SCRIPT_NAME";
  public static final String PARAMETERS = "PARAMETERS";
  private String myScriptName;
  private String myScriptParameters;

  protected TheRRunConfiguration(Project project, ConfigurationFactory configurationFactory) {
    super(project, configurationFactory);
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

    if (StringUtil.isEmptyOrSpaces(myScriptName)) {
      throw new RuntimeConfigurationException("No script specified");
    }
  }

  @Override
  public String suggestedName() {
    final String scriptName = getScriptName();
    if (scriptName == null) return null;
    String name = new File(scriptName).getName();
    if (StringUtil.endsWithIgnoreCase(name, ".r")) {
      return name.substring(0, name.length() - 2);
    }
    return name;
  }

  @Override
  public String getScriptName() {
    return myScriptName;
  }

  @Override
  public void setScriptName(@NotNull final String scriptName) {
    myScriptName = scriptName;
  }

  @Override
  public String getScriptParameters() {
    return myScriptParameters;
  }

  @Override
  public void setScriptParameters(@NotNull final String scriptParameters) {
    myScriptParameters = scriptParameters;
  }

  @Override
  public Collection<Module> getValidModules() {
    return Arrays.asList(ModuleManager.getInstance(getProject()).getModules());
  }

  @Override
  public void readExternal(Element element) throws InvalidDataException {
    PathMacroManager.getInstance(getProject()).expandPaths(element);
    super.readExternal(element);
    myScriptName = JDOMExternalizerUtil.readField(element, SCRIPT_NAME);
    myScriptParameters = JDOMExternalizerUtil.readField(element, PARAMETERS);
  }

  @Override
  public void writeExternal(Element element) throws WriteExternalException {
    super.writeExternal(element);
    JDOMExternalizerUtil.writeField(element, SCRIPT_NAME, myScriptName);
    JDOMExternalizerUtil.writeField(element, PARAMETERS, myScriptParameters);
    PathMacroManager.getInstance(getProject()).collapsePathsRecursively(element);
  }

  public static void copyParams(TheRRunConfigurationParams source, TheRRunConfigurationParams target) {
    target.setScriptName(source.getScriptName());
    target.setScriptParameters(source.getScriptParameters());
  }

}
