package ru.yandex.backendschool.megamarket.util;

public record Tuple<T1, T2>(
        T1 first,
        T2 second
) { }
