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
public class SuspiciousFailureStatisticsReason implements Reason {
  private final int myNumber;
  private final int myTotalLength;

  public SuspiciousFailureStatisticsReason(int number, int totalLength) {
    myNumber = number;
    myTotalLength = totalLength;
  }

  public int getNumber() {
    return myNumber;
  }

  public int getTotalLength() {
    return myTotalLength;
  }
}
