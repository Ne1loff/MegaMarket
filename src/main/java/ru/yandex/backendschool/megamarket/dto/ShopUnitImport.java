package ru.yandex.backendschool.megamarket.dto;

import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

public record ShopUnitImport(
        String id, String name, String parentId,
        ShopUnitType type, Long price
) {
    public boolean haveParentId() {
        return parentId != null;
    }
}
