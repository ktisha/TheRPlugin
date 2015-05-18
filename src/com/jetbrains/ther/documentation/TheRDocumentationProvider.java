package com.jetbrains.ther.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.jetbrains.ther.TheRHelp;
import com.jetbrains.ther.TheRPsiUtils;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import org.jetbrains.annotations.Nullable;


public class TheRDocumentationProvider extends AbstractDocumentationProvider {

  @Nullable
  @Override
  public String generateDoc(PsiElement element, @Nullable PsiElement element1) {
    for (PsiElement el = element.getFirstChild(); el != null; el = el.getNextSibling()) {
      if (el instanceof TheRFunctionExpression) {
        final String docString = ((TheRFunctionExpression)el).getDocStringValue();
        if (docString != null) {
          return TheRDocumentationUtils.getFormattedString(docString);
        }
        break;
      }
    }
    final String helpText = TheRPsiUtils.getHelpForFunction(element);
    if (helpText == null) {
      return null;
    }
    else {
      return TheRDocumentationUtils.getFormattedString(new TheRHelp(helpText));
    }
  }
}
