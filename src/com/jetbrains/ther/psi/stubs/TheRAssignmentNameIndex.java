package com.jetbrains.ther.psi.stubs;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.jetbrains.ther.psi.api.TheRAssignmentStatement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class TheRAssignmentNameIndex extends StringStubIndexExtension<TheRAssignmentStatement> {
  public static final StubIndexKey<String, TheRAssignmentStatement> KEY = StubIndexKey.createIndexKey("TheR.function.shortName");

  @Override
  @NotNull
  public StubIndexKey<String, TheRAssignmentStatement> getKey() {
    return KEY;
  }

  public static Collection<TheRAssignmentStatement> find(String name, Project project, GlobalSearchScope scope) {
    return StubIndex.getElements(KEY, name, project, scope, TheRAssignmentStatement.class);
  }
}
