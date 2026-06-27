package cz.cvut.fel.myprojects.taskmanager.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import cz.cvut.fel.myprojects.taskmanager.exception.NotFoundException;
import cz.cvut.fel.myprojects.taskmanager.user.dto.ChangePasswordRequest;
import cz.cvut.fel.myprojects.taskmanager.user.dto.UpdateProfileRequest;
import cz.cvut.fel.myprojects.taskmanager.user.dto.UpdateUserRoleRequest;
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

    @Test
    void updateCurrentUser_ShouldUpdateFirstNameAndLastName() {
        String email = "test@test.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password("encodedPassword")
                .firstName("Old")
                .lastName("Name")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        UpdateProfileRequest request = new UpdateProfileRequest(
                "New",
                "User"
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateCurrentUser(email, request);

        assertEquals("New", response.firstName());
        assertEquals("User", response.lastName());

        verify(userRepository).findByEmail(email);
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_WhenOldPasswordIsCorrect_ShouldChangePassword() {
        String email = "test@test.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password("encodedOldPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword",
                "newPassword"
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        userService.changePassword(email, request);

        assertEquals("encodedNewPassword", user.getPassword());

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_WhenOldPasswordIsIncorrect_ShouldThrowIllegalArgumentException() {
        String email = "test@test.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password("encodedOldPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        ChangePasswordRequest request = new ChangePasswordRequest(
                "wrongPassword",
                "newPassword"
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.changePassword(email, request)
        );

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches("wrongPassword", "encodedOldPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

     @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = User.builder()
                .id(1L)
                .email("user1@test.com")
                .firstName("User")
                .lastName("One")
                .role(Role.USER)
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserResponse> response = userService.getAllUsers();

        assertEquals(2, response.size());
        assertEquals("user1@test.com", response.get(0).email());
        assertEquals("admin@test.com", response.get(1).email());

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId);

        assertEquals(userId, response.id());
        assertEquals("test@test.com", response.email());

        verify(userRepository).findById(userId);
    }

    @Test
    void updateUserRole_ShouldUpdateRole() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();

        UpdateUserRoleRequest request = new UpdateUserRoleRequest(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateUserRole(userId, request);

        assertEquals(Role.ADMIN, response.role());

        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .email("test@test.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }
}