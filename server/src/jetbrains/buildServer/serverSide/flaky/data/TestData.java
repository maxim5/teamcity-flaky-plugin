/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.data;

import jetbrains.buildServer.serverSide.STest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Represents the test analysis for a single test.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class TestData implements Serializable {
  private static final long NO_BUILD_ID = -1;

  private final long myTestId;
  private final String myProjectId;
  private final Map<String, FailureRate> myBuildTypeFailureRates;
  private final Map<String, FailureRate> myAgentFailureRates;
  private final long myFromBuildId;
  private final Type myType;
  private final Reason myReason;

  public TestData(long testId,
                  @NotNull String projectId,
                  @NotNull Map<String, FailureRate> buildTypeFailureRates,
                  @NotNull Map<String, FailureRate> agentFailureRates,
                  long fromBuildId,
                  @Nullable Type type,
                  @Nullable Reason reason) {
    myProjectId = projectId;
    myTestId = testId;
    myBuildTypeFailureRates = buildTypeFailureRates;
    myAgentFailureRates = agentFailureRates;
    myFromBuildId = fromBuildId;
    myType = type != null ? type : getType(buildTypeFailureRates.values());
    myReason = reason;
  }

  public TestData(long testId, @NotNull String projectId) {
    this(testId, projectId,
         Collections.<String, FailureRate>emptyMap(),
         Collections.<String, FailureRate>emptyMap(),
         NO_BUILD_ID, null, null);
  }

  public TestData(@NotNull STest test) {
    this(test.getTestNameId(), test.getProjectId());
  }

  public TestData(@NotNull STest test,
                  @NotNull Map<String, FailureRate> buildTypeFailureRates,
                  @NotNull Map<String, FailureRate> agentFailureRates) {
    this(test.getTestNameId(), test.getProjectId(),
         buildTypeFailureRates, agentFailureRates,
         NO_BUILD_ID, null, null);
  }

  public long getTestId() {
    return myTestId;
  }

  @NotNull
  public String getProjectId() {
    return myProjectId;
  }

  @NotNull
  public Map<String, FailureRate> getBuildTypeFailureRates() {
    return myBuildTypeFailureRates;
  }

  @NotNull
  public Map<String, FailureRate> getAgentFailureRates() {
    return myAgentFailureRates;
  }

  public long getFromBuildId() {
    return myFromBuildId;
  }

  public boolean wasProcessed() {
    return myFromBuildId != NO_BUILD_ID;
  }

  @NotNull
  public Type getType() {
    return myType;
  }

  @Nullable
  public Reason getReason() {
    return myReason;
  }

  @NotNull
  private static Type getType(@NotNull Collection<FailureRate> failureRates) {
    for (FailureRate failureRate : failureRates) {
      if (!failureRate.isAllFailures()) {
        return Type.FLAKY;
      }
    }
    return Type.ALWAYS_FAILING;
  }
}
