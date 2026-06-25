package cz.cvut.fel.myprojects.taskmanager.project;

import cz.cvut.fel.myprojects.taskmanager.exception.NotFoundException;
import cz.cvut.fel.myprojects.taskmanager.project.dto.CreateProjectRequest;
import cz.cvut.fel.myprojects.taskmanager.project.dto.ProjectResponse;
import cz.cvut.fel.myprojects.taskmanager.project.dto.UpdateProjectRequest;
import cz.cvut.fel.myprojects.taskmanager.user.User;
import cz.cvut.fel.myprojects.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<ProjectResponse> getMyProjects(String email) {
        return projectRepository.findAllByOwnerEmail(email)
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    
    public ProjectResponse getMyProjectById(Long id, String email) {
        Project project = getProjectForOwner(id, email);
        return ProjectResponse.from(project);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ProjectResponse createProject(String email, CreateProjectRequest request) {
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .build();

        Project savedProject = projectRepository.save(project);
        return ProjectResponse.from(savedProject);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ProjectResponse updateProject(Long id, String email, UpdateProjectRequest request) {
        Project project = getProjectForOwner(id, email);

        project.setName(request.name());
        project.setDescription(request.description());

        return ProjectResponse.from(project);
    }

    @Transactional
    public void deleteProject(Long id, String email) {
        Project project = getProjectForOwner(id, email);
        projectRepository.delete(project);
    }

    private Project getProjectForOwner(Long id, String email) {
        return projectRepository.findByIdAndOwnerEmail(id, email)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + id));
    }
}