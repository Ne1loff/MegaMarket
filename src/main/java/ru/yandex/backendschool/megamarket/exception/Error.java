package ru.yandex.backendschool.megamarket.exception;

import javax.validation.constraints.NotNull;

public class Error {
    Integer status;
    String message;

    public Error(@NotNull Integer status, @NotNull String message) {
        this.status = status;
        this.message = message;
    }
}
