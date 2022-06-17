package ru.yandex.backendschool.megamarket.dto;

import lombok.Data;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class ShopUnitStatisticUnit {

    @NotNull
    String id;

    @NotNull
    String name;

    String parentId;

    @NotNull
    ShopUnitType type;

    Long price;

    @NotNull
    Date date;
}
