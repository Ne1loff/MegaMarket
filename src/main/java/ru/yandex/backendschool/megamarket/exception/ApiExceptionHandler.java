package ru.yandex.backendschool.megamarket.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.backendschool.megamarket.exception.badRequest.BadRequestException;
import ru.yandex.backendschool.megamarket.exception.notFound.NotFoundException;
import ru.yandex.backendschool.megamarket.exception.tooManyRequest.TooManyRequestException;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {NotFoundException.class})
    ResponseEntity<Error> handleNotFoundException(NotFoundException exception) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        Error error = new Error(status.value(), exception.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(value = {BadRequestException.class})
    ResponseEntity<Error> handleBadRequestException(BadRequestException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Error error = new Error(status.value(), exception.getMessage());
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(value = {TooManyRequestException.class})
    ResponseEntity<Error> handleTooManyRequest(TooManyRequestException exception) {
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
        Error error = new Error(status.value(), exception.getMessage());
        return ResponseEntity.status(status).body(error);
    }
}
