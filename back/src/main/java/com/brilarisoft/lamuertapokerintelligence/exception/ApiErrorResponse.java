package com.brilarisoft.lamuertapokerintelligence.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String method,
        List<ApiErrorDetail> details
) {
}
