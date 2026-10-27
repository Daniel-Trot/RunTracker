package runtracker.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import runtracker.backend.dto.UserRequest;
import runtracker.backend.dto.UserResponse;
import runtracker.backend.exception.EmailAlreadyExistsException;
import runtracker.backend.model.User;
import runtracker.backend.repository.UserRepository;

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
}
