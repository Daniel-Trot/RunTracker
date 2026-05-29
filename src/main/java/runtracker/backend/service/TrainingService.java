package runtracker.backend.service;

import org.springframework.stereotype.Service;
import runtracker.backend.dto.TrainingStats;
import runtracker.backend.model.Training;
import runtracker.backend.repository.TrainingRepository;

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

        double helpDistance = 0;
        for(Training training: trainings){
            helpDistance += training.getDistanceKm();
        }
        trainingStats.setTotalDistance(helpDistance);

        double helpPace = 0;
        for(Training training: trainings){
            helpPace += training.getPace();
        }
        helpPace = helpPace / trainings.size();
        trainingStats.setAveragePace(helpPace);

        double helpAvgHeartRate = 0;
        for(Training training: trainings){
            helpAvgHeartRate += training.getAverageHeartRate();
        }
        helpAvgHeartRate /= trainings.size();
        trainingStats.setAverageHeartRate(helpAvgHeartRate);

        return trainingStats;
    }

}
