/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.flaky.data.Reason;
import jetbrains.buildServer.serverSide.flaky.data.Type;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class ModificationBasedAlgorithm implements CheckAlgorithm {
  private final SBuildServer myBuildServer;
  private final Map<Long, Integer> myStatusMap;

  public ModificationBasedAlgorithm(@NotNull SBuildServer buildServer) {
    myBuildServer = buildServer;
    myStatusMap = new HashMap<Long, Integer>();
  }

  @Nullable
  public CheckResult checkTest(@NotNull STest test, @NotNull List<RawData> rawDataList) {
    CheckResult checkResult = tryToFindInOneBuildType(rawDataList);
    if (checkResult == null) {
      checkResult = tryToFindInSeveralBuildTypes(rawDataList);
    }
    return checkResult;
  }

  @Nullable
  private CheckResult tryToFindInSeveralBuildTypes(@NotNull List<RawData> rawDataList) {
    myStatusMap.clear();
    for (RawData rawData : rawDataList) {
      long modificationId = rawData.getModificationId();
      int currentStatus = rawData.getStatus();
      Integer status = myStatusMap.get(modificationId);
      if (status != null && status != currentStatus) {
        Reason reason = null;

        RawData matched = findDataWith(modificationId, status, null, rawDataList);
        if (matched != null) {
          // The test has different results on same sources (in different build types).
          reason = new BuildsOnSameModificationReason(status, matched.getBuildId(),
                                                      currentStatus, rawData.getBuildId());
        }

        if (reason != null) {
          return new CheckResult(Type.FLAKY, reason);
        }
      }
      if (status == null) {
        myStatusMap.put(modificationId, currentStatus);
      }
    }
    return null;
  }

  @Nullable
  private CheckResult tryToFindInOneBuildType(@NotNull List<RawData> rawDataList) {
    myStatusMap.clear();
    for (RawData rawData : rawDataList) {
      long hash = getHash(rawData);

      int currentStatus = rawData.getStatus();
      Integer status = myStatusMap.get(hash);
      if (status != null && status != currentStatus) {
        Reason reason = null;
        long buildId = getMaxBuildInListWithHash(rawDataList, hash);

        if (Status.getStatus(currentStatus).isFailed() && isBuildWithoutChanges(buildId)) {
          // The test has different results in build without changes.
          reason = new BuildWithoutChangesReason(buildId);
        } else {
          RawData matched = findDataWith(rawData.getModificationId(), status,
                                         rawData.getBuildTypeId(), rawDataList);
          if (matched != null) {
            // The test has different results on same sources.
            reason = new BuildsOnSameModificationReason(status, matched.getBuildId(),
                                                        currentStatus, rawData.getBuildId());
          }
        }

        if (reason != null) {
          return new CheckResult(Type.FLAKY, reason);
        }
      }
      if (status == null) {
        myStatusMap.put(hash, currentStatus);
      }
    }
    return null;
  }

  private static long getHash(@NotNull RawData rawData) {
    long modificationId = rawData.getModificationId();
    String buildTypeId = rawData.getBuildTypeId();
    return buildTypeId.hashCode() * 17L + modificationId * 7L;
  }

  private static long getMaxBuildInListWithHash(@NotNull List<RawData> rawDataList, long hash) {
    long maxBuildId = 0;
    for (RawData rawData : rawDataList) {
      long buildId = rawData.getBuildId();
      if (buildId > maxBuildId && hash == getHash(rawData)) {
        maxBuildId = buildId;
      }
    }
    return maxBuildId;
  }

  private boolean isBuildWithoutChanges(long buildId) {
    SBuild build = myBuildServer.findBuildInstanceById(buildId);
    return build != null && build.getChanges(SelectPrevBuildPolicy.SINCE_LAST_BUILD, false).isEmpty();
  }

  @Nullable
  private static RawData findDataWith(long modificationId, int status, @Nullable String buildTypeId,
                                      @NotNull List<RawData> rawDataList) {
    for (RawData rawData : rawDataList) {
      if (rawData.getModificationId() == modificationId &&
          rawData.getStatus() == status &&
          (buildTypeId == null || rawData.getBuildTypeId().equals(buildTypeId))) {
        return rawData;
      }
    }
    return null;
  }
}
