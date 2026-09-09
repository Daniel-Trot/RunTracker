package runtracker.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import runtracker.backend.dto.TrainingRequest;
import runtracker.backend.dto.TrainingResponse;
import runtracker.backend.dto.TrainingStats;
import runtracker.backend.model.Training;
import runtracker.backend.repository.TrainingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    public TrainingResponse createTraining(TrainingRequest request) {
        Training training = mapToEntity(request);

        training.setPace((double) training.getDurationMinutes() / training.getDistanceKm());

        Training savedTraining = trainingRepository.save(training);

        return mapToResponse(savedTraining);
    }

    private Training mapToEntity(TrainingRequest request) {
        Training training = new Training();

        training.setDate(request.getDate());
        training.setDistanceKm(request.getDistanceKm());
        training.setDurationMinutes(request.getDurationMinutes());
        training.setAverageHeartRate(request.getAverageHeartRate());

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

    public TrainingResponse getTrainingById(long id) {
        Training foundTraining = trainingRepository.findById(id).orElse(null);

        if (foundTraining == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    String.format("Training with id %d not found", id)
            );
        }

        return mapToResponse(foundTraining);
    }

    public Page<TrainingResponse> getTrainingsByPage(int page, int size, String sort) {

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

        String[] sortParams = sort.split(",", -1);

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
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Parameter " + property + " is not valid");
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

        Page<Training> trainingPage = trainingRepository.findAll(PageRequest.of(page, size, sortObject));
        return trainingPage.map(this::mapToResponse);
    }

    public void deleteTrainingById(long id) {
        trainingRepository.deleteById(id);
    }

    public TrainingResponse updateTraining(long id, TrainingRequest request) {
        Training existingTraining = trainingRepository.findById(id).orElse(null);

        if (existingTraining == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Training with id " + id + " not found");
        }

        existingTraining.setDate(request.getDate());
        existingTraining.setDistanceKm(request.getDistanceKm());
        existingTraining.setDurationMinutes(request.getDurationMinutes());
        existingTraining.setAverageHeartRate(request.getAverageHeartRate());

        existingTraining.setPace(existingTraining.getDurationMinutes() / existingTraining.getDistanceKm());

        Training savedTraining = trainingRepository.save(existingTraining);
        return mapToResponse(savedTraining);
    }

    public List<TrainingResponse> getTrainingsByMinDistance(double minDistance) {
        List<Training> foundTrainings = trainingRepository.findByDistanceKmGreaterThanEqual(minDistance);
        List<TrainingResponse> response = new ArrayList<>();

        for (Training training : foundTrainings) {
            response.add(mapToResponse(training));
        }

        return response;
    }

    public TrainingStats getStats(LocalDate from, LocalDate to) {

        if (from == null && to == null) {
            return calculateTrainingStats(trainingRepository.findAll());
        } else if (from == null || to == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Both from and to must be provided"
            );
        } else if (from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "From must not be after to"
            );
        }

        List<Training> trainings =
                trainingRepository.findByDateBetweenOrderByDateAsc(from, to);

        return calculateTrainingStats(trainings);
    }

    public TrainingStats calculateTrainingStats(List<Training> trainings) {
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

    public List<TrainingResponse> getTrainingsBetweenDates(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "from must not be after to");
        }

        List<Training> trainings = trainingRepository.findByDateBetweenOrderByDateAsc(from, to);
        List<TrainingResponse> response = new ArrayList<>();

        for (Training training : trainings) {
            response.add(mapToResponse(training));
        }

        return response;
    }

}
