package ru.yandex.backendschool.megamarket.dto;

import lombok.Data;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;

import java.util.Date;
import java.util.List;

@Data
public class ShopUnitDto {

    String id;

    String name;

    Date date;

    String parentId;

    ShopUnitType type;

    Long price;

    List<ShopUnit> children;
}
