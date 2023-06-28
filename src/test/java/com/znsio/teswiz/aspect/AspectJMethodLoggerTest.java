package com.znsio.teswiz.aspect;

import com.znsio.teswiz.aspects.AspectJMethodLoggers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class AspectJMethodLoggerTest {

    private Method method;

    @BeforeEach
    void initiateMethod(Method method){
        this.method = method;
    }

    @Test
    void validateLoggerWithNoArguments() {
        String loggerString = "";
        System.out.println(AspectJMethodLoggers.generateBeforeMethodAspectJLogger(method.getName(), method.getParameters()));
//        assertThat(AspectJMethodLoggers.generateBeforeMethodAspectJLogger(method.getName(), method.getParameters()))
//                .as("The string generated is different than expected").as(loggerString);
    }
}
