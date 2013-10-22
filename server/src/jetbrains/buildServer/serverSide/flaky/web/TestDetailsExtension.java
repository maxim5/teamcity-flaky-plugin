/*
 * Copyright (c) 2000-2013 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.web;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.TestEx;
import jetbrains.buildServer.serverSide.flaky.data.TestAnalysisResult;
import jetbrains.buildServer.serverSide.flaky.data.TestAnalysisResultHolder;
import jetbrains.buildServer.serverSide.flaky.data.TestData;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.1
 */
public class TestDetailsExtension extends SimplePageExtension {
  @NotNull
  private final PluginDescriptor myDescriptor;
  private final TestAnalysisResultHolder myTestAnalysisResultHolder;

  public TestDetailsExtension(@NotNull PagePlaces pagePlaces,
                              @NotNull PluginDescriptor descriptor,
                              @NotNull TestAnalysisResultHolder testAnalysisResultHolder) {
    super(pagePlaces, PlaceId.TEST_DETAILS_BLOCK, "flakyDetails",
          descriptor.getPluginResourcesPath("/testDetailsExtension.jsp"));
    myDescriptor = descriptor;
    myTestAnalysisResultHolder = testAnalysisResultHolder;
    register();
  }

  @Override
  public boolean isAvailable(@NotNull HttpServletRequest request) {
    return findTest(request) != null;
  }

  @Override
  public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
    STest test = findTest(request);
    assert test != null;
    if (test instanceof TestEx) {
      TestAnalysisResult results = null;
      SProject project = ((TestEx) test).getProject();
      while (project != null) {
        TestAnalysisResult result = myTestAnalysisResultHolder.getTestAnalysisResult(project);
        if (result.getStartDate() != null) {
          results = result;
          break;
        }
        project = project.getParentProject();
      }

      if (results != null) {
        long testId = test.getTestNameId();
        TestData currentTestData = null;
        boolean isFlaky = false;
        boolean isSuspicious = false;
        boolean isAlwaysFailing = false;

        for (TestData testData : results.getFlakyTests()) {
          if (testId == testData.getTestId()) {
            currentTestData = testData;
            isFlaky = true;
          }
        }
        for (TestData testData : results.getSuspiciousTests()) {
          if (testId == testData.getTestId()) {
            currentTestData = testData;
            isSuspicious = true;
          }
        }
        for (TestData testData : results.getAlwaysFailingTests()) {
          if (testId == testData.getTestId()) {
            currentTestData = testData;
            isAlwaysFailing = true;
          }
        }

        model.put("project", project);
        model.put("testData", currentTestData);
        model.put("isFlaky", isFlaky);
        model.put("isSuspicious", isSuspicious);
        model.put("isAlwaysFailing", isAlwaysFailing);
        model.put("iconUrl", myDescriptor.getPluginResourcesPath("dice-16.png"));
      }
    }
  }

  @Nullable
  private static STest findTest(@NotNull HttpServletRequest request) {
    Object attribute = request.getAttribute("test");
    if (attribute != null && attribute instanceof STest) {
      return (STest) attribute;
    }
    return null;
  }
}
