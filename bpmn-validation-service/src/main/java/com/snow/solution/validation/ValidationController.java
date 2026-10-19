package com.snow.solution.validation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/validation")
public class ValidationController {

    private final BpmnValidationService validationService;

    public ValidationController(BpmnValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping("/check")
    public ResponseEntity<ValidationResponse> checkBpmn(@RequestParam("file") MultipartFile file) {
        try {
            ValidationResponse response = validationService.validate(file.getInputStream());
            if (response.valid()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                new ValidationResponse(false, "Ошибка при чтении файла: " + e.getMessage(), null)
            );
        }
    }
}
