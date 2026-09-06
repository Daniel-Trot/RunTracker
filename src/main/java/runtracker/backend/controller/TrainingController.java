package runtracker.backend.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import runtracker.backend.dto.TrainingRequest;
import runtracker.backend.dto.TrainingResponse;
import runtracker.backend.dto.TrainingStats;
import runtracker.backend.model.Training;
import runtracker.backend.service.TrainingService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    public TrainingResponse createTraining(@Valid @RequestBody TrainingRequest request) {
        return trainingService.createTraining(request);
    }

    @GetMapping("/{id}")
    public Training getTrainingById(@PathVariable long id) {
        return trainingService.getTrainingById(id);
    }

    @GetMapping
    public Page<Training> getTrainingsByPage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(defaultValue = "date,asc") String sort) {
        return trainingService.getTrainingsByPage(page, size, sort);
    }

    @DeleteMapping("/{id}")
    public void deleteTrainingById(@PathVariable long id) {
        trainingService.deleteTrainingById(id);
    }

    @PutMapping("/{id}")
    public TrainingResponse updateTraining(@PathVariable long id, @Valid @RequestBody TrainingRequest request) {
        return trainingService.updateTraining(id, request);
    }

    @GetMapping("/filter")
    public List<Training> getTrainingsByMinDistance(@RequestParam double minDistance) {
        return trainingService.getTrainingsByMinDistance(minDistance);
    }

    @GetMapping("/stats")
    public TrainingStats getStats(@RequestParam(required = false) LocalDate from ,
                                  @RequestParam(required = false) LocalDate to ) {
        return trainingService.getStats(from, to);
    }

    @GetMapping("/by-date")
    public List<Training> getTrainingsBetweenDates(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return trainingService.getTrainingsBetweenDates(from, to);
    }

}