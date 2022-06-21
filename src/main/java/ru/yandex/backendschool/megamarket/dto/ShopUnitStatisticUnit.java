package ru.yandex.backendschool.megamarket.dto;

import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import java.time.ZonedDateTime;

public record ShopUnitStatisticUnit(
        String id,
        String name,
        String parentId,
        ShopUnitType type,
        Long price,
        ZonedDateTime date
) { }
