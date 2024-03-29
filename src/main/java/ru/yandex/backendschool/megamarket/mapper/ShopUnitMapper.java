package ru.yandex.backendschool.megamarket.mapper;

import ru.yandex.backendschool.megamarket.dto.ShopUnitDto;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.dto.ShopUnitStatisticUnit;
import ru.yandex.backendschool.megamarket.entity.ShopHistory;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;

import java.time.ZonedDateTime;

public interface ShopUnitMapper {

    String mapDateToString(ZonedDateTime date);
    ShopUnit mapToShopUnit(ShopUnitImport unitImport, ZonedDateTime date);
    ShopUnitDto mapToShopUnitDto(ShopUnit unit);
    ShopHistory mapToShopHistory(ShopUnit unit);
    ShopUnitStatisticUnit mapToShopUnitStatisticUnit(ShopHistory shopHistory);
}
