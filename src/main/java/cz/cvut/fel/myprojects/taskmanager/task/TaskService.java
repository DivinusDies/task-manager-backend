package cz.cvut.fel.myprojects.taskmanager.task;

import cz.cvut.fel.myprojects.taskmanager.exception.NotFoundException;
import cz.cvut.fel.myprojects.taskmanager.project.Project;
import cz.cvut.fel.myprojects.taskmanager.project.ProjectRepository;
import cz.cvut.fel.myprojects.taskmanager.task.dto.CreateTaskRequest;
import cz.cvut.fel.myprojects.taskmanager.task.dto.TaskResponse;
import cz.cvut.fel.myprojects.taskmanager.task.dto.UpdateTaskRequest;
import cz.cvut.fel.myprojects.taskmanager.task.dto.UpdateTaskStatusRequest;
import cz.cvut.fel.myprojects.taskmanager.user.User;
import cz.cvut.fel.myprojects.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<TaskResponse> getMyTasks(String email) {
        return taskRepository.findAllByProjectOwnerEmail(email)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public List<TaskResponse> getTasksByProject(Long projectId, String email) {
        return taskRepository.findAllByProjectIdAndProjectOwnerEmail(projectId, email)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse getTaskById(Long taskId, String email) {
        Task task = getTaskForOwner(taskId, email);
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse createTask(String email, CreateTaskRequest request) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Project project = projectRepository.findByIdAndOwnerEmail(request.projectId(), email)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        Task task = Task.builder()
                .title(request.title())
                .description(request.description())
                .deadline(request.deadline())
                .project(project)
                .assignee(currentUser)
                .status(TaskStatus.TODO)
                .build();

        Task savedTask = taskRepository.save(task);
        return TaskResponse.from(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, String email, UpdateTaskRequest request) {
        Task task = getTaskForOwner(taskId, email);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setDeadline(request.deadline());

        Task savedTask = taskRepository.save(task);
        return TaskResponse.from(savedTask);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long taskId, String email, UpdateTaskStatusRequest request) {
        Task task = getTaskForOwner(taskId, email);

        task.setStatus(request.status());

        Task savedTask = taskRepository.save(task);
        return TaskResponse.from(savedTask);
    }

    @Transactional
    public void deleteTask(Long taskId, String email) {
        Task task = getTaskForOwner(taskId, email);
        taskRepository.delete(task);
    }

    private Task getTaskForOwner(Long taskId, String email) {
        return taskRepository.findByIdAndProjectOwnerEmail(taskId, email)
                .orElseThrow(() -> new NotFoundException("Task not found with id: " + taskId));
    }
}