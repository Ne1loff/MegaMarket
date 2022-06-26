package ru.yandex.backendschool.megamarket.exception.notFound;

public class ItemNotFoundError extends NotFoundException {

    private static final String MESSAGE = "Item not found";

    public ItemNotFoundError() {
        super(MESSAGE);
    }

    public ItemNotFoundError(Throwable cause) {
        super(MESSAGE, cause);
    }
}
