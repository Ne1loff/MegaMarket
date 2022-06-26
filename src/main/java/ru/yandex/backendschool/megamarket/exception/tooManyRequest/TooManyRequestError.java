package ru.yandex.backendschool.megamarket.exception.tooManyRequest;

public class TooManyRequestError extends TooManyRequestException {

    private static final String MESSAGE = "Too many request";

    public TooManyRequestError() {
        super(MESSAGE);
    }

    public TooManyRequestError(Throwable cause) {
        super(MESSAGE, cause);
    }
}
