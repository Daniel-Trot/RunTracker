package runtracker.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class TrainingRequest {

    @NotNull(message = "date must not be null")
    private LocalDate date;

    @NotNull(message = "distanceKm must not be null")
    @Positive(message = "distanceKm must be grater than 0")
    private Double distanceKm;

    @NotNull(message = "durationMinutes must not be null")
    @Positive(message = "durationMinutes must be greater than 0")
    private Integer durationMinutes;

    @NotNull(message = "averageHeartRate must not be null")
    @Min(value = 30, message = "averageHeartRate must be at least 30")
    @Max(value = 250, message = "averageHeartRate must be at most 250")
    private Integer averageHeartRate;

}
