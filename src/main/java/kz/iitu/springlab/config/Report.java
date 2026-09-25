package kz.iitu.springlab.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.bind.DefaultValue;
import java.time.Duration;

public record Report(
        @NotBlank String timezone,
        @NotNull @DefaultValue("30d") Duration retention,
        @DefaultValue("true") boolean includeCharts) {
}
