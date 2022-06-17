package ru.yandex.backendschool.megamarket.exception.badRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:exceptionMessages.properties")
public class ValidationError extends BadRequestException {

    @Value("${error.validationErrorMessage}")
    private static String message;

    public ValidationError() {
        super(message);
    }

    public ValidationError(Throwable cause) {
        super(message, cause);
    }
}
