package com.snow.solution.validation;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class BpmnValidationService {

    private static final Pattern REQ_ID_PATTERN = Pattern.compile("^[A-Z]{2,3}-\\d{3,6}$"); // Example: REQ-123

    public ValidationResponse validate(InputStream bpmnStream) {
        List<ValidationError> errors = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(bpmnStream);

            // We validate UserTasks and ServiceTasks
            String[] taskTypes = {"bpmn:UserTask", "bpmn:ServiceTask"};
            for (String type : taskTypes) {
                NodeList tasks = doc.getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", type);
                for (int i = 0; i < tasks.getLength(); i++) {
                    Element task = (Element) tasks.item(i);
                    validateTask(task, errors);
                }
            }

        } catch (Exception e) {
            return new ValidationResponse(false, "Ошибка при парсинге XML: " + e.getMessage(), errors);
        }

        boolean isValid = errors.isEmpty();
        return new ValidationResponse(isValid, isValid ? "Схема валидна" : "Обнаружены ошибки валидации", errors);
    }

    private void validateTask(Element task, List<ValidationError> errors) {
        String taskId = task.getAttribute("id");
        if (taskId == null || taskId.isEmpty()) {
            taskId = "unknown";
        }

        Map<String, String> properties = extractProperties(task);

        // 1. Target Description Block
        checkRequiredField(properties, "req_id", taskId, errors);
        checkRequiredField(properties, "feature_name", taskId, errors);
        checkRequiredField(properties, "target_description", taskId, errors);

        // Validate req_id format
        String reqId = properties.get("req_id");
        if (reqId != null && !REQ_ID_PATTERN.matcher(reqId).matches()) {
            errors.add(new ValidationError(taskId, "req_id", "Неверный формат идентификатора. Ожидается паттерн типа REQ-123"));
        }

        // 2. Functional Requirements Block (fr_*)
        if (!hasPropertyPrefix(properties, "fr_")) {
            errors.add(new ValidationError(taskId, "fr_*", "Отсутствуют функциональные требования (поля с префиксом fr_)"));
        }

        // 3. Non-Functional Requirements Block (nfr_*)
        if (!hasPropertyPrefix(properties, "nfr_")) {
            errors.add(new ValidationError(taskId, "nfr_*", "Отсутствуют нефункциональные требования (поля с префиксом nfr_)"));
        }
    }

    private Map<String, String> extractProperties(Element task) {
        Map<String, String> properties = new HashMap<>();
        NodeList extensionElements = task.getElementsByTagNameNS("http://www.omg.org/spec/BPMN/20100524/MODEL", "bpmn:extensionElements");

        if (extensionElements.getLength() > 0) {
            Element extElem = (Element) extensionElements.item(0);
            // Support both camunda:property and generic properties
            NodeList props = extElem.getElementsByTagNameNS("*", "property");
            for (int i = 0; i < props.getLength(); i++) {
                Element prop = (Element) props.item(i);
                String name = prop.getAttribute("name");
                String value = prop.getAttribute("value");
                if (name != null && !name.isEmpty()) {
                    properties.put(name, value);
                }
            }
        }
        return properties;
    }

    private void checkRequiredField(Map<String, String> props, String field, String taskId, List<ValidationError> errors) {
        if (!props.containsKey(field) || props.get(field) == null || props.get(field).trim().isEmpty()) {
            errors.add(new ValidationError(taskId, field, "Обязательное поле отсутствует или пусто"));
        }
    }

    private boolean hasPropertyPrefix(Map<String, String> props, String prefix) {
        return props.keySet().stream().anyMatch(k -> k.startsWith(prefix));
    }
}
