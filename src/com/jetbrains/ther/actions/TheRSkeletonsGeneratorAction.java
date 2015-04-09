package com.jetbrains.ther.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jetbrains.ther.interpreter.TheRSkeletonGenerator;


public class TheRSkeletonsGeneratorAction extends AnAction {

  public void actionPerformed(AnActionEvent e) {
    TheRSkeletonGenerator.runSkeletonGeneration();
  }
}
