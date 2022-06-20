package ru.yandex.backendschool.megamarket.dto;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;

import java.util.Date;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ShopUnitDto {

    String id;

    String name;

    Date date;

    String parentId;

    ShopUnitType type;

    Long price;

    List<ShopUnitDto> children;

    public ShopUnitDto(String id, String name,
                       Date date, String parentId,
                       ShopUnitType type, Long price,
                       List<ShopUnitDto> children
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.parentId = parentId;
        this.type = type;
        this.price = price;
        this.children = children;
    }
}
