package ru.yandex.backendschool.megamarket.exception.badRequest;

public class ValidationError extends BadRequestException {

    private static final String MESSAGE = "Validation Failed";

    public ValidationError() {
        super(MESSAGE);
    }

    public ValidationError(Throwable cause) {
        super(MESSAGE, cause);
    }
}
