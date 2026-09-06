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

    @NotNull
    private LocalDate date;

    @Positive
    private double distanceKm;

    @Positive
    private int durationMinutes;

    @Min(30)
    @Max(250)
    private int averageHeartRate;

}
