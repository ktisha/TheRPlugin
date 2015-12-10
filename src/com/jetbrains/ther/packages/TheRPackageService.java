package com.jetbrains.ther.packages;

import com.google.common.collect.Lists;
import com.intellij.openapi.components.*;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author avesloguzova
 */
@State(name = "TheRPackageService",
  storages = {
    @Storage(file = StoragePathMacros.APP_CONFIG + "/rpackages.xml")
  }
)
public class TheRPackageService implements PersistentStateComponent<TheRPackageService> {

  public Map<String, String> allPackages = ContainerUtil.newConcurrentMap();
  public int CRANMirror = 1;
  public List<Integer> defaultRepos = Lists.newArrayList();
  public List<String> userRepos = Lists.newArrayList();

  public static TheRPackageService getInstance() {
    return ServiceManager.getService(TheRPackageService.class);
  }

  @Nullable
  @Override
  public TheRPackageService getState() {
    return this;
  }

  @Override
  public void loadState(TheRPackageService state) {
    XmlSerializerUtil.copyBean(state, this);
  }

}
