/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.web;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.flaky.data.TestAnalysisProgress;
import jetbrains.buildServer.serverSide.flaky.data.TestAnalysisProgressManager;
import jetbrains.buildServer.serverSide.flaky.data.TestAnalysisResultHolder;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.project.ProjectTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class TestsAnalysisTab extends ProjectTab {
  private final SBuildServer myBuildServer;
  private final STestManager myTestManager;
  private final BuildAgentManager myAgentManager;
  private final TestAnalysisProgressManager myProgressManager;
  private final TestAnalysisResultHolder myHolder;

  public TestsAnalysisTab(@NotNull SBuildServer buildServer,
                          @NotNull PagePlaces pagePlaces,
                          @NotNull ProjectManager projectManager,
                          @NotNull PluginDescriptor descriptor,
                          @NotNull STestManager testManager,
                          @NotNull BuildAgentManager agentManager,
                          @NotNull TestAnalysisProgressManager progressManager,
                          @NotNull TestAnalysisResultHolder holder) {
    super("analysis", "Tests Analysis", pagePlaces, projectManager,
          descriptor.getPluginResourcesPath("/analysis.jsp"));
    myBuildServer = buildServer;
    myTestManager = testManager;
    myAgentManager = agentManager;
    myProgressManager = progressManager;
    myHolder = holder;

    setPosition(PositionConstraint.after("mutedProblems"));
    addCssFile("/css/viewModification.css");
    addJsFile("/js/bs/blocksWithHeader.js");
    addJsFile("/js/bs/buildResultsDiv.js");
    addJsFile("/js/bs/testDetails.js");
    addCssFile(descriptor.getPluginResourcesPath("/analysis.css"));
    addJsFile(descriptor.getPluginResourcesPath("/analysis.js"));
  }

  @Override
  protected void fillModel(@NotNull Map<String, Object> model,
                           @NotNull HttpServletRequest request,
                           @NotNull SProject project,
                           @Nullable SUser user) {
    TestAnalysisProgress progress = myProgressManager.getProgressFor(project);
    TestsAnalysisBean bean = new TestsAnalysisBean(myBuildServer, myTestManager,
                                                   getProjectManager(), myAgentManager,
                                                   progress, myHolder, project);
    model.put("bean", bean);
  }
}
