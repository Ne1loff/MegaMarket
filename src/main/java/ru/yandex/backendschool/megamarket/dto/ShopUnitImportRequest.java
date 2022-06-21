package ru.yandex.backendschool.megamarket.dto;

import java.util.List;

public record ShopUnitImportRequest(
        List<ShopUnitImport> items,
        String updateDate
) { }
