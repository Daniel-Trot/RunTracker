package runtracker.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import runtracker.backend.dto.TrainingRequest;
import runtracker.backend.dto.TrainingResponse;
import runtracker.backend.dto.TrainingStats;
import runtracker.backend.model.Training;
import runtracker.backend.repository.TrainingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void checkPaceCalculation() {

        // GIVEN
        TrainingRequest trainingRequest = new TrainingRequest();
        trainingRequest.setDate(LocalDate.of(2026, 9, 9));
        trainingRequest.setDistanceKm(10);
        trainingRequest.setDurationMinutes(50);
        trainingRequest.setAverageHeartRate(145);

        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(invocation -> {
                    Training training = invocation.getArgument(0);
                    training.setId(1L);
                    return training;
                });

        // WHEN
        TrainingResponse response =
                trainingService.createTraining(trainingRequest);

        // THEN
        assertEquals(5.0, response.getPace());
    }

    @Test
    void getTrainingByIdReturnsTraining(){
        // GIVEN
        Training training = new Training();
        training.setId(5L);
        training.setDate(LocalDate.of(2026, 9, 9));
        training.setDistanceKm(10);
        training.setDurationMinutes(50);
        training.setAverageHeartRate(145);
        training.setPace(5.0);

        when(trainingRepository.findById(5L)).
                thenReturn(Optional.of(training));

        // WHEN
        TrainingResponse response =
                trainingService.getTrainingById(5L);

        // THEN
        assertEquals(5L, response.getId());
        assertEquals(10, response.getDistanceKm());
        assertEquals(50, response.getDurationMinutes());
        assertEquals(5.0, response.getPace());
    }

    @Test
    void getTrainingByIdThrowsNotFoundException(){
        // GIVEN
        when(trainingRepository.findById(999L)).
                thenReturn(Optional.empty());

        // WHEN + THEN
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> trainingService.getTrainingById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void checkTrainingStatsCalculation() {
        // GIVEN
        Training training1 = new Training();
        training1.setDistanceKm(5);
        training1.setDurationMinutes(50);
        training1.setAverageHeartRate(95);

        Training training2 = new Training();
        training2.setDistanceKm(31);
        training2.setDurationMinutes(150);
        training2.setAverageHeartRate(180);

        Training training3 = new Training();
        training3.setDistanceKm(12);
        training3.setDurationMinutes(46);
        training3.setAverageHeartRate(155);

        List<Training> trainingList = new ArrayList<>();
        trainingList.add(training1);
        trainingList.add(training2);
        trainingList.add(training3);

        when(trainingRepository.findAll()).thenReturn(trainingList);

        // WHEN
        TrainingStats stats = trainingService.getStats(null,null);

        // THEN
        assertEquals(3, stats.getTotalTrainings());
        assertEquals(48.0, stats.getTotalDistance());
        assertEquals(5.125, stats.getAveragePace());
        assertEquals(143.333, stats.getAverageHeartRate(), 0.001);
    }

    @Test
    void checkTrainingStatsThrowsNotFoundException(){
        // GIVEN
        LocalDate from = LocalDate.of(2026, 9, 30);
        LocalDate to = LocalDate.of(2026, 9, 1);

        // WHEN + THEN
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> trainingService.getStats(from,to));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

    }
}