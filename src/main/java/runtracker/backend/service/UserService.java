package runtracker.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import runtracker.backend.dto.LoginRequest;
import runtracker.backend.dto.LoginResponse;
import runtracker.backend.dto.UserRequest;
import runtracker.backend.dto.UserResponse;
import runtracker.backend.exception.EmailAlreadyExistsException;
import runtracker.backend.exception.InvalidCredentialsException;
import runtracker.backend.model.User;
import runtracker.backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(UserRequest userRequest) {

        String email = userRequest.getEmail()
                .trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }

        User user = new User();
        user.setEmail(email);

        String password = userRequest.getPassword();
        String passwordHash = passwordEncoder.encode(password);

        user.setPasswordHash(passwordHash);
        User savedUser = userRepository.save(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setEmail(savedUser.getEmail());

        return userResponse;
    }

    public LoginResponse login(LoginRequest loginRequest) {

        String email = loginRequest.getEmail()
                .trim().toLowerCase();

        User foundUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password!"));

        if (!passwordEncoder.matches(
                loginRequest.getPassword(),
                foundUser.getPasswordHash()
        )) {
            throw new InvalidCredentialsException("Invalid email or password!");
        }

        LoginResponse response = new LoginResponse();
        response.setToken(jwtService.generateToken(email));

        return response;
    }
}
