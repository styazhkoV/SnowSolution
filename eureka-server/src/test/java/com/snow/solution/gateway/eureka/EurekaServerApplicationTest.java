package com.snow.solution.gateway.eureka;

import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EurekaServerApplicationTest {

    @Test
    void declaresSpringBootAndEurekaServerApplications() {
        assertTrue(EurekaServerApplication.class.isAnnotationPresent(SpringBootApplication.class));
        assertTrue(EurekaServerApplication.class.isAnnotationPresent(EnableEurekaServer.class));
    }

    @Test
    void mainHasStandardJavaEntryPointSignature() throws NoSuchMethodException {
        var main = EurekaServerApplication.class.getDeclaredMethod("main", String[].class);

        assertTrue(Modifier.isPublic(main.getModifiers()));
        assertTrue(Modifier.isStatic(main.getModifiers()));
        assertEquals(void.class, main.getReturnType());
    }
}
