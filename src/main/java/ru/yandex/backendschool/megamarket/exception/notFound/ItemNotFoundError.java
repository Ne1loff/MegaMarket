package ru.yandex.backendschool.megamarket.exception.notFound;

public class ItemNotFoundError extends NotFoundException {

    private static String message = "Item not found";

    public ItemNotFoundError() {
        super(message);
    }

    public ItemNotFoundError(Throwable cause) {
        super(message, cause);
    }
}
