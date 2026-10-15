package com.snow.solution.gateway;

import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiGatewayApplicationTest {

    @Test
    void declaresSpringBootApplication() {
        assertTrue(ApiGatewayApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }

    @Test
    void mainHasStandardJavaEntryPointSignature() throws NoSuchMethodException {
        var main = ApiGatewayApplication.class.getDeclaredMethod("main", String[].class);

        assertTrue(Modifier.isPublic(main.getModifiers()));
        assertTrue(Modifier.isStatic(main.getModifiers()));
        assertEquals(void.class, main.getReturnType());
    }
}