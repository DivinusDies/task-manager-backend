package cz.cvut.fel.myprojects.taskmanager.project;

import cz.cvut.fel.myprojects.taskmanager.project.dto.CreateProjectRequest;
import cz.cvut.fel.myprojects.taskmanager.project.dto.ProjectResponse;
import cz.cvut.fel.myprojects.taskmanager.project.dto.UpdateProjectRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectResponse> getMyProjects(Authentication authentication) {
        return projectService.getMyProjects(authentication.getName());
    }

    @GetMapping("/{id}")
    public ProjectResponse getMyProjectById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return projectService.getMyProjectById(id, authentication.getName());
    }

    @PostMapping
    public ProjectResponse createProject(
            Authentication authentication,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        return projectService.createProject(authentication.getName(), request);
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        return projectService.updateProject(id, authentication.getName(), request);
    }

    @DeleteMapping("/{id}")
    public void deleteProject(
            @PathVariable Long id,
            Authentication authentication
    ) {
        projectService.deleteProject(id, authentication.getName());
    }
}