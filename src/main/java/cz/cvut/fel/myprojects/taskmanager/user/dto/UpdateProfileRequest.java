package cz.cvut.fel.myprojects.taskmanager.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @Size(max = 50, message = "First name must be less than 50 characters")
        String firstName,

        @Size(max = 50, message = "Last name must be less than 50 characters")
        String lastName
) {
}