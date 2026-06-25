package cz.cvut.fel.myprojects.taskmanager.task.dto;

import cz.cvut.fel.myprojects.taskmanager.task.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(

        @NotNull(message = "Status is required")
        TaskStatus status
) {
}