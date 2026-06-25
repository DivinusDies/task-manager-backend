package cz.cvut.fel.myprojects.taskmanager.project;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cz.cvut.fel.myprojects.taskmanager.auth.JwtService;
import cz.cvut.fel.myprojects.taskmanager.project.dto.ProjectResponse;
import cz.cvut.fel.myprojects.taskmanager.user.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturnProject() throws Exception {
        // String token = "fake-jwt-token";

        // when(jwtService.extractEmail(token))
        //         .thenReturn("test@test.com");

        // when(jwtService.isTokenValid(eq(token), any()))
        //         .thenReturn(true);
        when(projectService.getMyProjectById(1L, "test@test.com"
        ))
                .thenReturn(new ProjectResponse(
                        1L,
                        "test",
                        "description",
                        1L,
                        LocalDateTime.now()
                ));

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test"))
                .andExpect(jsonPath("$.description").value("description"));
    }
}