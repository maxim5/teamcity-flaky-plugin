/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.SQLRunner;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.db.DBAction;
import jetbrains.buildServer.serverSide.db.DBException;
import jetbrains.buildServer.serverSide.db.DBFunctions;
import jetbrains.buildServer.serverSide.db.SQLRunnerEx;
import jetbrains.buildServer.serverSide.db.queries.GenericQuery;
import jetbrains.buildServer.serverSide.flaky.data.*;
import jetbrains.buildServer.serverSide.stat.TestFailureRate;
import jetbrains.buildServer.serverSide.stat.TestFailuresStatistics;
import jetbrains.buildServer.util.SimpleObjectPool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents the tests analyser.
 * Responsible for fetching the raw data from the DB and running the algorithms.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class TestsAnalyser {
  private static final int BUFFER_SIZE = 1024;
  private static final int POOL_SIZE = 1024;
  private static final float FAILURE_RATE = 0.01f;
  private static final int HEURISTIC_MIN_FAILURES_NUMBER = 10;
  private static final int MIN_RUNS_NUMBER = 1;

  private static final long NO_BUILDS_TO_ANALYSE = -1;

  private final TestFailuresStatistics myFailuresStatistics;
  private final SQLRunner mySQLRunner;

  private final AlgorithmsHolder myAlgorithmsHolder;
  private final TestAnalysisProgressManager myProgressManager;
  private final TestAnalysisResultHolder myHolder;

  public TestsAnalyser(@NotNull TestFailuresStatistics failuresStatistics,
                       @NotNull SQLRunner sqlRunner,
                       @NotNull AlgorithmsHolder algorithmsHolder,
                       @NotNull TestAnalysisProgressManager progressManager,
                       @NotNull TestAnalysisResultHolder holder) {
    myFailuresStatistics = failuresStatistics;
    mySQLRunner = sqlRunner;
    myAlgorithmsHolder = algorithmsHolder;
    myProgressManager = progressManager;
    myHolder = holder;
  }

  public void analyseTestsInProject(@NotNull SProject project,
                                    @NotNull TestAnalysisSettings settings) {
    TestAnalysisProgress progress = myProgressManager.getProgressFor(project);
    if (!progress.getLock().compareAndSet(false, true)) {
      return;
    }

    try {
      // Starting...
      progress.start("Starting...");
      TestAnalysisResult result = new TestAnalysisResult();
      result.setStartDate(new Date());

      // Preparing data...
      progress.setCurrentStep("Preparing data...");
      SimpleObjectPool<RawData> rawDataPool = getRawDataPool();
      List<TestData> testDataList = new ArrayList<TestData>();
      List<CheckAlgorithm> algorithms = myAlgorithmsHolder.getAlgorithms(settings);

      try {
        List<RawData> buffer = new ArrayList<RawData>(BUFFER_SIZE);    // reuse the allocated buffer
        Set<String> buildTypeIds = getBuildTypeIds(project, settings);

        // Collecting failure statistics...
        List<TestFailureRate> allFailingTests =
          myFailuresStatistics.getFailingTests(project, FAILURE_RATE);
        progress.setTotalSize(allFailingTests.size());
        result.setTotalTests(allFailingTests.size());

        // Calculating the start build...
        long buildId = findBuildIdForTest(null, settings.getAnalyseTimePeriod());
        if (buildId == NO_BUILDS_TO_ANALYSE) {
          return;
        }

        // Analysis...
        progress.setCurrentStep("Analysing tests failures...");
        for (TestFailureRate testFailureRate : allFailingTests) {
          STest test = testFailureRate.getTest();
          int failureCount = testFailureRate.getFailureCount();
          int successCount = testFailureRate.getSuccessCount();
          int totalCount = failureCount + successCount;

          if (settings.isSpeedUpAlwaysFailing() &&
              failureCount >= HEURISTIC_MIN_FAILURES_NUMBER && successCount == 0) {
            testDataList.add(new TestData(test));
          } else if (totalCount > MIN_RUNS_NUMBER) {
            TestData testData = getTestData(test, buildId, algorithms,
                                            buildTypeIds, rawDataPool, buffer);
            if (testData != null) {
              testDataList.add(testData);
            }
          }
          progress.incDoneSize();
        }
      } finally {
        // Finishing...
        progress.setCurrentStep("Finishing...");
        result.setFinishDate(new Date());
        result.setTests(testDataList);
        result.setSettings(settings);
        myHolder.putTestAnalysisResult(project, result);
      }
    } finally {
      progress.getLock().set(false);
      myProgressManager.clearProgressFor(project);
    }
  }

  @NotNull
  public TestData getTestData(long testId, @NotNull SProject project,
                              @NotNull TestAnalysisResult lastResult) {
    List<RawData> buffer = new ArrayList<RawData>(BUFFER_SIZE);
    TestAnalysisSettings settings = lastResult.getSettings();
    Set<String> buildTypeIds = getBuildTypeIds(project, settings);
    SimpleObjectPool<RawData> rawDataPool = getRawDataPool();
    long buildId = findBuildIdForTest(lastResult.getStartDate(),
                                      settings.getAnalyseTimePeriod());
    if (buildId == NO_BUILDS_TO_ANALYSE) {
      return new TestData(testId, project.getProjectId());
    }
    collectRawData(testId, buildId, buildTypeIds, rawDataPool, buffer);
    return buildTestData(testId, project.getProjectId(), buildId, null, buffer);
  }

  @Nullable
  private TestData getTestData(@NotNull STest test,
                               long buildId,
                               @NotNull List<CheckAlgorithm> algorithms,
                               @NotNull final Set<String> buildTypeIds,
                               @NotNull final SimpleObjectPool<RawData> rawDataPool,
                               @NotNull final List<RawData> buffer) {
    buffer.clear();
    collectRawData(test.getTestNameId(), buildId, buildTypeIds, rawDataPool, buffer);

    if (buffer.size() <= 1) {
      // Not enough data to analyze.
      return null;
    }
    return runAlgorithms(algorithms, test, buildId, buffer);
  }

  private void collectRawData(final long testId,
                              final long buildId,
                              @NotNull final Set<String> buildTypeIds,
                              @NotNull final SimpleObjectPool<RawData> rawDataPool,
                              @NotNull final List<RawData> buffer) {
    ((SQLRunnerEx) mySQLRunner).withDB(new DBAction<Object>() {
      public Object run(DBFunctions dbf) throws DBException {
        new GenericQuery<Object>(GET_TEST_DATA_SQL, new GenericQuery.ResultSetProcessor<Object>() {
          @Nullable
          public Object process(ResultSet rs) throws SQLException {
            while (rs.next()) {
              long buildId = rs.getLong(1);
              long testId = rs.getLong(2);
              int status = rs.getInt(3);
              String buildTypeId = rs.getString(4);
              long modificationId = rs.getLong(5);
              String agentName = rs.getString(6);
              long buildStartTime = rs.getLong(7);

              if (!buildTypeIds.contains(buildTypeId)) {
                // The test runs in another projects don't matter here.
                continue;
              }

              RawData rawData = rawDataPool.getFromPool();
              rawData.set(buildId, testId, status, buildTypeId,
                          modificationId, agentName, buildStartTime);
              buffer.add(rawData);
            }
            return null;
          }
        }).execute(dbf, buildId, buildId, buildId, testId);

        return null;
      }
    });
  }

  @Nullable
  private TestData runAlgorithms(@NotNull List<CheckAlgorithm> algorithms,
                                 @NotNull STest test,
                                 long buildId,
                                 @NotNull List<RawData> data) {
    for (CheckAlgorithm algorithm : algorithms) {
      CheckResult checkResult = algorithm.checkTest(test, data);
      if (checkResult != null && !checkResult.getType().isOrdinary()) {
        return buildTestData(test.getTestNameId(), test.getProjectId(), buildId, checkResult, data);
      }
    }
    return null;    // we're not sure about this test.
  }

  @NotNull
  private TestData buildTestData(long testId,
                                 @NotNull String projectId,
                                 long buildId,
                                 @Nullable CheckResult checkResult,
                                 @NotNull List<RawData> data) {
    FailureRate failureRate;
    Map<String, FailureRate> buildTypeFailureRates = new HashMap<String, FailureRate>();
    Map<String, FailureRate> agentFailureRates = new HashMap<String, FailureRate>();

    for (RawData rawData : data) {
      String buildTypeId = rawData.getBuildTypeId();
      String agentName = rawData.getAgentName();
      int status = rawData.getStatus();
      boolean failure = Status.getStatus(status).isFailed();

      failureRate = buildTypeFailureRates.get(buildTypeId);
      if (failureRate == null) {
        failureRate = new FailureRate(1, failure ? 1 : 0);
        buildTypeFailureRates.put(buildTypeId, failureRate);
      } else {
        failureRate.incTotalRuns();
        if (failure) failureRate.incFailures();
      }

      failureRate = agentFailureRates.get(agentName);
      if (failureRate == null) {
        failureRate = new FailureRate(1, failure ? 1 : 0);
        agentFailureRates.put(agentName, failureRate);
      } else {
        failureRate.incTotalRuns();
        if (failure) failureRate.incFailures();
      }
    }

    Type type = checkResult != null ? checkResult.getType() : null;
    Reason reason = checkResult != null ? checkResult.getReason() : null;
    return new TestData(testId, projectId,
                        buildTypeFailureRates, agentFailureRates,
                        buildId, type, reason);
  }

  private long findBuildIdForTest(@Nullable Date startDate, long period) {
    if (period < 0) {
      return 0;
    }

    if (startDate == null) {
      startDate = new Date();
    }
    long timestamp = startDate.getTime() - period;
    return new GenericQuery<Long>(GET_FIRST_BUILD_ID_SQL,
                                  new GenericQuery.ReturnSingle<Long>(NO_BUILDS_TO_ANALYSE))
           .execute(mySQLRunner, timestamp);
  }

  @NotNull
  private static SimpleObjectPool<RawData> getRawDataPool() {
    return new SimpleObjectPool<RawData>(
      new SimpleObjectPool.ObjectFactory<RawData>() {
        @NotNull
        public RawData create() {
          return new RawData();
        }
      }, POOL_SIZE);
  }

  @NotNull
  private static Set<String> getBuildTypeIds(@NotNull SProject project,
                                             @NotNull TestAnalysisSettings settings) {
    Set<String> result = new HashSet<String>();
    for (SBuildType buildType : project.getBuildTypes()) {
      result.add(buildType.getBuildTypeId());
    }
    result.removeAll(settings.getExcludeBuildTypes());
    return result;
  }

  private static final String GET_TEST_DATA_SQL =
    "select ti.build_id, ti.test_name_id, ti.status,                                    \n" +
    "       bs.build_type_id, bs.modification_id,                                       \n" +
    "       h.agent_name, h.build_start_time_server                                     \n" +
    "    from test_info ti                                                              \n" +
    "    join build_state bs on bs.build_id=ti.build_id                                 \n" +
    "                       and bs.build_id is not null and bs.build_id >= ?            \n" +
    "    join history h on h.build_id=ti.build_id                                       \n" +
    "                       and h.build_id is not null and h.build_id >= ?              \n" +
    "    where ti.build_id is not null and ti.build_id >= ?                             \n" +
    "      and ti.test_name_id = ? and ti.status != 0                                   \n" +
    "    order by bs.modification_id                                                    \n";

  private static final String GET_FIRST_BUILD_ID_SQL =
    "select min(build_id) from history                                                  \n" +
    "    where build_start_time_server > ?                                              \n";
}
