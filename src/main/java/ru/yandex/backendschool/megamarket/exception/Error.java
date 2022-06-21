package ru.yandex.backendschool.megamarket.exception;

public record Error(
        Integer status,
        String message
) { }
