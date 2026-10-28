package com.qa.core;

import org.junit.jupiter.api.extension.*;

/** Capture failures in setup as well as in the test, before browser teardown. */
public class FailureScreenshotExtension
    implements TestExecutionExceptionHandler, LifecycleMethodExecutionExceptionHandler {
  @Override
  public void handleTestExecutionException(ExtensionContext context, Throwable failure)
      throws Throwable {
    capture(context, failure);
    throw failure;
  }

  @Override
  public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable failure)
      throws Throwable {
    capture(context, failure);
    throw failure;
  }

  private void capture(ExtensionContext context, Throwable failure) {
    try {
      if (context.getRequiredTestInstance() instanceof BaseTest base)
        base.attachFailureScreenshot();
    } catch (RuntimeException screenshotFailure) {
      failure.addSuppressed(screenshotFailure);
    }
  }
}
