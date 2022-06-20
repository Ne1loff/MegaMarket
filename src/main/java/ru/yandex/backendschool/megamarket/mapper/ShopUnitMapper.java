package ru.yandex.backendschool.megamarket.mapper;

import ru.yandex.backendschool.megamarket.dto.ShopUnitDto;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.dto.ShopUnitStatisticUnit;
import ru.yandex.backendschool.megamarket.entity.ShopHistory;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;

import java.util.Date;

public interface ShopUnitMapper {

    ShopUnit mapToShopUnit(ShopUnitImport unitImport, Date date);
    ShopUnitDto mapToShopUnitDto(ShopUnit unit);
    ShopUnitStatisticUnit mapToShopUnitStatisticUnit(ShopHistory shopHistory);
}
