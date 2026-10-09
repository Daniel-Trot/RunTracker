package runtracker.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import runtracker.backend.dto.TrainingRequest;
import runtracker.backend.dto.TrainingResponse;
import runtracker.backend.service.TrainingService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingController.class)
public class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingService trainingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getTrainingById() throws Exception {
        // GIVEN
        TrainingResponse response = new TrainingResponse();
        response.setId(5L);
        response.setDistanceKm(10);
        response.setDurationMinutes(50);
        response.setAverageHeartRate(145);
        response.setPace(5.0);

        when(trainingService.getTrainingById(5L))
                .thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(get("/api/trainings/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.distanceKm").value(10))
                .andExpect(jsonPath("$.pace").value(5.0));
    }

    @Test
    void createTraining() throws Exception {

        // GIVEN
        TrainingRequest request = new TrainingRequest();
        request.setDate(LocalDate.of(2026, 9, 17));
        request.setDistanceKm(10);
        request.setDurationMinutes(50);
        request.setAverageHeartRate(145);

        TrainingResponse response = new TrainingResponse();
        response.setId(5L);
        response.setDate(LocalDate.of(2026, 9, 17));
        response.setDistanceKm(10);
        response.setDurationMinutes(50);
        response.setAverageHeartRate(145);
        response.setPace(5.0);

        when(trainingService.createTraining(any(TrainingRequest.class)))
                .thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(
                        post("/api/trainings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.distanceKm").value(10))
                .andExpect(jsonPath("$.pace").value(5.0));
    }
}
