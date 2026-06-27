package cz.cvut.fel.myprojects.taskmanager.task.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

import cz.cvut.fel.myprojects.taskmanager.task.TaskStatus;

public record UpdateTaskRequest(

        @Size(max = 150, message = "Task title must be less than 150 characters")
        String title,

        @Size(max = 2000, message = "Description must be less than 2000 characters")
        String description,

        @NotNull
        TaskStatus status,

        Long projectId,

        @FutureOrPresent(message = "Deadline must be today or in the future")
        LocalDateTime deadline
) {
}