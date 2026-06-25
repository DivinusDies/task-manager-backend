package cz.cvut.fel.myprojects.taskmanager.user.dto;


import cz.cvut.fel.myprojects.taskmanager.user.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(

        @NotNull(message = "Role is required")
        Role role
) {
}