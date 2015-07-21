package com.jetbrains.ther.xdebugger.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO [xdbg][test]
final class TheRXFunctionDescriptorUtils {

  @Nullable
  public static TheRXFunctionDescriptor resolve(@NotNull final TheRXFunctionDescriptor currentDescriptor,
                                                final int currentLine,
                                                @NotNull final String nextFunctionName) {
    if (currentDescriptor.getChildren().containsKey(nextFunctionName)) {
      TheRXFunctionDescriptor result = null;
      int distance = Integer.MAX_VALUE;

      for (final TheRXFunctionDescriptor candidate : currentDescriptor.getChildren().get(nextFunctionName)) {

        if (currentDescriptor.getStartLine() + currentLine > candidate.getStartLine()) { // candidate is declared before the current line
          final int currentDistance = currentDescriptor.getStartLine() + currentLine - candidate.getStartLine();

          if (currentDistance < distance) {
            result = candidate;
            distance = currentDistance;
          }
        }
      }

      if (result != null) {
        return result;
      }
    }

    if (currentDescriptor.getParent() == null) {
      return null;
    }

    return resolve(currentDescriptor.getParent(), currentLine + currentDescriptor.getStartLine(), nextFunctionName);
  }

  public static void add(@NotNull final TheRXFunctionDescriptor root,
                         @NotNull final String name,
                         final int startLine,
                         final int endLine) {
    addFrom(root, name, startLine, endLine);
  }

  private static void addFrom(@NotNull final TheRXFunctionDescriptor currentDescriptor,
                              @NotNull final String name,
                              final int startLine,
                              final int endLine) {
    if (!trySiftDown(currentDescriptor, name, startLine, endLine)) {
      addAsChild(currentDescriptor, name, startLine, endLine);
    }
  }

  private static boolean trySiftDown(@NotNull final TheRXFunctionDescriptor currentDescriptor,
                                     @NotNull final String name,
                                     final int startLine,
                                     final int endLine) {
    for (final List<TheRXFunctionDescriptor> sameNameChildren : currentDescriptor.getChildren().values()) {
      for (final TheRXFunctionDescriptor child : sameNameChildren) {
        if (child.getStartLine() <= startLine && endLine <= child.getEndLine()) {
          addFrom(
            child,
            name,
            startLine,
            endLine
          );

          return true;
        }
      }
    }

    return false;
  }

  private static void addAsChild(@NotNull final TheRXFunctionDescriptor currentDescriptor,
                                 @NotNull final String name,
                                 final int startLine,
                                 final int endLine) {
    final Map<String, List<TheRXFunctionDescriptor>> children = currentDescriptor.getChildren();

    if (!children.containsKey(name)) {
      children.put(
        name,
        new ArrayList<TheRXFunctionDescriptor>()
      );
    }

    children.get(name).add(
      new TheRXFunctionDescriptor(
        name,
        currentDescriptor,
        startLine,
        endLine
      )
    );
  }
}
