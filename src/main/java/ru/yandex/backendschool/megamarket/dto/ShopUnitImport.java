package ru.yandex.backendschool.megamarket.dto;

import lombok.Data;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

@Data
public class ShopUnitImport {

    String id;

    String name;

    String parentId;

    ShopUnitType type;

    Long price;
}
