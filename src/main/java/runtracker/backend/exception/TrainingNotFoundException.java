package runtracker.backend.exception;

public class TrainingNotFoundException extends RuntimeException {

    public TrainingNotFoundException(long id) {
        super("Training with id " + id + " not found");
    }
}