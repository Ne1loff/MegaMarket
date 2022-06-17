package ru.yandex.backendschool.megamarket.exception.notFound;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:exceptionMessages.properties")
public class ItemNotFoundError extends NotFoundException {

    @Value("${error.validationErrorMessage}")
    private static String message;

    public ItemNotFoundError() {
        super(message);
    }

    public ItemNotFoundError(Throwable cause) {
        super(message, cause);
    }
}
