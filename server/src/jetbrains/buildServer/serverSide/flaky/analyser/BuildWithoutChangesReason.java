/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

import jetbrains.buildServer.serverSide.flaky.data.Reason;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class BuildWithoutChangesReason implements Reason {
  private final long myBuildId;

  public BuildWithoutChangesReason(long buildId) {
    myBuildId = buildId;
  }

  public long getBuildId() {
    return myBuildId;
  }
}
