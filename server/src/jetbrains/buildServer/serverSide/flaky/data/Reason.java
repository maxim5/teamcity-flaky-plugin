/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky.data;

import java.io.Serializable;

/**
 * Represents the reason for choosing a particular test as "flaky", or "suspicious", etc.
 * <p>
 * The reason encapsulates all the details necessary to clarify the decision.
 *
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public interface Reason extends Serializable {
}
