package runtracker.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import runtracker.backend.dto.LoginRequest;
import runtracker.backend.dto.LoginResponse;
import runtracker.backend.dto.UserRequest;
import runtracker.backend.dto.UserResponse;
import runtracker.backend.exception.EmailAlreadyExistsException;
import runtracker.backend.exception.InvalidCredentialsException;
import runtracker.backend.service.UserService;
import org.springframework.context.annotation.Import;
import runtracker.backend.config.SecurityConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser() throws Exception {

        // GIVEN
        UserRequest request = new UserRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password");

        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("test@gmail.com");

        when(userService.register(any(UserRequest.class)))
                .thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }

    @Test
    void emailAlreadyExists() throws Exception {

        // GIVEN
        UserRequest request = new UserRequest();
        request.setEmail("Test@gmail.com");
        request.setPassword("password");

        when(userService.register(any(UserRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists!"));

        // WHEN + THEN
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());
    }

    @Test
    void createUserWithInvalidEmail() throws Exception {

        // GIVEN
        UserRequest request = new UserRequest();
        request.setEmail("abc");
        request.setPassword("password");

        // WHEN + THEN
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserWithInvalidPassword() throws Exception {

        // GIVEN
        UserRequest request = new UserRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("123");

        // WHEN + THEN
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginSucceded() throws Exception {

        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password");

        LoginResponse response = new LoginResponse();
        response.setToken("token");

        when(userService.login(any(LoginRequest.class)))
                .thenReturn(response);

        // WHEN + THEN
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("token"));
    }

    @Test
    void loginInvalidCredentialsIsUnauthorized() throws Exception {

        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password!"));

        // WHEN + THEN
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Invalid email or password!"));
    }

    @Test
    void loginInvalidCredentialsBadRequest() throws Exception {

        // GIVEN
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("123");

        // WHEN + THEN
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }
}
