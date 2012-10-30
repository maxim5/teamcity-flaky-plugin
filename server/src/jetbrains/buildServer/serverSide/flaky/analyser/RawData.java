/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

/**
 * Holds the raw test run data.
 *
* @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
* @since 8.0
*/
class RawData {
  private long myBuildId;
  private long myTestId;
  private int myStatus;
  private String myBuildTypeId;
  private long myModificationId;
  private String myAgentName;
  private long myBuildStartTime;

  public void set(long buildId, long testId, int status, String buildTypeId,
                  long modificationId, String agentName, long buildStartTime) {
    myBuildId = buildId;
    myTestId = testId;
    myStatus = status;
    myBuildTypeId = buildTypeId;
    myModificationId = modificationId;
    myAgentName = agentName;
    myBuildStartTime = buildStartTime;
  }

  public long getBuildId() {
    return myBuildId;
  }

  public long getTestId() {
    return myTestId;
  }

  public int getStatus() {
    return myStatus;
  }

  public String getBuildTypeId() {
    return myBuildTypeId;
  }

  public long getModificationId() {
    return myModificationId;
  }

  public String getAgentName() {
    return myAgentName;
  }

  public long getBuildStartTime() {
    return myBuildStartTime;
  }
}
