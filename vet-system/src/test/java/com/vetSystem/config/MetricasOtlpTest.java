package com.vetSystem.config;

import io.micrometer.registry.otlp.OtlpMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

// Con el envio activado tiene que existir el registry que exporta las metricas por OTLP
@SpringBootTest(properties = {
        "management.otlp.metrics.export.enabled=true",
        "management.otlp.metrics.export.url=http://localhost:4318/v1/metrics"
})
class MetricasOtlpTest {
    @Autowired
    private ApplicationContext context;

    @Test
    void conEnvioActivado_existeElRegistryOtlp() {
        assertThat(context.getBeansOfType(OtlpMeterRegistry.class)).isNotEmpty();
    }
}
