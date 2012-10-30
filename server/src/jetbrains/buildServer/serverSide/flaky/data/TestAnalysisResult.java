/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A serializable entity holding the last results of test analysis (in a project).
 * The data is stored on disk and used as a cache.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class TestAnalysisResult implements Serializable {
  public static final TestAnalysisResult EMPTY_FLAKY_TESTS = new TestAnalysisResult();

  private List<TestData> myFlakyTests = Collections.emptyList();
  private List<TestData> mySuspiciousTests = Collections.emptyList();
  private List<TestData> myAlwaysFailingTests = Collections.emptyList();

  private TestAnalysisSettings mySettings = TestAnalysisSettings.DEFAULT_SETTINGS;
  private Date myStartDate = null;
  private Date myFinishDate = null;
  private int myTotalTests = 0;

  public TestAnalysisResult() {
  }

  @NotNull
  public List<TestData> getFlakyTests() {
    return myFlakyTests;
  }

  @NotNull
  public List<TestData> getSuspiciousTests() {
    return mySuspiciousTests;
  }

  @NotNull
  public List<TestData> getAlwaysFailingTests() {
    return myAlwaysFailingTests;
  }

  @NotNull
  public TestAnalysisSettings getSettings() {
    return mySettings;
  }

  @Nullable
  public Date getStartDate() {
    return myStartDate;
  }

  @Nullable
  public Date getFinishDate() {
    return myFinishDate;
  }

  public int getTotalTests() {
    return myTotalTests;
  }

  @Nullable
  public TestData findTest(long testId) {
    for (TestData testData : myFlakyTests) {
      if (testId == testData.getTestId()) {
        return testData;
      }
    }
    for (TestData testData : mySuspiciousTests) {
      if (testId == testData.getTestId()) {
        return testData;
      }
    }
    for (TestData testData : myAlwaysFailingTests) {
      if (testId == testData.getTestId()) {
        return testData;
      }
    }
    return null;
  }

  public void setTests(@NotNull List<TestData> allTests) {
    myFlakyTests = new ArrayList<TestData>();
    mySuspiciousTests = new ArrayList<TestData>();
    myAlwaysFailingTests = new ArrayList<TestData>();
    for (TestData test : allTests) {
      Type type = test.getType();
      if (type.isAlwaysFailing()) {
        myAlwaysFailingTests.add(test);
      } else if (type.isSuspicious()) {
        mySuspiciousTests.add(test);
      } else if (type.isFlaky()) {
        myFlakyTests.add(test);
      } else {
        throw new RuntimeException("Unexpected type: " + type);
      }
    }
  }

  public void setSettings(@NotNull TestAnalysisSettings settings) {
    mySettings = settings;
  }

  public void setStartDate(@NotNull Date startDate) {
    myStartDate = startDate;
  }

  public void setFinishDate(@NotNull Date finishDate) {
    myFinishDate = finishDate;
  }

  public void setTotalTests(int totalTests) {
    myTotalTests = totalTests;
  }
}
