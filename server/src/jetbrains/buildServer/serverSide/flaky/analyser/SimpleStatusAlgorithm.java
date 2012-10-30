/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

import java.util.List;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.STest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class SimpleStatusAlgorithm implements CheckAlgorithm {
  @Nullable
  public CheckResult checkTest(@NotNull STest test, @NotNull List<RawData> rawDataList) {
    int status = rawDataList.get(0).getStatus();
    for (RawData rawData : rawDataList) {
      int currentStatus = rawData.getStatus();
      if (currentStatus != status) {
        return null;
      }
    }

    return status != Status.NORMAL.getPriority() ?
             CheckResult.ALWAYS_FAILING_RESULT :        // always a failure
             CheckResult.ORDINARY_RESULT;               // always successful
  }
}
