package cz.cvut.fel.myprojects.taskmanager.task;

import cz.cvut.fel.myprojects.taskmanager.task.dto.CreateTaskRequest;
import cz.cvut.fel.myprojects.taskmanager.task.dto.TaskResponse;
import cz.cvut.fel.myprojects.taskmanager.task.dto.UpdateTaskRequest;
import cz.cvut.fel.myprojects.taskmanager.task.dto.UpdateTaskStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<TaskResponse> getMyTasks(Authentication authentication) {
        return taskService.getMyTasks(authentication.getName());
    }

    @GetMapping("/project/{projectId}")
    public List<TaskResponse> getTasksByProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        return taskService.getTasksByProject(projectId, authentication.getName());
    }

    @GetMapping("/{taskId}")
    public TaskResponse getTaskById(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        return taskService.getTaskById(taskId, authentication.getName());
    }

    @PostMapping
    public TaskResponse createTask(
            Authentication authentication,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return taskService.createTask(authentication.getName(), request);
    }

    @PutMapping("/{taskId}")
    public TaskResponse updateTask(
            @PathVariable Long taskId,
            Authentication authentication,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.updateTask(taskId, authentication.getName(), request);
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateTaskStatus(
            @PathVariable Long taskId,
            Authentication authentication,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return taskService.updateTaskStatus(taskId, authentication.getName(), request);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(
            @PathVariable Long taskId,
            Authentication authentication
    ) {
        taskService.deleteTask(taskId, authentication.getName());
    }
}