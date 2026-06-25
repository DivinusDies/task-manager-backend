package cz.cvut.fel.myprojects.taskmanager.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOwnerEmail(String email);

    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Project> findByIdAndOwnerEmail(Long id, String email);
}