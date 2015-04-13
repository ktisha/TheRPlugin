package com.jetbrains.ther.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TheRDocumentationProvider extends AbstractDocumentationProvider {

  @Nullable
  @Override
  public String generateDoc(PsiElement element, @Nullable PsiElement element1) {
    for (PsiElement el = element.getNextSibling(); el != null; el = el.getNextSibling()) {
      if (el instanceof TheRFunctionExpression) {
        return getFormattedString(((TheRFunctionExpression)el).getDocStringValue());
      }
    }
    return null;
  }

  private String getFormattedString(String docString) {
    StringBuilder builder = new StringBuilder();
    String[] strings = StringUtil.splitByLines(docString);
    Pattern pattern = Pattern.compile("^.+:");
    boolean argsStarted = false;
    for (String string : strings) {
      if (string.trim().startsWith("Args:")) {
        builder.append("<br>");
        builder.append("<b>").append(string).append("</b>");
      }
      else {
        Matcher matcher = pattern.matcher(string.trim());
        if(matcher.find()){
          builder.append("<br>");
          builder.append(matcher.replaceFirst("<b>$0</b>"));
        }else {
          builder.append(string);
        }
        builder.append(" ");
      }
    }
    return builder.toString();
  }
}
