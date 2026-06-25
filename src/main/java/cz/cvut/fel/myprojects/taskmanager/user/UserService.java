package cz.cvut.fel.myprojects.taskmanager.user;

import cz.cvut.fel.myprojects.taskmanager.exception.NotFoundException;
import cz.cvut.fel.myprojects.taskmanager.user.dto.ChangePasswordRequest;
import cz.cvut.fel.myprojects.taskmanager.user.dto.UpdateProfileRequest;
import cz.cvut.fel.myprojects.taskmanager.user.dto.UpdateUserRoleRequest;
import cz.cvut.fel.myprojects.taskmanager.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getCurrentUser(String email) {
        User user = getUserByEmail(email);
        return UserResponse.from(user);
    }

    public UserResponse updateCurrentUser(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = getUserEntityById(id);
        return UserResponse.from(user);
    }

    public UserResponse updateUserRole(Long id, UpdateUserRoleRequest request) {
        User user = getUserEntityById(id);

        user.setRole(request.role());

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    public void deleteUser(Long id) {
        User user = getUserEntityById(id);
        userRepository.delete(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }
}