package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class TheRDebuggedFunctions {

  @NotNull
  private final TheRDebuggedFunctionsNode myRoot;

  public TheRDebuggedFunctions() {
    myRoot = new TheRDebuggedFunctionsNode(TheRDebugConstants.MAIN_FUNCTION.getName(), null);
  }

  public boolean isDebugged(@NotNull final TheRFunction function) {
    final TheRDebuggedFunctionsNode node = loadNode(myRoot, function.getDefinition().iterator());

    return node != null && node.isDebugged();
  }

  public void add(@NotNull final TheRFunction function) {
    final TheRDebuggedFunctionsNode node = loadOrCreateNode(myRoot, function.getDefinition().iterator());

    node.setDebugged(true);
  }

  public void remove(@NotNull final TheRFunction function) {
    remove(loadNode(myRoot, function.getDefinition().iterator()));
  }

  public boolean hasDebuggedInner(@NotNull final TheRFunction function) {
    final TheRDebuggedFunctionsNode node = loadNode(myRoot, function.getDefinition().iterator());

    return node != null && !node.getChildren().isEmpty();
  }

  @Nullable
  private static TheRDebuggedFunctionsNode loadNode(@NotNull final TheRDebuggedFunctionsNode root,
                                                    @NotNull final Iterator<String> iterator) {
    if (!iterator.hasNext()) {
      return root;
    }

    final TheRDebuggedFunctionsNode child = root.getChildren().get(iterator.next());

    if (child == null) {
      return null;
    }

    return loadNode(child, iterator);
  }

  @NotNull
  private static TheRDebuggedFunctionsNode loadOrCreateNode(@NotNull final TheRDebuggedFunctionsNode root,
                                                            @NotNull final Iterator<String> iterator) {
    if (!iterator.hasNext()) {
      return root;
    }

    final String name = iterator.next();
    final TheRDebuggedFunctionsNode child = root.getChildren().get(name);

    if (child == null) {
      final TheRDebuggedFunctionsNode newChild = new TheRDebuggedFunctionsNode(name, root);

      root.getChildren().put(name, newChild);

      return loadOrCreateNode(newChild, iterator);
    }
    else {
      return loadOrCreateNode(child, iterator);
    }
  }

  private void remove(@Nullable final TheRDebuggedFunctionsNode node) {
    if (node == null) {
      return;
    }

    node.setDebugged(false);

    if (node.getChildren().isEmpty()) {
      final TheRDebuggedFunctionsNode parent = node.getParent();

      if (parent != null) {
        parent.getChildren().remove(node.getName());

        removeObsoleteParents(parent);
      }
    }
  }

  private void removeObsoleteParents(@NotNull final TheRDebuggedFunctionsNode current) {
    if (!current.isDebugged() && current.getChildren().isEmpty()) {
      final TheRDebuggedFunctionsNode parent = current.getParent();

      if (parent != null) {
        parent.getChildren().remove(current.getName());

        removeObsoleteParents(parent);
      }
    }
  }
}
