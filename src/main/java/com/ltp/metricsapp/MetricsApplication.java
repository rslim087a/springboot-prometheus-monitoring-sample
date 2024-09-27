package com.ltp.metricsapp;

import io.micrometer.core.instrument.*;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@RestController
public class MetricsApplication implements WebMvcConfigurer {

    private final Map<Integer, String> items = new HashMap<>();
    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public MetricsApplication(PrometheusMeterRegistry prometheusMeterRegistry) {
        this.prometheusMeterRegistry = prometheusMeterRegistry;
    }

    public static void main(String[] args) {
        SpringApplication.run(MetricsApplication.class, args);
    }

    @Bean
    public MeterRegistry meterRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MetricsInterceptor(prometheusMeterRegistry));
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of("message", "Hello World"));
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> createItem(@RequestBody Item item) {
        int itemId = items.size() + 1;
        items.put(itemId, item.getName());
        return ResponseEntity.ok(Map.of(
                "item_id", itemId,
                "name", item.getName(),
                "status", "created"
        ));
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> readItem(@PathVariable int itemId) {
        if (!items.containsKey(itemId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", "Item not found"));
        }
        return ResponseEntity.ok(Map.of(
                "item_id", itemId,
                "name", items.get(itemId)
        ));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> updateItem(@PathVariable int itemId, @RequestBody Item item) {
        if (!items.containsKey(itemId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", "Item not found"));
        }
        items.put(itemId, item.getName());
        return ResponseEntity.ok(Map.of(
                "item_id", itemId,
                "name", item.getName(),
                "status", "updated"
        ));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> deleteItem(@PathVariable int itemId) {
        if (!items.containsKey(itemId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("detail", "Item not found"));
        }
        items.remove(itemId);
        return ResponseEntity.ok(Map.of(
                "item_id", itemId,
                "status", "deleted"
        ));
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> metrics() {
        return ResponseEntity.ok().body(prometheusMeterRegistry.scrape());
    }

    static class Item {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static class MetricsInterceptor implements HandlerInterceptor {
        private final MeterRegistry meterRegistry;
        private final AtomicInteger inProgressRequests;

        public MetricsInterceptor(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            this.inProgressRequests = new AtomicInteger(0);
            meterRegistry.gauge("http_requests_in_progress", Tags.empty(), inProgressRequests);
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            inProgressRequests.incrementAndGet();
            request.setAttribute("startTime", System.nanoTime());
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            inProgressRequests.decrementAndGet();
            long startTime = (long) request.getAttribute("startTime");
            long duration = System.nanoTime() - startTime;

            String method = request.getMethod();
            String path = request.getRequestURI();
            String status = String.valueOf(response.getStatus());

            // Use the variables to record more detailed metrics
            Tags tags = Tags.of(
                "method", method,
                "path", path,
                "status", status
            );

            meterRegistry.counter("http_request_total", tags).increment();
            meterRegistry.timer("http_request_duration_seconds", tags).record(duration, TimeUnit.NANOSECONDS);
        }
    }

}