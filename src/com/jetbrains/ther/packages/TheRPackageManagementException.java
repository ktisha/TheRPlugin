package com.jetbrains.ther.packages;

import static com.intellij.webcore.packaging.PackageManagementService.ErrorDescription;

/**
 * @author avesloguzova
 */
public class TheRPackageManagementException extends Exception {

  private ErrorDescription myErrorDescription;

  public TheRPackageManagementException(ErrorDescription stdErr) {
    this.myErrorDescription = stdErr;
  }

  public ErrorDescription getErrorDescription() {
    return myErrorDescription;
  }
}
