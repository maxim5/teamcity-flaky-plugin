/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

import jetbrains.buildServer.serverSide.STest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents the analysis algorithm for classifying the test into one of categories.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @see jetbrains.buildServer.serverSide.flaky.data.Type
 * @since 8.0
 */
public interface CheckAlgorithm {
  /**
   * Returns the check result of a single test based on raw data of its runs.
   *
   * @param test the test to analyse
   * @param rawDataList the raw data list
   * @return check result
   */
  @Nullable
  CheckResult checkTest(@NotNull STest test, @NotNull List<RawData> rawDataList);
}
