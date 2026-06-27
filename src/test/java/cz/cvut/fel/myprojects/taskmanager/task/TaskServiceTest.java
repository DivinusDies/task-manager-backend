package cz.cvut.fel.myprojects.taskmanager.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cz.cvut.fel.myprojects.taskmanager.exception.NotFoundException;
import cz.cvut.fel.myprojects.taskmanager.project.Project;
import cz.cvut.fel.myprojects.taskmanager.project.ProjectRepository;
import cz.cvut.fel.myprojects.taskmanager.task.dto.CreateTaskRequest;
import cz.cvut.fel.myprojects.taskmanager.task.dto.TaskResponse;
import cz.cvut.fel.myprojects.taskmanager.task.dto.UpdateTaskRequest;
import cz.cvut.fel.myprojects.taskmanager.task.dto.UpdateTaskStatusRequest;
import cz.cvut.fel.myprojects.taskmanager.user.Role;
import cz.cvut.fel.myprojects.taskmanager.user.User;
import cz.cvut.fel.myprojects.taskmanager.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void getMyTasks_ShouldReturnTasksForCurrentUser() {
        String email = "test@test.com";
        LocalDateTime now = LocalDateTime.now();

        User user = createUser(email);
        Project project = createProject(user);

        Task task1 = Task.builder()
                .id(1L)
                .title("Task 1")
                .description("Description 1")
                .status(TaskStatus.TODO)
                .deadline(now.plusDays(1))
                .project(project)
                .assignee(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Task task2 = Task.builder()
                .id(2L)
                .title("Task 2")
                .description("Description 2")
                .status(TaskStatus.IN_PROGRESS)
                .deadline(now.plusDays(2))
                .project(project)
                .assignee(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(taskRepository.findAllByProjectOwnerEmail(email))
                .thenReturn(List.of(task1, task2));

        List<TaskResponse> response = taskService.getMyTasks(email);

        assertEquals(2, response.size());
        assertEquals("Task 1", response.get(0).title());
        assertEquals("Task 2", response.get(1).title());

        verify(taskRepository).findAllByProjectOwnerEmail(email);
    }

    @Test
    void getTasksByProject_ShouldReturnTasksFromProject() {
        String email = "test@test.com";
        Long projectId = 10L;
        LocalDateTime now = LocalDateTime.now();

        User user = createUser(email);
        Project project = createProject(user);

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .description("Description")
                .status(TaskStatus.TODO)
                .project(project)
                .assignee(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(taskRepository.findAllByProjectIdAndProjectOwnerEmail(projectId, email))
                .thenReturn(List.of(task));

        List<TaskResponse> response = taskService.getTasksByProject(projectId, email);

        assertEquals(1, response.size());
        assertEquals("Task", response.get(0).title());
        assertEquals(project.getId(), response.get(0).projectId());

        verify(taskRepository).findAllByProjectIdAndProjectOwnerEmail(projectId, email);
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        String email = "test@test.com";
        Long taskId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User user = createUser(email);
        Project project = createProject(user);

        Task task = Task.builder()
                .id(taskId)
                .title("Task")
                .description("Description")
                .status(TaskStatus.TODO)
                .project(project)
                .assignee(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(taskRepository.findByIdAndProjectOwnerEmail(taskId, email))
                .thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(taskId, email);

        assertEquals(taskId, response.id());
        assertEquals("Task", response.title());
        assertEquals(TaskStatus.TODO, response.status());

        verify(taskRepository).findByIdAndProjectOwnerEmail(taskId, email);
    }

    @Test
    void getTaskById_WhenTaskNotFound_ShouldThrowNotFoundException() {
        String email = "test@test.com";
        Long taskId = 999L;

        when(taskRepository.findByIdAndProjectOwnerEmail(taskId, email))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.getTaskById(taskId, email)
        );

        verify(taskRepository).findByIdAndProjectOwnerEmail(taskId, email);
    }

    @Test
    void createTask_ShouldCreateTaskInCurrentUsersProject() {
        String email = "test@test.com";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(3);

        User user = createUser(email);
        Project project = createProject(user);

        CreateTaskRequest request = new CreateTaskRequest(
                "Learn tests",
                "Write unit tests for service",
                deadline,
                project.getId()
        );

        Task savedTask = Task.builder()
                .id(100L)
                .title(request.title())
                .description(request.description())
                .status(TaskStatus.TODO)
                .deadline(deadline)
                .project(project)
                .assignee(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndOwnerEmail(project.getId(), email))
                .thenReturn(Optional.of(project));
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(email, request);

        assertEquals(100L, response.id());
        assertEquals("Learn tests", response.title());
        assertEquals("Write unit tests for service", response.description());
        assertEquals(TaskStatus.TODO, response.status());
        assertEquals(project.getId(), response.projectId());
        assertEquals(user.getId(), response.assigneeId());

        verify(userRepository).findByEmail(email);
        verify(projectRepository).findByIdAndOwnerEmail(project.getId(), email);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WhenUserNotFound_ShouldThrowNotFoundException() {
        String email = "missing@test.com";

        CreateTaskRequest request = new CreateTaskRequest(
                "Task",
                "Description",
                null,
                10L
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.createTask(email, request)
        );

        verify(userRepository).findByEmail(email);
        verify(projectRepository, never()).findByIdAndOwnerEmail(anyLong(), anyString());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_WhenProjectNotFound_ShouldThrowNotFoundException() {
        String email = "test@test.com";
        Long projectId = 999L;

        User user = createUser(email);

        CreateTaskRequest request = new CreateTaskRequest(
                "Task",
                "Description",
                null,
                projectId
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(projectRepository.findByIdAndOwnerEmail(projectId, email))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> taskService.createTask(email, request)
        );

        verify(userRepository).findByEmail(email);
        verify(projectRepository).findByIdAndOwnerEmail(projectId, email);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldUpdateTask() {
        String email = "test@test.com";
        Long taskId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newDeadline = now.plusDays(5);

        User user = createUser(email);
        Project project = createProject(user);

        Task task = Task.builder()
                .id(taskId)
                .title("Old title")
                .description("Old description")
                .status(TaskStatus.TODO)
                .deadline(now.plusDays(1))
                .project(project)
                .assignee(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UpdateTaskRequest request = new UpdateTaskRequest(
                "New title",
                "New description",
                TaskStatus.IN_PROGRESS,
                null,
                newDeadline
        );

        when(taskRepository.findByIdAndProjectOwnerEmail(taskId, email))
                .thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse response = taskService.updateTask(taskId, email, request);

        assertEquals("New title", response.title());
        assertEquals("New description", response.description());
        assertEquals(TaskStatus.IN_PROGRESS, response.status());
        assertEquals(newDeadline, response.deadline());

        verify(taskRepository).findByIdAndProjectOwnerEmail(taskId, email);
        verify(taskRepository).save(task);
    }

    @Test
    void updateTaskStatus_ShouldUpdateOnlyStatus() {
        String email = "test@test.com";
        Long taskId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User user = createUser(email);
        Project project = createProject(user);

        Task task = Task.builder()
                .id(taskId)
                .title("Task")
                .description("Description")
                .status(TaskStatus.TODO)
                .project(project)
                .assignee(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.DONE);

        when(taskRepository.findByIdAndProjectOwnerEmail(taskId, email))
                .thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse response = taskService.updateTaskStatus(taskId, email, request);

        assertEquals(TaskStatus.DONE, response.status());
        assertEquals("Task", response.title());

        verify(taskRepository).findByIdAndProjectOwnerEmail(taskId, email);
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTask_ShouldDeleteTask() {
        String email = "test@test.com";
        Long taskId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User user = createUser(email);
        Project project = createProject(user);

        Task task = Task.builder()
                .id(taskId)
                .title("Task")
                .description("Description")
                .status(TaskStatus.TODO)
                .project(project)
                .assignee(user)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(taskRepository.findByIdAndProjectOwnerEmail(taskId, email))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(taskId, email);

        verify(taskRepository).findByIdAndProjectOwnerEmail(taskId, email);
        verify(taskRepository).delete(task);
    }

    private User createUser(String email) {
        return User.builder()
                .id(1L)
                .email(email)
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Project createProject(User owner) {
        return Project.builder()
                .id(10L)
                .name("Project")
                .description("Project description")
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();
    }
}