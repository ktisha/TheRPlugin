package com.jetbrains.ther.psi.references;

import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.impl.scopes.LibraryScope;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.ther.TheRLanguage;
import com.jetbrains.ther.interpreter.TheRInterpreterConfigurable;
import com.jetbrains.ther.interpreter.TheRInterpreterService;
import com.jetbrains.ther.psi.api.*;
import com.jetbrains.ther.psi.stubs.TheRAssignmentNameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TheRReferenceImpl implements PsiReference, PsiPolyVariantReference {
  private static final Logger LOG = Logger.getInstance(TheRReferenceImpl.class.getName());
  protected final TheRElement myElement;

  public TheRReferenceImpl(TheRElement element) {
    myElement = element;
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final List<ResolveResult> result = new ArrayList<ResolveResult>();
    final String name = myElement.getText();
    if (name != null) {

      TheRBlock rBlock = PsiTreeUtil.getParentOfType(myElement, TheRBlock.class);
      while (rBlock != null) {
        final TheRAssignmentStatement[] statements = PsiTreeUtil.getChildrenOfType(rBlock, TheRAssignmentStatement.class);
        if (statements != null) {
          for (TheRAssignmentStatement statement : statements) {
            final PsiElement assignee = statement.getAssignee();
            if (assignee != null && assignee.getText().equals(name)) {
              result.add(new PsiElementResolveResult(assignee));
            }
          }
        }
        rBlock = PsiTreeUtil.getParentOfType(rBlock, TheRBlock.class);
      }
      final TheRFunction rFunction = PsiTreeUtil.getParentOfType(myElement, TheRFunction.class);
      if (rFunction != null) {
        final TheRParameterList list = rFunction.getParameterList();
        for (TheRParameter parameter : list.getParameters()) {
          if (name.equals(parameter.getName())) {
            result.add(new PsiElementResolveResult(parameter));
          }
        }
      }
      final PsiFile file = myElement.getContainingFile();
      final TheRAssignmentStatement[] statements = PsiTreeUtil.getChildrenOfType(file, TheRAssignmentStatement.class);
      if (statements != null) {
        for (TheRAssignmentStatement statement : statements) {
          final PsiElement assignee = statement.getAssignee();
          if (assignee != null && assignee.getText().equals(name)) {
            result.add(new PsiElementResolveResult(assignee));
          }
        }
      }
      if (!result.isEmpty())
        return result.toArray(new ResolveResult[result.size()]);
      addFromLibrary(result, name);
      if (result.isEmpty()) {
        addRuntimeDefinition(result, name);
      }
    }
    return result.toArray(new ResolveResult[result.size()]);
  }

  private void addRuntimeDefinition(@NotNull final List<ResolveResult> result, @NotNull final String name) {
    final String path = TheRInterpreterService.getInstance().getInterpreterPath();
    if (path == null) return;
    try {
      final Process process = Runtime.getRuntime().exec(path + " --slave -e " + name);
      final CapturingProcessHandler processHandler = new CapturingProcessHandler(process);
      final ProcessOutput output = processHandler.runProcess(5000);
      if (output.getExitCode() != 0) {
        LOG.error("Failed to obtain function definition from runtime: " + output.getStderr());
      }
      if (output.isTimeout())
        LOG.error("Failed to obtain function definition from runtime because of timeout.");

      String stdout = output.getStdout();
      final int byteCodeIndex = stdout.indexOf("<bytecode");
      if (byteCodeIndex > 0) stdout = stdout.substring(0, byteCodeIndex);

      final PsiFileFactory factory = PsiFileFactory.getInstance(myElement.getProject());
      final String fileName = name + ".r";
      final LightVirtualFile virtualFile = new LightVirtualFile(fileName, TheRLanguage.getInstance(), stdout);
      final PsiFile psiFile = ((PsiFileFactoryImpl)factory).trySetupPsiForFile(virtualFile, TheRLanguage.getInstance(), true, true);
      if (psiFile != null)
        result.add(new PsiElementResolveResult(psiFile));
    }
    catch (IOException e) {
      LOG.error("Failed to obtain function definition from runtime because: \n" +
                "Interpreter path " + path + "\n" +
                "Exception occured: " + e.getMessage());
    }

  }

  private void addFromLibrary(@NotNull final List<ResolveResult> result, @NotNull final String name) {
    final ModifiableModelsProvider modelsProvider = ModifiableModelsProvider.SERVICE.getInstance();
    final LibraryTable.ModifiableModel model = modelsProvider.getLibraryTableModifiableModel(myElement.getProject());
    if (model != null) {
      final Library library = model.getLibraryByName(TheRInterpreterConfigurable.THE_R_LIBRARY);
      if (library != null) {
        final Collection<TheRAssignmentStatement> assignmentStatements = TheRAssignmentNameIndex.find(name, myElement.getProject(),
                                                                                        new LibraryScope(myElement.getProject(), library));
        for (TheRAssignmentStatement statement : assignmentStatements) {
          final PsiFile containingFile = statement.getContainingFile();
          final PsiElement assignee = statement.getAssignee();
          if(assignee == null) continue;
          if (FileUtil.getNameWithoutExtension(containingFile.getName()).equalsIgnoreCase(name)) {
            result.add(0, new PsiElementResolveResult(assignee));
          }
          else
            result.add(new PsiElementResolveResult(assignee));
        }
      }
    }
  }

  @Override
  public PsiElement getElement() {
    return myElement;
  }

  @Override
  public TextRange getRangeInElement() {
    final TextRange range = myElement.getNode().getTextRange();
    return range.shiftRight(-myElement.getNode().getStartOffset());
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final ResolveResult[] results = multiResolve(false);
    return results.length >= 1 ? results[0].getElement() : null;

  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return getElement().getText();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return null;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return false;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return new Object[0];
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}
