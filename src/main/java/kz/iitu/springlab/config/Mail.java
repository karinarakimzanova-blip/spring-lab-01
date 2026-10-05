package kz.iitu.springlab.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;
import java.time.Duration;

@Validated
public record Mail(
        @NotBlank @Email String from,
        @Min(1) @Max(10) @DefaultValue("3") int retryCount,
        @DefaultValue("5s") Duration timeout,
        @DefaultValue("true") boolean enabled) {
}

