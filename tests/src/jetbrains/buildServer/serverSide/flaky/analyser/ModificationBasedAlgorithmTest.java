/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.STest;
import jetbrains.buildServer.serverSide.flaky.BaseServerTestCase;
import jetbrains.buildServer.tests.TestName;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
@Test
public class ModificationBasedAlgorithmTest extends BaseServerTestCase {
  private static final int FAIL = Status.FAILURE.getPriority();
  private static final int OK = Status.NORMAL.getPriority();

  private SProject myProject;
  private ModificationBasedAlgorithm myAlgorithm;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myProject = myProjectManager.createProject("project");
    myAlgorithm = new ModificationBasedAlgorithm(myServer);
  }

  public void test0() throws Exception {
    STest test = myTestManager.createTest(new TestName("test"), myProject.getProjectId());
    myAlgorithm.checkTest(test, Arrays.asList(rawData(1, OK, "", 1)));
  }

  @NotNull
  private static RawData rawData(long buildId, int status, @NotNull String btId, long modificationId) {
    RawData rawData = new RawData();
    rawData.set(buildId, 0L, status, btId, modificationId, "agent", 0L);
    return rawData;
  }
}
