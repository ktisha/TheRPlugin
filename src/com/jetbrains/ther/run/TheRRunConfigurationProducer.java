package com.jetbrains.ther.run;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.ther.TheRFileType;
import org.jetbrains.annotations.Nullable;

public class TheRRunConfigurationProducer extends RunConfigurationProducer<TheRRunConfiguration> {

  public TheRRunConfigurationProducer() {
    super(TheRConfigurationType.getInstance().getConfigurationFactories()[0]);
  }

  @Override
  protected boolean setupConfigurationFromContext(TheRRunConfiguration configuration,
                                                  ConfigurationContext context,
                                                  Ref<PsiElement> sourceElement) {

    final Location location = context.getLocation();
    if (location == null) return false;
    final PsiFile script = location.getPsiElement().getContainingFile();
    if (!isAvailable(script)) return false;

    final VirtualFile vFile = script.getVirtualFile();
    if (vFile == null) return false;
    configuration.setScriptPath(vFile.getPath());
    configuration.setName(configuration.suggestedName());
    return true;
  }

  @Override
  public boolean isConfigurationFromContext(TheRRunConfiguration configuration, ConfigurationContext context) {
    final Location location = context.getLocation();
    if (location == null) return false;
    final PsiFile script = location.getPsiElement().getContainingFile();
    if (!isAvailable(script)) return false;
    final VirtualFile virtualFile = script.getVirtualFile();
    if (virtualFile == null) return false;
    if (virtualFile instanceof LightVirtualFile) return false;
    final String scriptPath = configuration.getScriptPath();
    final String path = virtualFile.getPath();
    return scriptPath.equals(path);
  }

  private static boolean isAvailable(@Nullable final PsiFile script) {
    return script != null && script.getFileType() == TheRFileType.INSTANCE;
  }
}
