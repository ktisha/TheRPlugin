package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class TheRDebuggedFunctions {

  @NotNull
  private final TheRDebuggedFunctionsNode myRoot;

  public TheRDebuggedFunctions() {
    myRoot = new TheRDebuggedFunctionsNode(TheRDebugConstants.MAIN_FUNCTION_NAME, null);
  }

  public boolean isDebugged(@NotNull final String function) {
    final TheRDebuggedFunctionsNode node = loadNode(function);

    return node != null && node.isDebugged();
  }

  public void add(@NotNull final String function) {
    final TheRDebuggedFunctionsNode node = loadOrCreateNode(function);

    node.setDebugged(true);
  }

  public void remove(@NotNull final String function) {
    remove(loadNode(function));
  }

  public boolean hasDebuggedInner(@NotNull final String function) {
    final TheRDebuggedFunctionsNode node = loadNode(function);

    return node != null && !node.getChildren().isEmpty();
  }

  public boolean isDebugged(@NotNull final List<String> innerFunction) {
    final TheRDebuggedFunctionsNode node = loadNode(myRoot, innerFunction.iterator());

    return node != null && node.isDebugged();
  }

  public void add(@NotNull final List<String> innerFunction) {
    final TheRDebuggedFunctionsNode node = loadOrCreateNode(myRoot, innerFunction.iterator());

    node.setDebugged(true);
  }

  public void remove(@NotNull final List<String> innerFunction) {
    remove(loadNode(myRoot, innerFunction.iterator()));
  }

  public boolean hasDebuggedInner(@NotNull final List<String> innerFunction) {
    final TheRDebuggedFunctionsNode node = loadNode(myRoot, innerFunction.iterator());

    return node != null && !node.getChildren().isEmpty();
  }

  @Nullable
  private TheRDebuggedFunctionsNode loadNode(@NotNull final String function) {
    return myRoot.getChildren().get(function);
  }

  @NotNull
  private TheRDebuggedFunctionsNode loadOrCreateNode(@NotNull final String function) {
    final TheRDebuggedFunctionsNode node = myRoot.getChildren().get(function);

    if (node == null) {
      final TheRDebuggedFunctionsNode newNode = new TheRDebuggedFunctionsNode(function, myRoot);

      myRoot.getChildren().put(function, newNode);

      return newNode;
    }
    else {
      return node;
    }
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
