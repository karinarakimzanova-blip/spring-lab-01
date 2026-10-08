package kz.iitu.springlab.aspect;

import kz.iitu.springlab.metrics.Measured;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Aspect
@Component
@Order(4)
public class MeasuredAspect {

    public record MetricSummary(long calls, long failures, double totalMs, double avgMs) { }

    private static class Stats {
        final LongAdder calls = new LongAdder();
        final LongAdder failures = new LongAdder();
        final LongAdder totalNanos = new LongAdder();
    }

    private final ConcurrentHashMap<String, Stats> stats = new ConcurrentHashMap<>();

    @Around("@annotation(measured)")
    public Object measure(ProceedingJoinPoint pjp, Measured measured) throws Throwable {
        String key = measured.name().isBlank()
                ? pjp.getSignature().getDeclaringType().getSimpleName()
                + "." + pjp.getSignature().getName()
                : measured.name();

        long started = System.nanoTime();
        boolean ok = false;
        try {
            Object result = pjp.proceed();
            ok = true;
            return result;
        } finally {
            long elapsed = System.nanoTime() - started;
            Stats s = stats.computeIfAbsent(key, k -> new Stats());
            s.calls.increment();
            s.totalNanos.add(elapsed);
            if (!ok) s.failures.increment();
        }
    }

    public Map<String, MetricSummary> summary() {
        Map<String, MetricSummary> result = new TreeMap<>();
        stats.forEach((key, s) -> {
            long calls = s.calls.sum();
            double totalMs = s.totalNanos.sum() / 1_000_000.0;
            double avgMs = calls == 0 ? 0 : totalMs / calls;
            result.put(key, new MetricSummary(calls, s.failures.sum(), round(totalMs), round(avgMs)));
        });
        return result;
    }

    public void reset() {
        stats.clear();
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
