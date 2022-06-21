package ru.yandex.backendschool.megamarket.dto;

import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import java.util.List;

public record ShopUnitDto(
        String id,
        String name,
        String date,
        String parentId,
        ShopUnitType type,
        Long price,
        List<ShopUnitDto> children
) { }
