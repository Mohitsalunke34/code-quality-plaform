package com.trident.codequality.web;

import com.trident.codequality.dto.dashboard.DashboardMetricsResponse;
import com.trident.codequality.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Aggregated platform metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/metrics")
    @Operation(summary = "Get aggregated dashboard metrics")
    public ResponseEntity<DashboardMetricsResponse> metrics() {
        return ResponseEntity.ok(dashboardService.metrics());
    }
}
