package com.jetbrains.ther.packages;

import com.google.common.collect.Lists;
import com.intellij.openapi.components.*;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Created by sasha on 4/27/15.
 */
@State(name = "TheRPackageService",
  storages = {
    @Storage(file = StoragePathMacros.APP_CONFIG + "/rpackages.xml")
  }
)
public class TheRPackageService implements PersistentStateComponent<TheRPackageService> {

  public List<String> allRepositories = Lists.newArrayList();
  public Map<String, String> allPackages = ContainerUtil.newConcurrentMap();

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

  public void addRepository(String url) {
    allRepositories.add(url);
  }

  public void removeRepository(String url) {
    if (allRepositories.contains(url)) {
      allRepositories.remove(url);
    }
  }
}
