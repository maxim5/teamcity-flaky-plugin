/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.web;

import java.util.*;
import jetbrains.buildServer.controllers.investigate.DummyTestRunImpl;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.flaky.data.*;
import jetbrains.buildServer.web.problems.GroupedTestsBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class TestsAnalysisBean {
  private final STestManager myTestManager;
  private final ProjectManager myProjectManager;

  private final SProject myProject;
  private final TestAnalysisResult myTestAnalysisResult;

  private final Map<Long, TestWebDetails> myDetails;

  private final TestAnalysisProgress myProgress;

  public TestsAnalysisBean(@NotNull SBuildServer buildServer,
                           @NotNull STestManager testManager,
                           @NotNull ProjectManager projectManager,
                           @NotNull BuildAgentManager agentManager,
                           @NotNull TestAnalysisProgress progress,
                           @NotNull TestAnalysisResultHolder holder,
                           @NotNull SProject project) {
    myTestManager = testManager;
    myProjectManager = projectManager;
    myProgress = progress;
    myProject = project;

    if (isInProgress()) {
      myTestAnalysisResult = TestAnalysisResult.EMPTY_FLAKY_TESTS;
      myDetails = Collections.emptyMap();
    } else {
      myTestAnalysisResult = holder.getTestAnalysisResult(project);
      myDetails = buildDetails(myTestAnalysisResult, buildServer, projectManager, agentManager);
    }
  }

  @NotNull
  public SProject getProject() {
    return myProject;
  }

  @NotNull
  public TestAnalysisResult getTestAnalysisResult() {
    return myTestAnalysisResult;
  }

  public boolean isTestAnalysisEverStarted() {
    return myTestAnalysisResult.getStartDate() != null;
  }

  public String getTestAnalysisDuration() {
    Date startDate = myTestAnalysisResult.getStartDate();
    Date finishDate = myTestAnalysisResult.getFinishDate();

    assert startDate != null;
    assert finishDate != null;
    long duration = (finishDate.getTime() - startDate.getTime()) / 1000;

    if (duration < 60) {
      int seconds = (int)duration;
      return seconds + " second" + (seconds != 1 ? "s" : "");
    } else if (duration < 60 * 60) {
      int minutes = (int)(duration / 60);
      return minutes + " minute" + (minutes != 1 ? "s" : "");
    } else {
      int hours = (int)(duration / 3600);
      return hours + " hour" + (hours != 1 ? "s" : "");
    }
  }

  @NotNull
  public Map<SBuildType, Boolean> getBuildTypeSettings() {
    TestAnalysisSettings settings = myTestAnalysisResult.getSettings();
    Set<String> excluded = new HashSet<String>(settings.getExcludeBuildTypes());
    TreeMap<SBuildType, Boolean> result = new TreeMap<SBuildType, Boolean>();
    for (SBuildType buildType : myProject.getBuildTypes()) {
      result.put(buildType, excluded.contains(buildType.getBuildTypeId()));
    }
    return result;
  }

  public boolean isHasData() {
    return isHasFlaky() || isHasSuspicious() || isHasAlwaysFailing();
  }

  public boolean isHasFlaky() {
    return !myTestAnalysisResult.getFlakyTests().isEmpty();
  }

  public boolean isHasSuspicious() {
    return !myTestAnalysisResult.getSuspiciousTests().isEmpty();
  }

  public boolean isHasAlwaysFailing() {
    return !myTestAnalysisResult.getAlwaysFailingTests().isEmpty();
  }

  public int getFlakyTestsSize() {
    return myTestAnalysisResult.getFlakyTests().size();
  }

  public int getSuspiciousTestsSize() {
    return myTestAnalysisResult.getSuspiciousTests().size();
  }

  public int getAlwaysFailingTestsSize() {
    return myTestAnalysisResult.getAlwaysFailingTests().size();
  }

  @NotNull
  public GroupedTestsBean getFlakyTests() {
    List<STestRun> testRuns = getTestRuns(myTestAnalysisResult.getFlakyTests());
    return GroupedTestsBean.createForTests(testRuns, null);
  }

  @NotNull
  public GroupedTestsBean getSuspiciousTests() {
    List<STestRun> testRuns = getTestRuns(myTestAnalysisResult.getSuspiciousTests());
    return GroupedTestsBean.createForTests(testRuns, null);
  }

  @NotNull
  public GroupedTestsBean getAlwaysFailingTests() {
    List<STestRun> testRuns = getTestRuns(myTestAnalysisResult.getAlwaysFailingTests());
    return GroupedTestsBean.createForTests(testRuns, null);
  }

  @NotNull
  public Map<Long, TestWebDetails> getDetails() {
    return myDetails;
  }

  @NotNull
  private List<STestRun> getTestRuns(@NotNull List<TestData> data) {
    ArrayList<STestRun> result = new ArrayList<STestRun>();
    for (TestData testData : data) {
      //noinspection deprecation
      TestEx test = (TestEx) myTestManager.createTest(testData.getTestId(), myProject.getProjectId());
      result.add(new DummyTestRunImpl(test) {
        @NotNull
        @Override
        public Status getStatus() {
          return Status.FAILURE;  // A hack to force the link on the web.
        }
      });
    }
    return result;
  }

  @NotNull
  private static Map<Long, TestWebDetails> buildDetails(@NotNull TestAnalysisResult testAnalysisResult,
                                                        @NotNull SBuildServer buildServer,
                                                        @NotNull ProjectManager projectManager,
                                                        @NotNull BuildAgentManager agentManager) {
    HashMap<Long, TestWebDetails> result = new HashMap<Long, TestWebDetails>();
    for (TestData data : testAnalysisResult.getFlakyTests()) {
      result.put(data.getTestId(), new TestWebDetails(buildServer, projectManager, agentManager, data));
    }
    for (TestData data : testAnalysisResult.getSuspiciousTests()) {
      result.put(data.getTestId(), new TestWebDetails(buildServer, projectManager, agentManager, data));
    }
    for (TestData data : testAnalysisResult.getAlwaysFailingTests()) {
      result.put(data.getTestId(), new TestWebDetails(buildServer, projectManager, agentManager, data));
    }
    return result;
  }

  // Progress

  public boolean isInProgress() {
    return myProgress.getLock().get();
  }

  @Nullable
  public TestAnalysisProgress getProgress() {
    return myProgress;
  }

  // Settings

  public List<SBuildType> getExcludedBuildTypes() {
    ArrayList<SBuildType> result = new ArrayList<SBuildType>();
    result.addAll(myProjectManager.findBuildTypes(myTestAnalysisResult.getSettings().getExcludeBuildTypes()));
    Collections.sort(result);
    return result;
  }
}
