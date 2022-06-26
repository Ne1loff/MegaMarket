package ru.yandex.backendschool.megamarket.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.dto.ShopUnitDto;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.dto.ShopUnitStatisticUnit;
import ru.yandex.backendschool.megamarket.entity.ShopHistory;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component(value = "MapperImpl")
public class ShopUnitMapperImpl implements ShopUnitMapper {

    @Override
    public String mapDateToString(ZonedDateTime date) {
        return date.withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV"));
    }

    @Override
    public ShopUnit mapToShopUnit(ShopUnitImport unitImport, ZonedDateTime date) {
        boolean unitIsOffer = unitImport.type() == ShopUnitType.OFFER;

        return new ShopUnit(
                unitImport.id(),
                unitImport.name(),
                date,
                unitImport.parentId(),
                unitImport.type(),
                unitImport.price(),
                unitImport.parentId() == null ?
                        unitImport.id() : "",
                unitIsOffer ? unitImport.price() : 0L,
                unitIsOffer ? 1L : 0L,
                unitIsOffer ? null : new ArrayList<>()
        );
    }

    @Override
    public ShopUnitDto mapToShopUnitDto(ShopUnit unit) {
        if (unit == null) return null;
        var isOffer = unit.getType() == ShopUnitType.OFFER;

        var dateString = mapDateToString(unit.getDate());

        return new ShopUnitDto(
                unit.getId(),
                unit.getName(),
                dateString,
                unit.getParentId(),
                unit.getType(),
                unit.getPrice(),
                isOffer ? null : unit.getChildren()
                        .stream().map(this::mapToShopUnitDto).toList()
        );
    }

    @Override
    public ShopHistory mapToShopHistory(ShopUnit unit) {
        return new ShopHistory(
                unit.getId(),
                unit.getName(),
                unit.getDate(),
                unit.getParentId(),
                unit.getType(),
                unit.getPrice()
        );
    }

    @Override
    public ShopUnitStatisticUnit mapToShopUnitStatisticUnit(ShopHistory shopHistory) {
        var dateString = mapDateToString(shopHistory.getDate());
        return new ShopUnitStatisticUnit(
                shopHistory.getShopUnitId(),
                shopHistory.getName(),
                shopHistory.getParentId(),
                shopHistory.getType(),
                shopHistory.getPrice(),
                dateString
        );
    }
}
