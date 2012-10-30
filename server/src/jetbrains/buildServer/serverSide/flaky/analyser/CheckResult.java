/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.analyser;

import jetbrains.buildServer.serverSide.flaky.data.Reason;
import jetbrains.buildServer.serverSide.flaky.data.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Check result for a single test.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class CheckResult {
  public static final CheckResult ORDINARY_RESULT = new CheckResult(Type.ORDINARY);
  public static final CheckResult ALWAYS_FAILING_RESULT = new CheckResult(Type.ALWAYS_FAILING);

  private final Type myType;
  private final Reason myReason;

  public CheckResult(@NotNull Type type) {
    myType = type;
    myReason = null;
  }

  public CheckResult(@NotNull Type type, @NotNull Reason reason) {
    myType = type;
    myReason = reason;
  }

  public Type getType() {
    return myType;
  }

  @Nullable
  public Reason getReason() {
    return myReason;
  }
}
