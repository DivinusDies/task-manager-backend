package cz.cvut.fel.myprojects.taskmanager.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cz.cvut.fel.myprojects.taskmanager.exception.NotFoundException;
import cz.cvut.fel.myprojects.taskmanager.project.dto.CreateProjectRequest;
import cz.cvut.fel.myprojects.taskmanager.project.dto.ProjectResponse;
import cz.cvut.fel.myprojects.taskmanager.project.dto.UpdateProjectRequest;
import cz.cvut.fel.myprojects.taskmanager.user.Role;
import cz.cvut.fel.myprojects.taskmanager.user.User;
import cz.cvut.fel.myprojects.taskmanager.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_ShouldCreateProjectForCurrentUser() {
        String email = "test@test.com";

        User owner = User.builder()
                .id(1L)
                .email(email)
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        CreateProjectRequest request = new CreateProjectRequest(
                "Study project",
                "My first project"
        );

        Project savedProject = Project.builder()
                .id(10L)
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(owner));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        ProjectResponse response = projectService.createProject(email, request);

        assertEquals(10L, response.id());
        assertEquals("Study project", response.name());
        assertEquals("My first project", response.description());
        assertEquals(1L, response.ownerId());

        verify(userRepository).findByEmail(email);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_WhenUserNotFound_ShouldThrowNotFoundException() {
        String email = "missing@test.com";

        CreateProjectRequest request = new CreateProjectRequest(
                "Project",
                "Description"
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> projectService.createProject(email, request)
        );

        verify(userRepository).findByEmail(email);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void getMyProjectById_WhenProjectExists_ShouldReturnProject() {
        String email = "test@test.com";
        Long projectId = 1L;

        User owner = User.builder()
                .id(1L)
                .email(email)
                .role(Role.USER)
                .build();

        Project project = Project.builder()
                .id(projectId)
                .name("Project")
                .description("Description")
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        when(projectRepository.findByIdAndOwnerEmail(projectId, email))
                .thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getMyProjectById(projectId, email);

        assertEquals(projectId, response.id());
        assertEquals("Project", response.name());
        assertEquals(1L, response.ownerId());

        verify(projectRepository).findByIdAndOwnerEmail(projectId, email);
    }

    @Test
    void getMyProjectById_WhenProjectNotFound_ShouldThrowNotFoundException() {
        String email = "test@test.com";
        Long projectId = 999L;

        when(projectRepository.findByIdAndOwnerEmail(projectId, email))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> projectService.getMyProjectById(projectId, email)
        );

        verify(projectRepository).findByIdAndOwnerEmail(projectId, email);
    }

    @Test
    void updateProject_ShouldUpdateProject() {
        String email = "test@test.com";
        Long projectId = 1L;

        User owner = User.builder()
                .id(1L)
                .email(email)
                .role(Role.USER)
                .build();

        Project project = Project.builder()
                .id(projectId)
                .name("Old name")
                .description("Old description")
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        UpdateProjectRequest request = new UpdateProjectRequest(
                "New name",
                "New description"
        );

        when(projectRepository.findByIdAndOwnerEmail(projectId, email))
                .thenReturn(Optional.of(project));

        ProjectResponse response = projectService.updateProject(projectId, email, request);

        assertEquals(1L, response.id());
        
        assertEquals("New name", response.name());
        assertEquals("New description", response.description());

        assertEquals("New name", project.getName());
        assertEquals("New description", project.getDescription());

        verify(projectRepository).findByIdAndOwnerEmail(projectId, email);
    }

    @Test
    void deleteProject_ShouldDeleteProject() {
        String email = "test@test.com";
        Long projectId = 1L;

        User owner = User.builder()
                .id(1L)
                .email(email)
                .role(Role.USER)
                .build();

        Project project = Project.builder()
                .id(projectId)
                .name("Project")
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        when(projectRepository.findByIdAndOwnerEmail(projectId, email))
                .thenReturn(Optional.of(project));

        projectService.deleteProject(projectId, email);

        verify(projectRepository).findByIdAndOwnerEmail(projectId, email);
        verify(projectRepository).delete(project);
    }
}
