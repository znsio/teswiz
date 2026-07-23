package com.znsio.teswiz.web.playwright.screen;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlaywrightTsScreenBridgeFactory {
    private static final String EXECUTOR_FIELD_NAME = "screenActionExecutor";

    private final PlaywrightTsScreenModuleResolver moduleResolver;
    private final ConcurrentMap<Class<?>, Class<?>> generatedClasses = new ConcurrentHashMap<>();

    public PlaywrightTsScreenBridgeFactory() {
        this(new PlaywrightTsScreenModuleResolver());
    }

    PlaywrightTsScreenBridgeFactory(PlaywrightTsScreenModuleResolver moduleResolver) {
        this.moduleResolver = moduleResolver;
    }

    public <T> Optional<T> createIfSupported(Class<T> screenContract, Driver driver, Visual visually) {
        Optional<String> modulePath = moduleResolver.findModulePath(screenContract);
        if (modulePath.isEmpty()) {
            return Optional.empty();
        }
        Class<? extends T> generatedClass = createBridgeClass(screenContract);
        return Optional.of(instantiateBridge(generatedClass, screenContract, driver, visually, modulePath.get()));
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> createBridgeClass(Class<T> screenContract) {
        return (Class<? extends T>) generatedClasses.computeIfAbsent(screenContract, this::generateBridgeClass);
    }

    private Class<?> generateBridgeClass(Class<?> screenContract) {
        try {
            return new ByteBuddy()
                    .subclass(screenContract)
                    .name(screenContract.getName() + "PlaywrightTsGeneratedBridge")
                    .defineField(EXECUTOR_FIELD_NAME, PlaywrightTsScreenActionExecutor.class)
                    .method(ElementMatchers.isAbstract())
                    .intercept(MethodDelegation.toField(EXECUTOR_FIELD_NAME))
                    .make()
                    .load(screenContract.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate playwright-ts screen bridge for "
                    + screenContract.getName(), exception);
        }
    }

    private <T> T instantiateBridge(Class<? extends T> generatedClass, Class<T> screenContract, Driver driver,
            Visual visually, String modulePath) {
        try {
            T instance = generatedClass.getDeclaredConstructor().newInstance();
            Field executorField = generatedClass.getDeclaredField(EXECUTOR_FIELD_NAME);
            executorField.setAccessible(true);
            executorField.set(instance, new PlaywrightTsScreenActionExecutor(screenContract, driver, visually,
                    modulePath));
            return instance;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to instantiate playwright-ts screen bridge for "
                    + screenContract.getName(), exception);
        }
    }
}
