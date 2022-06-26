package ru.yandex.backendschool.megamarket.mapper;

import org.junit.jupiter.api.Test;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.dto.ShopUnitDto;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.dto.ShopUnitStatisticUnit;
import ru.yandex.backendschool.megamarket.entity.ShopHistory;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShopUnitMapperImplTest {


    private final ShopUnitMapper mapper = new ShopUnitMapperImpl();

    @Test
    void mapToShopUnit() {
        var time = ZonedDateTime.now();
        var unitImport = new ShopUnitImport(
                "id",
                "name",
                null,
                ShopUnitType.OFFER,
                1000L
        );
        var expectedUnit = new ShopUnit(
                "id",
                "name",
                time,
                null,
                ShopUnitType.OFFER,
                1000L,
                "id",
                1000L,
                1L,
                null
        );

        var actual = mapper.mapToShopUnit(unitImport, time);
        assertEquals(expectedUnit, actual);
    }

    @Test
    void mapToShopUnitDto() {
        var time = ZonedDateTime.now();
        var timeStr = time.withZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV"));
        var unit = new ShopUnit(
                "id",
                "name",
                time,
                null,
                ShopUnitType.OFFER,
                1000L,
                "id",
                1000L,
                1L,
                null
        );
        var expectedDto = new ShopUnitDto(
                "id",
                "name",
                timeStr,
                null,
                ShopUnitType.OFFER,
                1000L,
                null
        );

        var actual = mapper.mapToShopUnitDto(unit);
        assertEquals(expectedDto, actual);
    }

    @Test
    void mapToShopHistory() {
        var time = ZonedDateTime.now();
        var unit = new ShopUnit(
                "id",
                "name",
                time,
                null,
                ShopUnitType.OFFER,
                1000L,
                "id",
                1000L,
                1L,
                null
        );
        var expectedHistory = new ShopHistory(
                "id",
                "name",
                time,
                null,
                ShopUnitType.OFFER,
                1000L
        );
        var actual = mapper.mapToShopHistory(unit);
        assertEquals(expectedHistory, actual);
    }

    @Test
    void mapToShopUnitStatisticUnit() {
        var time = ZonedDateTime.now();
        var timeStr = mapper.mapDateToString(time);
        var history = new ShopHistory(
                "id",
                "name",
                time,
                null,
                ShopUnitType.OFFER,
                1000L
        );
        var expectedStatistic = new ShopUnitStatisticUnit(
                "id",
                "name",
                null,
                ShopUnitType.OFFER,
                1000L,
                timeStr
        );
        var actual = mapper.mapToShopUnitStatisticUnit(history);
        assertEquals(expectedStatistic, actual);
    }
}