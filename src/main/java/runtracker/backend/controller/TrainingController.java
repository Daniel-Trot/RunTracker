package runtracker.backend.controller;

import org.springframework.web.bind.annotation.*;
import runtracker.backend.dto.TrainingStats;
import runtracker.backend.model.Training;
import runtracker.backend.service.TrainingService;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    public Training createTraining(@RequestBody Training training) {
        return trainingService.createTraining(training);
    }

    @GetMapping("/{id}")
    public Training getTrainingById(@PathVariable long id) {
        return trainingService.getTrainingById(id);
    }

    @GetMapping
    public List<Training> getAllTrainings() {
        return trainingService.getAllTrainings();
    }

    @DeleteMapping("/{id}")
    public void deleteTrainingById(@PathVariable long id) {
        trainingService.deleteTrainingById(id);
    }

    @PutMapping("/{id}")
    public Training updateTraining(@PathVariable long id, @RequestBody Training training) {
        return trainingService.updateTraining(id, training);
    }

    @GetMapping("/filter")
    public List<Training> getTrainingsByMinDistance(@RequestParam double minDistance) {
        return trainingService.getTrainingsByMinDistance(minDistance);
    }

    @GetMapping("/stats")
    public TrainingStats getStats() {
        return trainingService.getStats();
    }

}