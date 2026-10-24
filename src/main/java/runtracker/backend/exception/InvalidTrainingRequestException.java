package runtracker.backend.exception;

public class InvalidTrainingRequestException extends RuntimeException {

    public InvalidTrainingRequestException(String message) {
        super(message);
    }
}
