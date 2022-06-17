package ru.yandex.backendschool.megamarket.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.backendschool.megamarket.dto.ShopUnitDto;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;

import java.util.Collections;
import java.util.Date;

@Component(value = "MapperImpl")
public class ShopUnitMapperImpl implements ShopUnitMapper {
    @Override
    public ShopUnit mapToShopUnit(ShopUnitImport unitImport, Date date) {
        return new ShopUnit(
                unitImport.getId(),
                unitImport.getName(),
                date,
                unitImport.getParentId(),
                unitImport.getType(),
                unitImport.getPrice(),
                null,
                Collections.emptyList()
        );
    }

    @Override
    public ShopUnitDto mapToShopUnitDto(ShopUnit unit) {
        return null;
    }
}
