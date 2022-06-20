package ru.yandex.backendschool.megamarket.exception.badRequest;

public class ValidationError extends BadRequestException {

    private static final String message = "Validation Failed";

    public ValidationError() {
        super(message);
    }

    public ValidationError(Throwable cause) {
        super(message, cause);
    }
}
