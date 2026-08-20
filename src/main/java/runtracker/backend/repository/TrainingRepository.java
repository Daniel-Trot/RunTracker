package runtracker.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import runtracker.backend.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    List<Training> findByDistanceKmGreaterThanEqual(double minDistanceKm);
    List<Training> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}