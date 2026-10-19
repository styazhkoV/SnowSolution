package com.snow.solution.validation;

import java.util.List;

public record ValidationResponse(
    boolean valid,
    String message,
    List<ValidationError> errors
) {}
