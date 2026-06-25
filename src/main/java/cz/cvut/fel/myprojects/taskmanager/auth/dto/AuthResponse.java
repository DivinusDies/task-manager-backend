package cz.cvut.fel.myprojects.taskmanager.auth.dto;

import cz.cvut.fel.myprojects.taskmanager.user.dto.UserResponse;

public record AuthResponse(
        String token,
        UserResponse user
) {
}