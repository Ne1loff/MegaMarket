package ru.yandex.backendschool.megamarket.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShopUnitStatisticResponse {
    List<ShopUnitStatisticUnit> items;

    public ShopUnitStatisticResponse setItems(List<ShopUnitStatisticUnit> items) {
        this.items = items;
        return this;
    }
}
