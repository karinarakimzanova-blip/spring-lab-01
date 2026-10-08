package kz.iitu.springlab.web;

import kz.iitu.springlab.aspect.MeasuredAspect;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/lab4")
public class MetricsController {

    private final MeasuredAspect measuredAspect;

    public MetricsController(MeasuredAspect measuredAspect) {
        this.measuredAspect = measuredAspect;
    }

    @GetMapping("/metrics")
    public Map<String, MeasuredAspect.MetricSummary> metrics() {
        return measuredAspect.summary();
    }

    @DeleteMapping("/metrics")
    public String reset() {
        measuredAspect.reset();
        return "metrics reset";
    }
}
