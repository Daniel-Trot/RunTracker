package runtracker.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Trainings",
        description = "Endpoints for managing and analyzing training activities"
)
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Operation(
            summary = "Create training",
            description = "Creates a new training activity"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Training created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid training data"
            )
    })
    @PostMapping
    public ResponseEntity<TrainingResponse> createTraining(@Valid @RequestBody TrainingRequest request) {

        TrainingResponse response = trainingService.createTraining(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get training by ID",
            description = "Returns a single training identified by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training found"),
            @ApiResponse(responseCode = "404", description = "Training not found")
    })
    @GetMapping("/{id}")
    public TrainingResponse getTrainingById(@PathVariable long id) {
        return trainingService.getTrainingById(id);
    }

    @Operation(
            summary = "Get trainings",
            description = "Returns trainings with pagination and sorting"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Trainings returned successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination or sorting parameters"
            )
    })
    @GetMapping
    public Page<TrainingResponse> getTrainingsByPage(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(defaultValue = "date,asc") String sort) {
        return trainingService.getTrainingsByPage(page, size, sort);
    }

    @Operation(
            summary = "Delete training",
            description = "Deletes a training identified by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Training deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Training not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingById(@PathVariable long id) {
        trainingService.deleteTrainingById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update training",
            description = "Updates an existing training identified by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid training data"),
            @ApiResponse(responseCode = "404", description = "Training not found")
    })
    @PutMapping("/{id}")
    public TrainingResponse updateTraining(@PathVariable long id, @Valid @RequestBody TrainingRequest request) {
        return trainingService.updateTraining(id, request);
    }

    @Operation(
            summary = "Filter trainings by minimum distance",
            description = "Returns trainings with a distance greater than or equal to the given minimum distance"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Filtered trainings returned successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid minimum distance"
            )
    })
    @GetMapping("/filter")
    public List<TrainingResponse> getTrainingsByMinDistance(@RequestParam double minDistance) {
        return trainingService.getTrainingsByMinDistance(minDistance);
    }

    @Operation(
            summary = "Get training statistics",
            description = "Returns aggregated statistics for training activities"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @GetMapping("/stats")
    public TrainingStats getStats(@RequestParam(required = false) LocalDate from,
                                  @RequestParam(required = false) LocalDate to) {
        return trainingService.getStats(from, to);
    }

    @Operation(
            summary = "Filter trainings by date range",
            description = "Returns trainings between the given start and end dates"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @GetMapping("/by-date")
    public List<TrainingResponse> getTrainingsBetweenDates(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return trainingService.getTrainingsBetweenDates(from, to);
    }

}