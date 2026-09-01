package runtracker.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import runtracker.backend.dto.TrainingStats;
import runtracker.backend.model.Training;
import runtracker.backend.repository.TrainingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public Training createTraining(Training training) {
        training.setPace(training.getDurationMinutes() / training.getDistanceKm());
        return trainingRepository.save(training);
    }

    public Training getTrainingById(long id) {
        return trainingRepository.findById(id).orElse(null);
    }

    public Page<Training> getTrainingsByPage(int page, int size, String sort) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be 0 or greater"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100"
            );
        }

        String[] sortParams = sort.split(",",-1);

        if (sortParams.length != 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sort must have format property,direction"
            );
        }

        String property = sortParams[0];
        String direction = sortParams[1];

        Set<String> validProperties = Set.of(
                "date",
                "distanceKm",
                "durationMinutes",
                "averageHeartRate",
                "pace"
        );

        if (!validProperties.contains(property)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter " + property + " is not valid");
        }

        Sort.Direction sortDirection;

        if (direction.equalsIgnoreCase("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (direction.equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid direction");
        }

        Sort sortObject = Sort.by(sortDirection, property);

        return trainingRepository.findAll(PageRequest.of(page, size, sortObject));
    }

    public void deleteTrainingById(long id) {
        trainingRepository.deleteById(id);
    }

    public Training updateTraining(long id, Training training) {
        Training existingTraining = trainingRepository.findById(id).orElse(null);

        if (existingTraining == null) {
            return null;
        }

        existingTraining.setDate(training.getDate());
        existingTraining.setDistanceKm(training.getDistanceKm());
        existingTraining.setDurationMinutes(training.getDurationMinutes());
        existingTraining.setAverageHeartRate(training.getAverageHeartRate());

        existingTraining.setPace(existingTraining.getDurationMinutes() / existingTraining.getDistanceKm());

        return trainingRepository.save(existingTraining);
    }

    public List<Training> getTrainingsByMinDistance(double minDistance) {
        return trainingRepository.findByDistanceKmGreaterThanEqual(minDistance);
    }

    public TrainingStats getStats() {
        List<Training> trainings = trainingRepository.findAll();
        TrainingStats trainingStats = new TrainingStats();

        trainingStats.setTotalTrainings(trainings.size());

        if (trainings.isEmpty()) {
            trainingStats.setTotalDistance(0);
            trainingStats.setAveragePace(0);
            trainingStats.setAverageHeartRate(0);
        } else {

            double totalDistance = 0;
            double averagePace;
            double totalHeartRate = 0;
            double totalMinutes = 0;

            for (Training training : trainings) {
                totalDistance += training.getDistanceKm();
                totalHeartRate += training.getAverageHeartRate();
                totalMinutes += training.getDurationMinutes();
            }

            averagePace = totalMinutes / totalDistance;
            totalHeartRate /= trainings.size();

            trainingStats.setTotalDistance(totalDistance);
            trainingStats.setAveragePace(averagePace);
            trainingStats.setAverageHeartRate(totalHeartRate);
        }

        return trainingStats;
    }

    public List<Training> getTrainingsBetweenDates(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must not be after to");
        }
        return trainingRepository.findByDateBetweenOrderByDateAsc(from, to);
    }

}
