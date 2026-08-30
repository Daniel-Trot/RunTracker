package runtracker.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Training {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "date must not be null")
    @Column(nullable = false)
    private LocalDate date;

    @Positive(message = "distanceKm must be greater than 0")
    private double distanceKm;

    @Positive(message = "durationMinutes must be greater than 0")
    private int durationMinutes;

    @Min(30)
    @Max(250)
    private int averageHeartRate;

    private double pace;
}