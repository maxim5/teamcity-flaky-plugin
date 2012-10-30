/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.data;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Holds the progress of test analysis process in one project.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class TestAnalysisProgress {
  private final AtomicBoolean myLock = new AtomicBoolean(false);

  private String myCurrentStep;
  private int myTotalSize;
  private int myDoneSize;

  @NotNull
  public AtomicBoolean getLock() {
    return myLock;
  }

  public void start(@NotNull String initialStep) {
    myCurrentStep = initialStep;
    myTotalSize = 0;
    myDoneSize = 0;
  }

  public String getCurrentStep() {
    return myCurrentStep;
  }

  public void setCurrentStep(@NotNull String currentStep) {
    myCurrentStep = currentStep;
  }

  public int getTotalSize() {
    return myTotalSize;
  }

  public void setTotalSize(int totalSize) {
    myTotalSize = totalSize;
  }

  public int getDoneSize() {
    return myDoneSize;
  }

  public void incDoneSize() {
    ++myDoneSize;
  }

  public void setDoneSize(int doneSize) {
    myDoneSize = doneSize;
  }
}
