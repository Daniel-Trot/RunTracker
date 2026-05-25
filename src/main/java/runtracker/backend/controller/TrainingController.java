package runtracker.backend.controller;

import org.springframework.web.bind.annotation.*;
import runtracker.backend.model.Training;
import runtracker.backend.repository.TrainingRepository;

import java.util.List;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingRepository trainingRepository;

    public TrainingController(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @PostMapping
    public Training createTraining(@RequestBody Training training) {
        return trainingRepository.save(training);
    }

    @GetMapping
    public List<Training> getAllTrainings() {
        return trainingRepository.findAll();
    }
}