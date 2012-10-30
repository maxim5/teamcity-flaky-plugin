/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jetbrains.buildServer.serverSide.SProject;
import org.jetbrains.annotations.NotNull;

/**
 * Controls the progress for each project tab.
 * TODO: test.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class TestAnalysisProgressManager {
  private final ConcurrentMap<String, TestAnalysisProgress> myProgressMap;

  public TestAnalysisProgressManager() {
    myProgressMap = new ConcurrentHashMap<String, TestAnalysisProgress>();
  }

  @NotNull
  public TestAnalysisProgress getProgressFor(@NotNull String projectId) {
    TestAnalysisProgress progress = myProgressMap.get(projectId);
    if (progress == null) {
      progress = new TestAnalysisProgress();
      myProgressMap.put(projectId, progress);
    }
    return progress;
  }

  public void clearProgressFor(@NotNull String projectId) {
    myProgressMap.remove(projectId);
  }

  @NotNull
  public TestAnalysisProgress getProgressFor(@NotNull SProject project) {
    return getProgressFor(project.getProjectId());
  }

  public void clearProgressFor(@NotNull SProject project) {
    clearProgressFor(project.getProjectId());
  }
}
