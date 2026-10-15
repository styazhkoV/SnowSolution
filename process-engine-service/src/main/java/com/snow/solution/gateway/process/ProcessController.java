package com.snow.solution.gateway.process;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/processes")
public class ProcessController {

    private static final Logger log = LoggerFactory.getLogger(ProcessController.class);

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    public ProcessController(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    // 1. Запуск нового бизнес-процесса с логированием и перехватом ошибок
    @PostMapping("/start")
    public ResponseEntity<String> startProcess(@RequestParam("processKey") String processKey) {
        try {
            log.info("Attempting to start process with key: {}", processKey);
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey);
            return ResponseEntity.ok("Process started with ID: " + processInstance.getId());
        } catch (Exception e) {
            log.error("CRITICAL: Failed to start process with key '{}'", processKey, e);
            return ResponseEntity.status(500).body("Error starting process: " + e.getMessage());
        }
    }

    // 2. Получение списка активных задач
    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, String>>> getTasks() {
        List<Task> tasks = taskService.createTaskQuery().list();
        List<Map<String, String>> response = tasks.stream().map(task -> Map.of(
                "taskId", task.getId(),
                "taskName", task.getName() != null ? task.getName() : "",
                "assignee", task.getAssignee() != null ? task.getAssignee() : "Unassigned",
                "processInstanceId", task.getProcessInstanceId()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 3. Завершение (выполнение) задачи
    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<String> completeTask(@PathVariable("taskId") String taskId) {
        taskService.complete(taskId);
        return ResponseEntity.ok("Task " + taskId + " completed successfully.");
    }
}