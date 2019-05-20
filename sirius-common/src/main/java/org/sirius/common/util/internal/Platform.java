package org.sirius.common.util.internal;

import java.util.Locale;

import org.sirius.common.util.SystemPropertyUtil;
import org.sirius.common.util.internal.logging.InternalLogger;
import org.sirius.common.util.internal.logging.InternalLoggerFactory;

public class Platform {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Platform.class);

    private static final boolean IS_WINDOWS = isWindows0();

    /**
     * Return {@code true} if the JVM is running on Windows
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    private static boolean isWindows0() {
        boolean windows = SystemPropertyUtil.get("os.name", "").toLowerCase(Locale.US).contains("win");
        if (windows) {
            logger.debug("Platform: Windows");
        }
        return windows;
    }
}
