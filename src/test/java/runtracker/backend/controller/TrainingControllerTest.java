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
import runtracker.backend.exception.TrainingNotFoundException;
import runtracker.backend.service.TrainingService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void getTrainingByIdReturnsTraining() throws Exception {
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
    void getTrainingsBetweenDatesReturnsTrainings() throws Exception {

        // GIVEN
        TrainingResponse response1 = new TrainingResponse();
        response1.setId(5L);
        response1.setDate(LocalDate.of(2026, 9, 17));
        response1.setDistanceKm(10);
        response1.setDurationMinutes(50);
        response1.setAverageHeartRate(145);
        response1.setPace(5.0);

        TrainingResponse response2 = new TrainingResponse();
        response2.setId(6L);
        response2.setDate(LocalDate.of(2026, 9, 17));
        response2.setDistanceKm(10);
        response2.setDurationMinutes(60);
        response2.setAverageHeartRate(145);
        response2.setPace(6.0);

        List<TrainingResponse> responseList = new ArrayList<>();
        responseList.add(response1);
        responseList.add(response2);

        when(trainingService.getTrainingsBetweenDates(
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(responseList);

        // WHEN + THEN
        mockMvc.perform(get("/api/trainings/by-date")
                        .param("from", "2026-09-17")
                        .param("to", "2026-09-17")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].distanceKm").value(10))
                .andExpect(jsonPath("$[0].durationMinutes").value(50))
                .andExpect(jsonPath("$[1].id").value(6))
                .andExpect(jsonPath("$[1].pace").value(6.0));
    }

    @Test
    void createTrainingReturnsCreatedTraining() throws Exception {

        // GIVEN
        TrainingRequest request = new TrainingRequest();
        request.setDate(LocalDate.of(2026, 9, 17));
        request.setDistanceKm(10.0);
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.distanceKm").value(10))
                .andExpect(jsonPath("$.pace").value(5.0));
    }

    @Test
    void createTrainingWithNegativeDistanceKm() throws Exception {

        // GIVEN
        TrainingRequest request = new TrainingRequest();
        request.setDate(LocalDate.of(2026, 9, 17));
        request.setDistanceKm(-5.0);
        request.setDurationMinutes(50);
        request.setAverageHeartRate(145);

        // WHEN + THEN
        mockMvc.perform(
                        post("/api/trainings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTrainingWithInvalidDate() throws Exception {

        // GIVEN
        TrainingRequest request = new TrainingRequest();
        request.setDate(null);
        request.setDistanceKm(10.0);
        request.setDurationMinutes(50);
        request.setAverageHeartRate(145);

        // WHEN + THEN
        mockMvc.perform(
                        post("/api/trainings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTrainingReturnsNoContent() throws Exception {

        // WHEN + THEN
        mockMvc.perform(
                        delete("/api/trainings/{id}", 5L)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTrainingByIdReturnsNotFound() throws Exception {

        // GIVEN
        doThrow(new TrainingNotFoundException(999L))
                .when(trainingService)
                .deleteTrainingById(999L);

        // WHEN + THEN
        mockMvc.perform(
                        delete("/api/trainings/{id}", 999L)
                )
                .andExpect(status().isNotFound());
    }
}
