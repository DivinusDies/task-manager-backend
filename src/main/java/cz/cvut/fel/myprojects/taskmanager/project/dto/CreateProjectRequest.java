package cz.cvut.fel.myprojects.taskmanager.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Project name must be less than 100 characters")
        String name,

        @Size(max = 1000, message = "Description must be less than 1000 characters")
        String description
) {
}