package com.jetbrains.ther.run.configuration;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.ther.TheRFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRRunConfigurationProducer extends RunConfigurationProducer<TheRRunConfiguration> {

  public TheRRunConfigurationProducer() {
    super(TheRRunConfigurationType.getInstance().getMainFactory());
  }

  @Override
  protected boolean setupConfigurationFromContext(@NotNull final TheRRunConfiguration configuration,
                                                  @NotNull final ConfigurationContext context,
                                                  @Nullable final Ref<PsiElement> sourceElement) {
    final VirtualFile scriptVirtualFile = getScriptVirtualFile(context);

    if (scriptVirtualFile == null) {
      return false;
    }

    configuration.setName(TheRRunConfigurationSuggests.suggestedName(configuration));
    configuration.setScriptPath(scriptVirtualFile.getPath());
    TheRRunConfigurationSuggests.setSuggestedWorkingDirectoryPathIfNotSpecified(configuration);

    return true;
  }

  @Override
  public boolean isConfigurationFromContext(@NotNull final TheRRunConfiguration configuration,
                                            @NotNull final ConfigurationContext context) {
    final VirtualFile scriptVirtualFile = getScriptVirtualFile(context);

    if (scriptVirtualFile == null) {
      return false;
    }

    final String configurationScriptPath = configuration.getScriptPath();
    final String configurationWorkingDirectoryPath = configuration.getWorkingDirectoryPath();

    final String contextScriptPath = scriptVirtualFile.getPath();
    final String contextWorkingDirectoryPath = TheRRunConfigurationSuggests.suggestedWorkingDirectoryPath(configuration);

    return configurationScriptPath.equals(contextScriptPath) && configurationWorkingDirectoryPath.equals(contextWorkingDirectoryPath);
  }

  @Nullable
  private static VirtualFile getScriptVirtualFile(@NotNull final ConfigurationContext context) {
    final Location location = context.getLocation();
    if (location == null) return null;

    final PsiFile psiFile = getRunnablePsiFile(location);
    if (psiFile == null) return null;

    return getPhysicalVirtualFile(psiFile);
  }

  @Nullable
  private static PsiFile getRunnablePsiFile(@NotNull final Location location) {
    final PsiFile result = location.getPsiElement().getContainingFile();

    if (result == null || result.getFileType() != TheRFileType.INSTANCE) return null;

    return result;
  }

  @Nullable
  private static VirtualFile getPhysicalVirtualFile(@NotNull final PsiFile psiFile) {
    final VirtualFile result = psiFile.getVirtualFile();

    if (result == null || result instanceof LightVirtualFile) return null;

    return result;
  }
}
