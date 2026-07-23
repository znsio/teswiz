package com.znsio.teswiz.screen;

import org.apache.commons.lang3.NotImplementedException;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;

public final class ScreenInstanceFactory {
    private ScreenInstanceFactory() {
    }

    public static <T> T create(Class<? extends T> implementationClass, Driver driver, Visual visually) {
        try {
            return implementationClass.getConstructor(Driver.class, Visual.class).newInstance(driver, visually);
        } catch (ReflectiveOperationException exception) {
            throw new NotImplementedException(
                    "Unable to instantiate screen: " + implementationClass.getName(),
                    exception);
        }
    }
}
