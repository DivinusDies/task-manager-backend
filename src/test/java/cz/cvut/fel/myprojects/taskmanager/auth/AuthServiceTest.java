package cz.cvut.fel.myprojects.taskmanager.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import cz.cvut.fel.myprojects.taskmanager.auth.dto.AuthResponse;
import cz.cvut.fel.myprojects.taskmanager.auth.dto.LoginRequest;
import cz.cvut.fel.myprojects.taskmanager.auth.dto.RegisterRequest;
import cz.cvut.fel.myprojects.taskmanager.user.Role;
import cz.cvut.fel.myprojects.taskmanager.user.User;
import cz.cvut.fel.myprojects.taskmanager.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WhenEmailIsFree_ShouldCreateUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "123456",
                "Test",
                "User"
        );

        User savedUser = User.builder()
                .id(1L)
                .email(request.email())
                .password("encodedPassword")
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.token());
        assertEquals(1L, response.user().id());
        assertEquals("test@test.com", response.user().email());
        assertEquals("Test", response.user().firstName());
        assertEquals("User", response.user().lastName());
        assertEquals(Role.USER, response.user().role());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(userCaptor.capture());
        verify(jwtService).generateToken(savedUser);

        User userToSave = userCaptor.getValue();

        assertEquals("test@test.com", userToSave.getEmail());
        assertEquals("encodedPassword", userToSave.getPassword());
        assertEquals("Test", userToSave.getFirstName());
        assertEquals("User", userToSave.getLastName());
        assertEquals(Role.USER, userToSave.getRole());
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest(
                "test@test.com",
                "123456",
                "Test",
                "User"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void login_WhenCredentialsAreValid_ShouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "123456"
        );

        User user = User.builder()
                .id(1L)
                .email(request.email())
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals(1L, response.user().id());
        assertEquals("test@test.com", response.user().email());
        assertEquals(Role.USER, response.user().role());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        verify(userRepository).findByEmail(request.email());
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_WhenUserNotFoundAfterAuthentication_ShouldThrowIllegalArgumentException() {
        LoginRequest request = new LoginRequest(
                "missing@test.com",
                "123456"
        );

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        verify(userRepository).findByEmail(request.email());
        verify(jwtService, never()).generateToken(any(User.class));
    }
}