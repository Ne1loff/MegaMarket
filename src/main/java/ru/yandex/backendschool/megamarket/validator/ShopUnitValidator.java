package ru.yandex.backendschool.megamarket.validator;

import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;

import java.util.Date;

public interface ShopUnitValidator {

    void validateShopUnitImport(ShopUnitImport unitImport);

    Date validateShopUnitImportRequestDateAndGet(String dateString);
}
