package runtracker.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import runtracker.backend.dto.TrainingStats;
import runtracker.backend.model.Training;
import runtracker.backend.repository.TrainingRepository;

import java.time.LocalDate;
import java.util.List;

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

    public List<Training> getAllTrainings() {
        return trainingRepository.findAll();
    }

    public void deleteTrainingById(long id) {
        trainingRepository.deleteById(id);
    }

    public Training updateTraining(long id, Training training){
        Training existingTraining = trainingRepository.findById(id).orElse(null);

        if(existingTraining == null){
            return null;
        }

        existingTraining.setDate(training.getDate());
        existingTraining.setDistanceKm(training.getDistanceKm());
        existingTraining.setDurationMinutes(training.getDurationMinutes());
        existingTraining.setAverageHeartRate(training.getAverageHeartRate());

        existingTraining.setPace(
                existingTraining.getDurationMinutes() / existingTraining.getDistanceKm()
        );

        return trainingRepository.save(existingTraining);
    }

    public List<Training> getTrainingsByMinDistance(double minDistance){
        return trainingRepository.findByDistanceKmGreaterThanEqual(minDistance);
    }

    public TrainingStats getStats(){
        List<Training> trainings = trainingRepository.findAll();
        TrainingStats trainingStats = new TrainingStats();

        trainingStats.setTotalTrainings(trainings.size());

        if (trainings.isEmpty()){
            trainingStats.setTotalDistance(0);
            trainingStats.setAveragePace(0);
            trainingStats.setAverageHeartRate(0);
        }else {

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
        if(from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must not be after to");
        }
        return trainingRepository.findByDateBetweenOrderByDateAsc(from, to);
    }
}
