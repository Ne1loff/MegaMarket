package ru.yandex.backendschool.megamarket.exception.tooManyRequest;

public class TooManyRequestException extends RuntimeException {

    public TooManyRequestException(String message) {
        super(message);
    }

    public TooManyRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
