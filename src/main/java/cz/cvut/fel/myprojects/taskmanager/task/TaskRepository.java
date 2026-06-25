package cz.cvut.fel.myprojects.taskmanager.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByProjectOwnerEmail(String email);

    List<Task> findAllByProjectIdAndProjectOwnerEmail(Long projectId, String email);

    Optional<Task> findByIdAndProjectOwnerEmail(Long taskId, String email);
}