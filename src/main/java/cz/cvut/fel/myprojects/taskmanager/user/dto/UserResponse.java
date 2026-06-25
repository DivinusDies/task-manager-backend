package cz.cvut.fel.myprojects.taskmanager.user.dto;

import cz.cvut.fel.myprojects.taskmanager.user.Role;
import cz.cvut.fel.myprojects.taskmanager.user.User;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}