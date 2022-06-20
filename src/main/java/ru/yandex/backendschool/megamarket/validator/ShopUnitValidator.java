package ru.yandex.backendschool.megamarket.validator;

import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;

import java.util.Date;

public interface ShopUnitValidator {

    boolean isInvalidUuid(String uuid);

    void validateShopUnitImport(ShopUnitImport unitImport);

    Date validateDateAndGet(String dateString);
}
