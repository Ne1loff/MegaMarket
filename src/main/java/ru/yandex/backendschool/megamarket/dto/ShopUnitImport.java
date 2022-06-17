package ru.yandex.backendschool.megamarket.dto;

import lombok.Data;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

import javax.validation.constraints.NotNull;

@Data
public class ShopUnitImport {

    @NotNull
    String id;

    @NotNull
    String name;

    String parentId;

    @NotNull
    ShopUnitType type;

    Long price;
}
