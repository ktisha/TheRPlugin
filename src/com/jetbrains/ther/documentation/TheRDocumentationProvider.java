package com.jetbrains.ther.documentation;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.ther.psi.api.TheRFunctionExpression;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TheRDocumentationProvider extends AbstractDocumentationProvider {

  private static final Pattern myPattern = Pattern.compile("^.+:");

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

  @Nullable
  private String getFormattedString(@Nullable String docString) {
    if (docString == null) {
      return null;
    }
    final StringBuilder builder = new StringBuilder();
    final String[] strings = StringUtil.splitByLines(docString);
    for (String string : strings) {
      final String trimmedString = string.trim();
      if (trimmedString.startsWith("Args:")) {
        builder.append("<br>");
        builder.append("<b>").append(string).append("</b>");
      }
      else {
        final Matcher matcher = myPattern.matcher(trimmedString);
        if (matcher.find()) {
          builder.append("<br>");
          builder.append(matcher.replaceFirst("<b>$0</b>"));
        }
        else {
          builder.append(string);
        }
        builder.append(" ");
      }
    }
    return builder.toString();
  }
}
