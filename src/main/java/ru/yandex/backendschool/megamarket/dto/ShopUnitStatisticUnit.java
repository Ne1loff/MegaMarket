package ru.yandex.backendschool.megamarket.dto;

import lombok.Getter;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

import java.util.Date;

@Getter
public class ShopUnitStatisticUnit {

    private final String id;

    private final String name;

    private final String parentId;

    private final ShopUnitType type;

    private final Long price;

    private final Date date;

    public ShopUnitStatisticUnit(String id, String name, String parentId, ShopUnitType type, Long price, Date date) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.type = type;
        this.price = price;
        this.date = date;
    }
}
