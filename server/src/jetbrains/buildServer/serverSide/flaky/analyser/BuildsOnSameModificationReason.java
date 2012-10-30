/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.flaky.data.Reason;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class BuildsOnSameModificationReason implements Reason {
  private final long myFailedInBuildId;
  private final long mySuccessfulInBuild;

  public BuildsOnSameModificationReason(int status1, long build1, int status2, long build2) {
    assert status1 != status2;
    myFailedInBuildId = status1 == Status.FAILURE.getPriority() ? build1 : build2;
    mySuccessfulInBuild = status1 == Status.FAILURE.getPriority() ? build2 : build1;
  }

  public long getFailedInBuildId() {
    return myFailedInBuildId;
  }

  public long getSuccessfulInBuild() {
    return mySuccessfulInBuild;
  }
}
