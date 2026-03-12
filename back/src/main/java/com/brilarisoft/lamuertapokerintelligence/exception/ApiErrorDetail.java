package com.brilarisoft.lamuertapokerintelligence.exception;

public record ApiErrorDetail(
        String field,
        String message
) {
}
