package runtracker.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import runtracker.backend.model.Training;

public interface TrainingRepository extends JpaRepository<Training, Long> {
}