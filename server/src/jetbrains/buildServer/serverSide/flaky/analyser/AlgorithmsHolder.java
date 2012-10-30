/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.flaky.data.TestAnalysisSettings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all test analysis algorithms.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class AlgorithmsHolder {
  private final SBuildServer myBuildServer;

  public AlgorithmsHolder(@NotNull SBuildServer buildServer) {
    myBuildServer = buildServer;
  }

  @NotNull
  public List<CheckAlgorithm> getAlgorithms(@NotNull TestAnalysisSettings settings) {
    List<CheckAlgorithm> algorithms = new ArrayList<CheckAlgorithm>();
    algorithms.add(new SimpleStatusAlgorithm());
    algorithms.add(new ModificationBasedAlgorithm(myBuildServer));
    algorithms.add(new StatisticAnalysisAlgorithm(settings));
    return algorithms;
  }
}
