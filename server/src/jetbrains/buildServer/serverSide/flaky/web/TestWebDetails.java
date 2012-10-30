/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.web;

import com.intellij.openapi.util.Pair;
import java.util.*;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.flaky.analyser.BuildWithoutChangesReason;
import jetbrains.buildServer.serverSide.flaky.analyser.BuildsOnSameModificationReason;
import jetbrains.buildServer.serverSide.flaky.analyser.SuspiciousFailureStatisticsReason;
import jetbrains.buildServer.serverSide.flaky.data.FailureRate;
import jetbrains.buildServer.serverSide.flaky.data.Reason;
import jetbrains.buildServer.serverSide.flaky.data.TestData;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class TestWebDetails {
  private final SBuildServer myBuildServer;
  private final TestData myTestData;

  private final List<SBuildType> myAllBuildTypes;
  private final List<SBuildType> myFailedInBuildTypes;

  private final List<SBuildAgent> myAllAgents;
  private final List<SBuildAgent> myFailedOnAgents;

  public TestWebDetails(@NotNull SBuildServer buildServer,
                        @NotNull ProjectManager projectManager,
                        @NotNull BuildAgentManager agentManager,
                        @NotNull TestData testData) {
    myBuildServer = buildServer;
    myTestData = testData;

    myAllBuildTypes = new ArrayList<SBuildType>();
    myFailedInBuildTypes = new ArrayList<SBuildType>();
    final Map<String, FailureRate> buildTypeFailureRates = myTestData.getBuildTypeFailureRates();
    for (Map.Entry<String, FailureRate> entry : buildTypeFailureRates.entrySet()) {
      SBuildType buildType = projectManager.findBuildTypeById(entry.getKey());
      if (buildType != null) {
        myAllBuildTypes.add(buildType);
        if (entry.getValue().hasFailures()) {
          myFailedInBuildTypes.add(buildType);
        }
      }
    }
    Collections.sort(myAllBuildTypes, new Comparator<SBuildType>() {
      public int compare(SBuildType bt1, SBuildType bt2) {
        FailureRate failureRate1 = buildTypeFailureRates.get(bt1.getBuildTypeId());
        FailureRate failureRate2 = buildTypeFailureRates.get(bt2.getBuildTypeId());
        int result = failureRate1.compareTo(failureRate2);
        return result != 0 ? result : bt1.compareTo(bt2);
      }
    });

    myAllAgents = new ArrayList<SBuildAgent>();
    myFailedOnAgents = new ArrayList<SBuildAgent>();
    final Map<String, FailureRate> agentFailureRates = myTestData.getAgentFailureRates();
    for (Map.Entry<String, FailureRate> entry : agentFailureRates.entrySet()) {
      SBuildAgent agent = agentManager.findAgentByName(entry.getKey(), true);
      if (agent != null) {
        myAllAgents.add(agent);
        if (entry.getValue().hasFailures()) {
          myFailedOnAgents.add(agent);
        }
      }
    }
    Collections.sort(myAllAgents, new Comparator<SBuildAgent>() {
      public int compare(SBuildAgent agent1, SBuildAgent agent2) {
        FailureRate failureRate1 = agentFailureRates.get(agent1.getName());
        FailureRate failureRate2 = agentFailureRates.get(agent2.getName());
        int result = failureRate1.compareTo(failureRate2);
        return result != 0 ? result : agent1.compareTo(agent2);
      }
    });
  }

  @NotNull
  public TestData getTestData() {
    return myTestData;
  }

  public boolean isFailedOnlyInSingleBuildType() {
    return myAllBuildTypes.size() > 1 && myFailedInBuildTypes.size() == 1;
  }

  public boolean isFailedOnlyOnSingleAgent() {
    return myAllAgents.size() > 1 && myFailedOnAgents.size() == 1;
  }

  @NotNull
  public List<SBuildType> getFailedInBuildTypes() {
    return myFailedInBuildTypes;
  }

  @NotNull
  public List<SBuildType> getAllBuildTypes() {
    return myAllBuildTypes;
  }

  @NotNull
  public List<SBuildAgent> getAllAgents() {
    return myAllAgents;
  }

  @NotNull
  public List<SBuildAgent> getFailedOnAgents() {
    return myFailedOnAgents;
  }

  public Pair<Integer, Integer> getStats() {
    int totalRuns = 0;
    int failures = 0;
    for (FailureRate rate : myTestData.getBuildTypeFailureRates().values()) {
      totalRuns += rate.getTotalRuns();
      failures += rate.getFailures();
    }
    return new Pair<Integer, Integer>(totalRuns, failures);
  }

  public boolean isHasReason() {
    return myTestData.getReason() != null;
  }

  public boolean isWithoutChangesReason() {
    return myTestData.getReason() instanceof BuildWithoutChangesReason;
  }

  @NotNull
  public SBuild getBuildWithoutChanges() {
    Reason reason = myTestData.getReason();
    assert reason instanceof BuildWithoutChangesReason;
    long buildId = ((BuildWithoutChangesReason)reason).getBuildId();
    SBuild build = myBuildServer.findBuildInstanceById(buildId);
    assert build != null;
    return build;
  }

  public boolean isBuildsOnSameModificationReason() {
    return myTestData.getReason() instanceof BuildsOnSameModificationReason;
  }

  @NotNull
  public SBuild getFailedInBuild() {
    Reason reason = myTestData.getReason();
    assert reason instanceof BuildsOnSameModificationReason;
    long buildId = ((BuildsOnSameModificationReason)reason).getFailedInBuildId();
    SBuild build = myBuildServer.findBuildInstanceById(buildId);
    assert build != null;
    return build;
  }

  @NotNull
  public SBuild getSuccessfulInBuild() {
    Reason reason = myTestData.getReason();
    assert reason instanceof BuildsOnSameModificationReason;
    long buildId = ((BuildsOnSameModificationReason)reason).getSuccessfulInBuild();
    SBuild build = myBuildServer.findBuildInstanceById(buildId);
    assert build != null;
    return build;
  }

  public boolean isSuspiciousStatisticsReason() {
    return myTestData.getReason() instanceof SuspiciousFailureStatisticsReason;
  }

  @NotNull
  public Pair<Integer, Integer> getSuspiciousStatistics() {
    Reason reason = myTestData.getReason();
    assert reason instanceof SuspiciousFailureStatisticsReason;
    return new Pair<Integer, Integer>(((SuspiciousFailureStatisticsReason)reason).getTotalLength(),
                                      ((SuspiciousFailureStatisticsReason)reason).getNumber());
  }
}
