/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.web;

import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.flaky.analyser.TestsAnalyser;
import jetbrains.buildServer.serverSide.flaky.data.TestAnalysisSettings;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import static jetbrains.buildServer.serverSide.flaky.analyser.StatisticAnalysisAlgorithm.DEFAULT_AVERAGE_SERIES_LENGTH;
import static jetbrains.buildServer.serverSide.flaky.analyser.StatisticAnalysisAlgorithm.DEFAULT_MIN_SERIES_NUMBER;
import static jetbrains.buildServer.serverSide.flaky.data.TestAnalysisSettings.DEFAULT_PERIOD_DAYS;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class RunTestsAnalysisController extends BaseController {
  private final ProjectManager myProjectManager;
  private final TestsAnalyser myAnalyser;

  public RunTestsAnalysisController(@NotNull SBuildServer server,
                                    @NotNull WebControllerManager webControllerManager,
                                    @NotNull ProjectManager projectManager,
                                    @NotNull TestsAnalyser analyser) {
    super(server);
    webControllerManager.registerController("/analyzeTests.html", this);
    myProjectManager = projectManager;
    myAnalyser = analyser;
  }

  @Nullable
  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request,
                                  @NotNull HttpServletResponse response) throws Exception {
    String projectId = request.getParameter("projectId");
    if (projectId == null) {
      return null;
    }

    final SProject project = myProjectManager.findProjectById(projectId);
    if (project == null) {
      return null;
    }

    final TestAnalysisSettings settings = getSettingsFromRequest(request);

    new Thread(new Runnable() {
      public void run() {
        myAnalyser.analyseTestsInProject(project, settings);
      }
    }, "Tests analyser").start();

    return null;
  }

  @NotNull
  private static TestAnalysisSettings getSettingsFromRequest(@NotNull HttpServletRequest request) {
    List<String> excludeBuildTypes = getListParam(request, "excludeBuildTypes", ':');
    boolean analyseFullHistory = getBoolParam(request, "analyseFullHistory");
    int analyseTimePeriodDays = getIntParam(request, "analyseTimePeriodDays", DEFAULT_PERIOD_DAYS);
    boolean speedUpAlwaysFailing = getBoolParam(request, "speedUpAlwaysFailing");
    int minSeriesNumber = getIntParam(request, "minSeriesNumber", DEFAULT_MIN_SERIES_NUMBER);
    double averageSeriesLength = getDoubleParam(request, "averageSeriesLength", DEFAULT_AVERAGE_SERIES_LENGTH);
    return new TestAnalysisSettings(excludeBuildTypes, analyseTimePeriodDays,
                                    analyseFullHistory, speedUpAlwaysFailing,
                                    minSeriesNumber, averageSeriesLength);
  }

  @NotNull
  private static List<String> getListParam(@NotNull HttpServletRequest request, @NotNull String name, char separator) {
    String param = request.getParameter(name);
    return (param != null) ? StringUtil.split(param, true, separator) : Collections.<String>emptyList();
  }

  private static boolean getBoolParam(@NotNull HttpServletRequest request, @NotNull String name) {
    return "true".equals(request.getParameter(name));
  }

  private static int getIntParam(@NotNull HttpServletRequest request, @NotNull String name, int defaultValue) {
    String param = request.getParameter(name);
    if (param != null) {
      try {
        return Integer.parseInt(param);
      } catch (NumberFormatException e) {
        // ignore
      }
    }
    return defaultValue;
  }

  private static double getDoubleParam(@NotNull HttpServletRequest request, @NotNull String name, double defaultValue) {
    String param = request.getParameter(name);
    if (param != null) {
      try {
        return Double.parseDouble(param);
      } catch (NumberFormatException e) {
        // ignore
      }
    }
    return defaultValue;
  }
}
