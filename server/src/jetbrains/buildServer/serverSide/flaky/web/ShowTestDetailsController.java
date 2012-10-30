/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.flaky.analyser.TestsAnalyser;
import jetbrains.buildServer.serverSide.flaky.data.TestAnalysisResult;
import jetbrains.buildServer.serverSide.flaky.data.TestAnalysisResultHolder;
import jetbrains.buildServer.serverSide.flaky.data.TestData;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class ShowTestDetailsController extends BaseController {
  private final String JSP_PATH;

  private final ProjectManager myProjectManager;
  private final BuildAgentManager myBuildAgentManager;
  private final TestAnalysisResultHolder myHolder;
  private final TestsAnalyser myTestsAnalyser;

  public ShowTestDetailsController(@NotNull SBuildServer server,
                                   @NotNull WebControllerManager webControllerManager,
                                   @NotNull PluginDescriptor descriptor,
                                   @NotNull ProjectManager projectManager,
                                   @NotNull BuildAgentManager buildAgentManager,
                                   @NotNull TestAnalysisResultHolder holder,
                                   @NotNull TestsAnalyser testsAnalyser) {
    super(server);
    webControllerManager.registerController("/flakyTestDetails.html", this);
    JSP_PATH = descriptor.getPluginResourcesPath("/testDetails.jsp");

    myBuildAgentManager = buildAgentManager;
    myProjectManager = projectManager;
    myHolder = holder;
    myTestsAnalyser = testsAnalyser;
  }

  @Nullable
  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request,
                                  @NotNull HttpServletResponse response) throws Exception {
    String testNameId = request.getParameter("testNameId");
    String projectId = request.getParameter("projectId");
    if (testNameId == null || projectId == null) {
      return simpleView("Incorrect request: not enough parameters");
    }

    SProject project = myProjectManager.findProjectById(projectId);
    if (project == null) {
      return simpleView("Incorrect request: project not found");
    }

    long longTestNameId;
    try {
      longTestNameId = Long.parseLong(testNameId);
    } catch (NumberFormatException e) {
      return simpleView("Incorrect request: test can't be parsed");
    }

    TestAnalysisResult result = myHolder.getTestAnalysisResult(project);
    TestData testData = result.findTest(longTestNameId);
    if (testData == null || !testData.wasProcessed()) {
      testData = myTestsAnalyser.getTestData(longTestNameId, project, result);
    }
    TestWebDetails testWebDetails = new TestWebDetails(myServer, myProjectManager,
                                                       myBuildAgentManager, testData);

    ModelAndView modelAndView = new ModelAndView(JSP_PATH);
    modelAndView.getModel().put("testDetails", testWebDetails);
    return modelAndView;
  }
}
