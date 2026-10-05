package kz.iitu.springlab.web;

import kz.iitu.springlab.config.*;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/lab3")
public class Lab3Controller {

    private final AppProperties props;
    private final EnvironmentBanner banner;
    private final Environment environment;

    public Lab3Controller(AppProperties props, EnvironmentBanner banner, Environment environment) {
        this.props = props;
        this.banner = banner;
        this.environment = environment;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        return Map.ofEntries(
                Map.entry("owner", props.owner()),
                Map.entry("group", props.group()),
                Map.entry("mailFrom", props.mail().from()),
                Map.entry("mailRetryCount", props.mail().retryCount()),
                Map.entry("mailTimeout", props.mail().timeout().toString()),
                Map.entry("mailEnabled", props.mail().enabled()),
                Map.entry("serverPort", Objects.requireNonNullElse(environment.getProperty("server.port"), "unknown")),
                Map.entry("activeProfiles", Arrays.asList(environment.getActiveProfiles())),
                Map.entry("banner", banner.describe()),
                Map.entry("reportTimezone", props.report().timezone()),
                Map.entry("reportRetention", props.report().retention().toString()),
                Map.entry("reportIncludeCharts", props.report().includeCharts())
        );
    }
}

