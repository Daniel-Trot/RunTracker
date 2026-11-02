package runtracker.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import runtracker.backend.dto.LoginRequest;
import runtracker.backend.dto.LoginResponse;
import runtracker.backend.dto.UserRequest;
import runtracker.backend.dto.UserResponse;
import runtracker.backend.exception.EmailAlreadyExistsException;
import runtracker.backend.exception.InvalidCredentialsException;
import runtracker.backend.model.User;
import runtracker.backend.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser(){

        // GIVEN
        UserRequest request = new UserRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password");

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password"))
                .thenReturn("$hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        // WHEN + THEN
        UserResponse response = userService.register(request);

        assertEquals(1L, response.getId());
        assertEquals("test@gmail.com", response.getEmail());

        verify(passwordEncoder).encode("password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("$hashedPassword", savedUser.getPasswordHash());
    }

    @Test
    void checkEmailAlreadyExists(){

        // GIVEN
        UserRequest request = new UserRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password");

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(true);

        // WHEN + THEN
        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.register(request)
        );

        assertEquals("Email already exists!", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void checkLoginSuccess(){

        // GIVEN
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPasswordHash("$hashedPassword");

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password","$hashedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken("test@gmail.com"))
                .thenReturn("token");

        // WHEN
        LoginResponse response = userService.login(request);

        // THEN
        assertEquals("token", response.getToken());
        verify(passwordEncoder).matches("password", "$hashedPassword");
        verify(jwtService).generateToken("test@gmail.com");
    }

    @Test
    void checkEmailNotFound(){

        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        // WHEN
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request)
        );

        // THEN
        assertEquals("Invalid email or password!", exception.getMessage());
        verify(userRepository).findByEmail("test@gmail.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService,  never()).generateToken(anyString());
    }

    @Test
    void checkWrongPassword(){

        // Given
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPasswordHash("$hashedPassword");

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password","$hashedPassword"))
                .thenReturn(false);

        // WHEN
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> userService.login(request));

        // THEN
        assertEquals("Invalid email or password!", exception.getMessage());
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches("password", "$hashedPassword");
        verify(jwtService, never()).generateToken(anyString());
    }
}
