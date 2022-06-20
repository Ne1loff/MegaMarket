package ru.yandex.backendschool.megamarket.exception;

import lombok.Getter;

@Getter
public class Error {
    Integer status;
    String message;

    public Error(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}
