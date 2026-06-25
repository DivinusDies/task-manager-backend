package cz.cvut.fel.myprojects.taskmanager.auth;

import cz.cvut.fel.myprojects.taskmanager.auth.dto.AuthResponse;
import cz.cvut.fel.myprojects.taskmanager.auth.dto.LoginRequest;
import cz.cvut.fel.myprojects.taskmanager.auth.dto.RegisterRequest;
import cz.cvut.fel.myprojects.taskmanager.user.Role;
import cz.cvut.fel.myprojects.taskmanager.user.User;
import cz.cvut.fel.myprojects.taskmanager.user.UserRepository;
import cz.cvut.fel.myprojects.taskmanager.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(token, UserResponse.from(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, UserResponse.from(user));
    }
}