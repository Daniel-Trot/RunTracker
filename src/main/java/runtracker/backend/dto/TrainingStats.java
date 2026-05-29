package runtracker.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrainingStats {

    private int totalTrainings;

    private double totalDistance;

    private double averagePace;

    private double averageHeartRate;
}
