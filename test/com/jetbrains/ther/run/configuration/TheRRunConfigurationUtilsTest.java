package com.jetbrains.ther.run.configuration;

import com.intellij.openapi.options.ConfigurationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class TheRRunConfigurationUtilsTest {

  @Test(expected = ConfigurationException.class)
  public void checkConfigurationWithoutScriptPath() throws ConfigurationException {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptPath()).thenReturn("");

    TheRRunConfigurationUtils.checkConfiguration(runConfiguration);
  }

  @Test(expected = ConfigurationException.class)
  public void checkConfigurationWithoutWorkingDirectoryPath() throws ConfigurationException {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getWorkingDirectoryPath()).thenReturn("");

    TheRRunConfigurationUtils.checkConfiguration(runConfiguration);
  }

  @Test
  public void checkConfiguration() throws ConfigurationException {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptPath()).thenReturn("s_p");
    when(runConfiguration.getWorkingDirectoryPath()).thenReturn("w_d_p");

    TheRRunConfigurationUtils.checkConfiguration(runConfiguration);

    verify(runConfiguration, times(1)).getScriptPath();
    verify(runConfiguration, times(1)).getWorkingDirectoryPath();
    verifyNoMoreInteractions(runConfiguration);
  }

  @Test
  public void suggestedNameForUnknownScriptPath() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptPath()).thenReturn("");

    assertNull(TheRRunConfigurationUtils.suggestedName(runConfiguration));

    verify(runConfiguration, times(1)).getScriptPath();
    verifyNoMoreInteractions(runConfiguration);
  }

  @Test
  public void suggestedNameForNotRScript() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptPath()).thenReturn("script.s");

    assertEquals("script.s", TheRRunConfigurationUtils.suggestedName(runConfiguration));

    verify(runConfiguration, times(1)).getScriptPath();
    verifyNoMoreInteractions(runConfiguration);
  }

  @Test
  public void suggestedNameForRScript() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptPath()).thenReturn("script.r");

    assertEquals("script", TheRRunConfigurationUtils.suggestedName(runConfiguration));

    verify(runConfiguration, times(1)).getScriptPath();
    verifyNoMoreInteractions(runConfiguration);
  }

  @Test
  public void suggestedWorkingDirectoryPathForUnknownScriptPath() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptPath()).thenReturn("");

    assertNull(TheRRunConfigurationUtils.suggestedWorkingDirectoryPath(runConfiguration));

    verify(runConfiguration, times(1)).getScriptPath();
    verifyNoMoreInteractions(runConfiguration);
  }

  @Test
  public void suggestedWorkingDirectoryPath() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptPath()).thenReturn("home/script.r");

    assertEquals("home", TheRRunConfigurationUtils.suggestedWorkingDirectoryPath(runConfiguration));

    verify(runConfiguration, times(1)).getScriptPath();
    verifyNoMoreInteractions(runConfiguration);
  }

  @Test
  public void setSuggestedWorkingDirectoryPathWhenNotSpecified() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptPath()).thenReturn("home/script.r");
    when(runConfiguration.getWorkingDirectoryPath()).thenReturn("");

    TheRRunConfigurationUtils.setSuggestedWorkingDirectoryPathIfNotSpecified(runConfiguration);

    verify(runConfiguration, times(1)).getScriptPath();
    verify(runConfiguration, times(1)).getWorkingDirectoryPath();
    verify(runConfiguration, times(1)).setWorkingDirectoryPath("home");
    verifyNoMoreInteractions(runConfiguration);
  }

  @Test
  public void setSuggestedWorkingDirectoryPathWhenSpecified() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getWorkingDirectoryPath()).thenReturn("home");

    TheRRunConfigurationUtils.setSuggestedWorkingDirectoryPathIfNotSpecified(runConfiguration);

    verify(runConfiguration, times(1)).getWorkingDirectoryPath();
    verifyNoMoreInteractions(runConfiguration);
  }

  @Test
  public void setSuggestedWorkingDirectoryPathWhenCouldNotBeSuggested() {
    final TheRRunConfiguration runConfiguration = mock(TheRRunConfiguration.class);

    when(runConfiguration.getScriptPath()).thenReturn("");
    when(runConfiguration.getWorkingDirectoryPath()).thenReturn("");

    TheRRunConfigurationUtils.setSuggestedWorkingDirectoryPathIfNotSpecified(runConfiguration);

    verify(runConfiguration, times(1)).getScriptPath();
    verify(runConfiguration, times(1)).getWorkingDirectoryPath();
    verifyNoMoreInteractions(runConfiguration);
  }
}