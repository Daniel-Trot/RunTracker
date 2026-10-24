package runtracker.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import runtracker.backend.dto.TrainingRequest;
import runtracker.backend.dto.TrainingResponse;
import runtracker.backend.dto.TrainingStats;
import runtracker.backend.exception.InvalidTrainingRequestException;
import runtracker.backend.exception.TrainingNotFoundException;
import runtracker.backend.model.Training;
import runtracker.backend.repository.TrainingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    private static final Set<String> VALID_SORT_PROPERTIES = Set.of(
            "date",
            "distanceKm",
            "durationMinutes",
            "averageHeartRate",
            "pace"
    );

    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public TrainingResponse createTraining(TrainingRequest request) {
        Training training = mapToEntity(request);

        training.setPace(calculatePace(training));

        Training savedTraining = trainingRepository.save(training);

        return mapToResponse(savedTraining);
    }

    private Training mapToEntity(TrainingRequest request) {
        Training training = new Training();
        updateTrainingFromRequest(training, request);
        return training;
    }

    private TrainingResponse mapToResponse(Training training) {
        TrainingResponse trainingResponse = new TrainingResponse();

        trainingResponse.setId(training.getId());
        trainingResponse.setDate(training.getDate());
        trainingResponse.setDistanceKm(training.getDistanceKm());
        trainingResponse.setDurationMinutes(training.getDurationMinutes());
        trainingResponse.setAverageHeartRate(training.getAverageHeartRate());
        trainingResponse.setPace(training.getPace());

        return trainingResponse;
    }

    private void updateTrainingFromRequest(Training training, TrainingRequest request) {
        training.setDate(request.getDate());
        training.setDistanceKm(request.getDistanceKm());
        training.setDurationMinutes(request.getDurationMinutes());
        training.setAverageHeartRate(request.getAverageHeartRate());
    }

    private List<TrainingResponse> mapToResponseList(List<Training> trainings) {
        List<TrainingResponse> responses = new ArrayList<>();

        for (Training training : trainings) {
            responses.add(mapToResponse(training));
        }
        return responses;
    }

    private Training findTrainingOrThrow(long id) {
        return trainingRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException(id));
    }

    private double calculatePace(Training training) {
        return (double) training.getDurationMinutes() / training.getDistanceKm();
    }

    private Sort.Direction parseSortDirection(String direction) {
        if (direction.equalsIgnoreCase("asc")) {
            return Sort.Direction.ASC;
        }

        if (direction.equalsIgnoreCase("desc")) {
            return Sort.Direction.DESC;
        }

        throw new InvalidTrainingRequestException("Invalid sort direction");
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new InvalidTrainingRequestException("From must not be after to");
        }
    }

    public TrainingResponse getTrainingById(long id) {
        Training foundTraining = findTrainingOrThrow(id);
        return mapToResponse(foundTraining);
    }

    public Page<TrainingResponse> getTrainingsByPage(int page, int size, String sort) {

        if (page < 0) {
            throw new InvalidTrainingRequestException("Page must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new InvalidTrainingRequestException("Size must be between 1 and 100");
        }

        String[] sortParams = sort.split(",", -1);

        if (sortParams.length != 2) {
            throw new InvalidTrainingRequestException("Sort must have format property,direction");
        }

        String property = sortParams[0];
        String direction = sortParams[1];

        if (!VALID_SORT_PROPERTIES.contains(property)) {
            throw new InvalidTrainingRequestException("Parameter " + property + " is not valid");
        }

        Sort.Direction sortDirection = parseSortDirection(direction);
        Sort sortObject = Sort.by(sortDirection, property);

        Page<Training> trainingPage = trainingRepository.findAll(PageRequest.of(page, size, sortObject));
        return trainingPage.map(this::mapToResponse);
    }

    public void deleteTrainingById(long id) {
        Training training = findTrainingOrThrow(id);
        trainingRepository.delete(training);
    }

    public TrainingResponse updateTraining(long id, TrainingRequest request) {
        Training existingTraining = findTrainingOrThrow(id);

        updateTrainingFromRequest(existingTraining, request);
        existingTraining.setPace(calculatePace(existingTraining));

        Training savedTraining = trainingRepository.save(existingTraining);
        return mapToResponse(savedTraining);
    }

    public List<TrainingResponse> getTrainingsByMinDistance(double minDistance) {

        if (minDistance < 0) {
            throw new InvalidTrainingRequestException("Minimum distance must be 0 or greater");
        }

        List<Training> foundTrainings =
                trainingRepository.findByDistanceKmGreaterThanEqual(minDistance);
        return mapToResponseList(foundTrainings);
    }

    public TrainingStats getStats(LocalDate from, LocalDate to) {

        if (from == null && to == null) {
            return calculateTrainingStats(trainingRepository.findAll());
        } else if (from == null || to == null) {
            throw new InvalidTrainingRequestException("Both from and to must be provided");
        }

        validateDateRange(from, to);

        List<Training> trainings =
                trainingRepository.findByDateBetweenOrderByDateAsc(from, to);

        return calculateTrainingStats(trainings);
    }

    private TrainingStats calculateTrainingStats(List<Training> trainings) {
        TrainingStats stats = new TrainingStats();

        stats.setTotalTrainings(trainings.size());

        if (trainings.isEmpty()) {
            stats.setTotalDistance(0);
            stats.setAveragePace(0);
            stats.setAverageHeartRate(0);
            return stats;
        }

        double totalDistance = 0;
        double totalHeartRate = 0;
        double totalMinutes = 0;

        for (Training training : trainings) {
            totalDistance += training.getDistanceKm();
            totalHeartRate += training.getAverageHeartRate();
            totalMinutes += training.getDurationMinutes();
        }

        double averagePace = totalMinutes / totalDistance;
        double averageHeartRate = totalHeartRate / trainings.size();

        stats.setTotalDistance(totalDistance);
        stats.setAveragePace(averagePace);
        stats.setAverageHeartRate(averageHeartRate);

        return stats;
    }

    public List<TrainingResponse> getTrainingsBetweenDates(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        List<Training> trainings =
                trainingRepository.findByDateBetweenOrderByDateAsc(from, to);
        return mapToResponseList(trainings);
    }
}
