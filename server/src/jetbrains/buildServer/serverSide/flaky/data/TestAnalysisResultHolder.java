/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.data;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.util.cache.EhCacheUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jetbrains.annotations.NotNull;

/**
 * Holds the test analysis results for all projects.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class TestAnalysisResultHolder {
  // A persistent cache: string (projectId) -> TestAnalysisResult.
  private final Cache myCache;

  public TestAnalysisResultHolder(@NotNull EhCacheUtil cacheUtil) {
    myCache = cacheUtil.createCache("tests-analysis");
    myCache.getCacheConfiguration().setEternal(true);
    myCache.getCacheConfiguration().setMaxElementsInMemory(3);
  }

  @NotNull
  public TestAnalysisResult getTestAnalysisResult(@NotNull SProject project) {
    return getFromCache(project);
  }

  public void putTestAnalysisResult(@NotNull SProject project, @NotNull TestAnalysisResult result) {
    putToCache(project, result);
  }

  private void putToCache(@NotNull SProject project, @NotNull TestAnalysisResult result) {
    Element element = new Element(project.getProjectId(), result);
    myCache.put(element);
  }

  @NotNull
  private TestAnalysisResult getFromCache(@NotNull SProject project) {
    // A familiar problem: see AbstractIssueFetcher.
    // EhCache uses 'Thread.currentThread().getContextClassLoader()' to load classes of serialized
    // instances, which is not the same as 'getClass().getClassLoader()'. This causes a problem
    // for plugin classes derived from IssueData.

    ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

    try {
      Element element = myCache.getQuiet(project.getProjectId());
      if (element != null) {
        //noinspection unchecked
        return (TestAnalysisResult) element.getValue();
      }
      return TestAnalysisResult.EMPTY_FLAKY_TESTS;
    } finally {
      Thread.currentThread().setContextClassLoader(currentLoader);
    }
  }
}
