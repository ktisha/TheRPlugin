package com.jetbrains.ther.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.ther.psi.api.TheRPsiElement;
import com.jetbrains.ther.psi.api.TheRVisitor;
import com.jetbrains.ther.typing.TheRTypeProvider;
import com.jetbrains.ther.typing.types.TheRType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class TheRGenerateTypingReport extends AnAction {

  private static final Logger LOG = Logger.getInstance(TheRGenerateTypingReport.class);
  public static final String HEADER = "text, node type, start, end, type\n";
  public static final String TYPE_REPORTS_DIRNAME = "type-reports";

  public TheRGenerateTypingReport() {
    super("Generate Typing Report");
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent event) {
    final Project project = event.getProject();
    assert project != null;
    generateReportForOpenedFile(project);
  }

  @Override
  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    if (!ApplicationManager.getApplication().isInternal()) {
      presentation.setEnabled(false);
      presentation.setVisible(false);
      return;
    }
    presentation.setVisible(true);
    presentation.setEnabled(true);
  }

  public static void generateReportForOpenedFile(final Project project) {
    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (editor == null) {
      Messages.showErrorDialog(project, "No files opened", "Failed Generate Typing Report");
      return;
    }
    final Document userDocument = editor.getDocument();
    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(userDocument);
    assert psiFile != null;
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        try {
          PsiDirectory psiDirectory = psiFile.getContainingFile().getParent();
          assert psiDirectory != null;
          VirtualFile parentDirectoryForFile = psiDirectory.getVirtualFile();
          VirtualFile typeReports = parentDirectoryForFile.findChild(TYPE_REPORTS_DIRNAME);
          if (typeReports == null) {
            typeReports = parentDirectoryForFile.createChildDirectory(this, TYPE_REPORTS_DIRNAME);
          }
          VirtualFile reportFile = typeReports.findOrCreateChildData(project, generateReportName(psiFile.getName()));
          final Document reportDocument = FileDocumentManager.getInstance().getDocument(reportFile);
          assert reportDocument != null;
          CommandProcessor.getInstance().runUndoTransparentAction(new Runnable() {
            @Override
            public void run() {
              reportDocument.deleteString(0, reportDocument.getTextLength());
              reportDocument.insertString(0, HEADER);
            }
          });
          final ElementReportGeneratorVisitor visitor = new ElementReportGeneratorVisitor(reportDocument, userDocument);
          psiFile.accept(visitor);
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              FileDocumentManager.getInstance().saveDocument(reportDocument);
            }
          });
          FileEditorManager.getInstance(project).openFile(reportFile, true);
        }
        catch (IOException e) {
          LOG.error(e);
        }
      }
    });
  }

  private static String generateReportName(String name) {
    return String.format("typing-report-%s.csv", FileUtil.getNameWithoutExtension(name));
  }

  static class ElementReportGeneratorVisitor extends TheRVisitor {
    private final Document myReportDocument;
    private final Document myUserDocument;

    public ElementReportGeneratorVisitor(Document document, Document userDocument) {
      myReportDocument = document;
      myUserDocument = userDocument;
    }

    @Override
    public void visitPsiElement(@NotNull TheRPsiElement element) {
      insertReportString(element);
      element.acceptChildren(this);
    }

    private void insertReportString(TheRPsiElement element) {
      TheRType type = TheRTypeProvider.getType(element);
      String typeName = type == null ? "null" : type.getName();
      int offset = element.getTextOffset();
      String start = getLineOffsetPresentation(offset);
      String end = getLineOffsetPresentation(offset + element.getTextLength());
      final String lineInformation = String.format("%s, %s, %s, %s, %s\n", escape(element.getText()),
                                                   element.getNode().getElementType().toString(),
                                                   start,
                                                   end,
                                                   typeName);
      CommandProcessor.getInstance().runUndoTransparentAction(new Runnable() {
        @Override
        public void run() {
          myReportDocument.insertString(myReportDocument.getTextLength(), lineInformation);
        }
      });
    }

    private String escape(String text) {
      text = text.replaceAll("\"", "\"\"");
      return String.format("\"%s\"", text);
    }

    private String getLineOffsetPresentation(int offset) {
      int lineNumber = myUserDocument.getLineNumber(offset);
      int offsetInLine = offset - myUserDocument.getLineStartOffset(lineNumber);
      return String.format("%d:%d", lineNumber + 1, offsetInLine + 1);
    }

    @Override
    public void visitFile(PsiFile file) {
      PsiElement[] children = file.getChildren();
      for (PsiElement child : children) {
        if (child instanceof TheRPsiElement) {
          visitPsiElement((TheRPsiElement)child);
        }
      }
    }
  }
}
