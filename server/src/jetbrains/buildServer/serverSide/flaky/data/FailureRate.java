/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.data;

import java.io.Serializable;

/**
 * Represents the failure rate of one test (in a build type, on an agent, etc).
 *
* @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
* @since 8.0
*/
public class FailureRate implements Serializable, Comparable<FailureRate> {
  private int myTotalRuns;
  private int myFailures;

  public FailureRate(int totalRuns, int failures) {
    myTotalRuns = totalRuns;
    myFailures = failures;
  }

  public int getTotalRuns() {
    return myTotalRuns;
  }

  public int getFailures() {
    return myFailures;
  }

  public boolean hasFailures() {
    return myFailures > 0;
  }

  public boolean isAllFailures() {
    return myTotalRuns == myFailures;
  }

  public void incTotalRuns() {
    ++myTotalRuns;
  }

  public void incFailures() {
    ++myFailures;
  }

  public int compareTo(FailureRate that) {
    Double thisRate = (1.0 * myFailures) / myTotalRuns;
    Double thatRate = (1.0 * that.myFailures) / that.myTotalRuns;
    return thatRate.compareTo(thisRate);
  }
}
