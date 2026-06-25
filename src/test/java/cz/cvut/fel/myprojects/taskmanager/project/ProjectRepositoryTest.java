package cz.cvut.fel.myprojects.taskmanager.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import cz.cvut.fel.myprojects.taskmanager.user.Role;
import cz.cvut.fel.myprojects.taskmanager.user.User;
import cz.cvut.fel.myprojects.taskmanager.user.UserRepository;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveProject() {
        User user = User.builder()
                .email("test@test.com")
                .firstName("test")
                .lastName("test")
                .password("password")
                .role(Role.USER)
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        Project project = Project.builder()
                .name("project")
                .description("description")
                .owner(savedUser)
                .build();

        Project savedProject = projectRepository.saveAndFlush(project);

        assertNotNull(savedProject.getId());
        assertEquals("project", savedProject.getName());
        assertNotNull(savedProject.getOwner());
        assertEquals(savedUser.getId(), savedProject.getOwner().getId());
    }
}