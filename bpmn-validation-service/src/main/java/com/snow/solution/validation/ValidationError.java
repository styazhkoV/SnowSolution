package com.snow.solution.validation;

public record ValidationError(
    String elementId,
    String fieldName,
    String errorDescription
) {}
