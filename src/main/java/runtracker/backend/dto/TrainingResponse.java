package runtracker.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class TrainingResponse {

    private long id;
    private LocalDate date;
    private double distanceKm;
    private int durationMinutes;
    private int averageHeartRate;
    private double pace;

}
