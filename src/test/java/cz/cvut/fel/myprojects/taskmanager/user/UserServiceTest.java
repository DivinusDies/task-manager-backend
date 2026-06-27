package cz.cvut.fel.myprojects.taskmanager.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import cz.cvut.fel.myprojects.taskmanager.exception.NotFoundException;
import cz.cvut.fel.myprojects.taskmanager.user.dto.UserResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUser_WhenUserExists_ShouldReturnUser() {
        String email = "test@test.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserResponse response = userService.getCurrentUser(email);

        assertEquals(1L, response.id());
        assertEquals(email, response.email());
        assertEquals("Test", response.firstName());
        assertEquals("User", response.lastName());
        assertEquals(Role.USER, response.role());

        verify(userRepository).findByEmail(email);
    }

    @Test
    void getCurrentUser_WhenUserNotFound_ShouldThrowNotFoundException() {
        String email = "missing@test.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.getCurrentUser(email)
        );

        verify(userRepository).findByEmail(email);
    }
    
}