/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.data;

/**
 * Represents the type of test after analysis:
 * <ul>
 *   <li>ordinary</li>
 *   <li>flaky</li>
 *   <li>suspicious</li>
 *   <li>always failing</li>
 * </ul>
 * <p>
 * A test can fall into just one category, can't be <i>flaky</i> and <i>suspicious</i>
 * at the same time.
 *
* @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
* @since 8.0
*/
public enum Type {
  ORDINARY,
  FLAKY,
  SUSPICIOUS,
  ALWAYS_FAILING;

  public boolean isOrdinary() {
    return this == ORDINARY;
  }

  public boolean isFlaky() {
    return this == FLAKY;
  }

  public boolean isSuspicious() {
    return this == SUSPICIOUS;
  }

  public boolean isAlwaysFailing() {
    return this == ALWAYS_FAILING;
  }
}
