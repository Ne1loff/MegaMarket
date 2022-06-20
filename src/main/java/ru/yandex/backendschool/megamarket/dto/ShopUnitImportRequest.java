package ru.yandex.backendschool.megamarket.dto;

import lombok.Data;

import java.util.List;

@Data
public class ShopUnitImportRequest {

    List<ShopUnitImport> items;

    String updateDate;
}
