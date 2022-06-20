package ru.yandex.backendschool.megamarket.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.dto.ShopUnitDto;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.dto.ShopUnitStatisticUnit;
import ru.yandex.backendschool.megamarket.entity.ShopHistory;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

@Component(value = "MapperImpl")
public class ShopUnitMapperImpl implements ShopUnitMapper {
    @Override
    public ShopUnit mapToShopUnit(ShopUnitImport unitImport, Date date) {
        boolean unitIsOffer = unitImport.getType() == ShopUnitType.OFFER;

        return new ShopUnit(
                unitImport.getId(),
                unitImport.getName(),
                date,
                unitImport.getParentId(),
                unitImport.getType(),
                unitImport.getPrice(),
                unitImport.getParentId() == null ?
                        unitImport.getId() : "",
                unitIsOffer ? unitImport.getPrice() : 0L,
                unitIsOffer ? 1L : 0L,
                unitIsOffer ? null : new ArrayList<>()
        );
    }

    @Override
    public ShopUnitDto mapToShopUnitDto(ShopUnit unit) {
        if (unit == null) return null;
        var isOffer = unit.getType() == ShopUnitType.OFFER;
        return new ShopUnitDto(
                unit.getId(),
                unit.getName(),
                unit.getDate(),
                unit.getParentId(),
                unit.getType(),
                unit.getPrice(),
                isOffer ? null : unit.getChildren()
                        .stream().map(this::mapToShopUnitDto).toList()
        );
    }

    @Override
    public ShopUnitStatisticUnit mapToShopUnitStatisticUnit(ShopHistory shopHistory) {
        return new ShopUnitStatisticUnit(
                shopHistory.getShopUnitId(),
                shopHistory.getName(),
                shopHistory.getParentId(),
                shopHistory.getType(),
                shopHistory.getPrice(),
                shopHistory.getDate()
        );
    }
}
