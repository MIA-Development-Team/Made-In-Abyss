package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.altnoir.mementoinabyss.MementoInAbyss;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.level.levelgen.NoiseRouter;

/** Bridges C2ME's compiled density graph to its source graph without linking against C2ME classes. */
public final class C2meDensityFunctionCompat {
    private static final String ORIGINAL_ROUTER_METHOD = "c2me$getOriginalNoiseRouter";
    private static final AtomicBoolean LOOKUP_WARNING_LOGGED = new AtomicBoolean();

    public static NoiseRouter originalRouter(NoiseRouter activeRouter) {
        try {
            Object original =
                    activeRouter.getClass().getMethod(ORIGINAL_ROUTER_METHOD).invoke(activeRouter);
            if (original instanceof NoiseRouter sourceRouter && sourceRouter != activeRouter)
                return sourceRouter;
            return activeRouter;
        } catch (NoSuchMethodException absent) {
            // Normal non-C2ME path.
            return activeRouter;
        } catch (IllegalAccessException | InvocationTargetException e) {
            warnOnce(
                    "Could not read C2ME's original NoiseRouter; using the conservative "
                            + "pipeline fallback",
                    e);
        } catch (SecurityException e) {
            warnOnce(
                    "Access to C2ME's original NoiseRouter was denied; using the conservative "
                            + "pipeline fallback",
                    e);
        }
        return activeRouter;
    }

    private static void warnOnce(String message, Exception cause) {
        if (!LOOKUP_WARNING_LOGGED.compareAndSet(false, true)) return;
        if (cause == null) MementoInAbyss.LOGGER.warn(message);
        else MementoInAbyss.LOGGER.warn(message, cause);
    }

    private C2meDensityFunctionCompat() {}
}
