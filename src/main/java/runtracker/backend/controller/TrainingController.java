package runtracker.backend.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import runtracker.backend.dto.TrainingRequest;
import runtracker.backend.dto.TrainingResponse;
import runtracker.backend.dto.TrainingStats;
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
    public ResponseEntity<TrainingResponse> createTraining(@Valid @RequestBody TrainingRequest request) {

        TrainingResponse response = trainingService.createTraining(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public TrainingResponse getTrainingById(@PathVariable long id) {
        return trainingService.getTrainingById(id);
    }

    @GetMapping
    public Page<TrainingResponse> getTrainingsByPage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(defaultValue = "date,asc") String sort) {
        return trainingService.getTrainingsByPage(page, size, sort);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingById(@PathVariable long id) {
        trainingService.deleteTrainingById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public TrainingResponse updateTraining(@PathVariable long id, @Valid @RequestBody TrainingRequest request) {
        return trainingService.updateTraining(id, request);
    }

    @GetMapping("/filter")
    public List<TrainingResponse> getTrainingsByMinDistance(@RequestParam double minDistance) {
        return trainingService.getTrainingsByMinDistance(minDistance);
    }

    @GetMapping("/stats")
    public TrainingStats getStats(@RequestParam(required = false) LocalDate from,
                                  @RequestParam(required = false) LocalDate to) {
        return trainingService.getStats(from, to);
    }

    @GetMapping("/by-date")
    public List<TrainingResponse> getTrainingsBetweenDates(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return trainingService.getTrainingsBetweenDates(from, to);
    }

}