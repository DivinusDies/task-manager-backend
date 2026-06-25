package cz.cvut.fel.myprojects.taskmanager.task.dto;

import cz.cvut.fel.myprojects.taskmanager.task.Task;
import cz.cvut.fel.myprojects.taskmanager.task.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime deadline,
        Long projectId,
        Long assigneeId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDeadline(),
                task.getProject().getId(),
                task.getAssignee().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}