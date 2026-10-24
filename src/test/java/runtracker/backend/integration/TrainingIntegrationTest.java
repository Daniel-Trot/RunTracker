package runtracker.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import runtracker.backend.dto.TrainingRequest;
import runtracker.backend.model.Training;
import runtracker.backend.repository.TrainingRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TrainingIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("runtracker_test")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrainingRepository trainingRepository;

    @BeforeEach
    void cleanDatabase() {
        trainingRepository.deleteAll();
    }

    @Test
    void createTrainingStoresTrainingInDatabase() throws Exception {

        // GIVEN
        TrainingRequest request = new TrainingRequest();
        request.setDate(LocalDate.of(2026, 10, 24));
        request.setDistanceKm(10);
        request.setAverageHeartRate(125);
        request.setDurationMinutes(50);

        // WHEN + THEN
        mockMvc.perform(
                        post("/api/trainings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.distanceKm").value(10))
                .andExpect(jsonPath("$.pace").value(5));

        // THEN: saved in ProstgreSQL?

        assertEquals(1, trainingRepository.count());

        Training savedTraining = trainingRepository.findAll().getFirst();

        assertEquals(10.0, savedTraining.getDistanceKm());
        assertEquals(50, savedTraining.getDurationMinutes());
        assertEquals(5.0, savedTraining.getPace());
    }

    @Test
    void getTrainingByIdReturnsStoredTraining() throws Exception {

        // GIVEN
        Training training = new Training();
        training.setDate(LocalDate.of(2026, 10, 24));
        training.setDistanceKm(10);
        training.setAverageHeartRate(125);
        training.setDurationMinutes(50);
        training.setPace(5.0);

        Training saved = trainingRepository.save(training);

        // WHEN + THEN
        mockMvc.perform(get("/api/trainings/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.distanceKm").value(10))
                .andExpect(jsonPath("$.pace").value(5.0));
    }

    @Test
    void getTrainingByIdReturnsNotFoundForUnknownId() throws Exception {

        mockMvc.perform(get("/api/trainings/999999"))
                .andExpect(status().isNotFound());
    }
}