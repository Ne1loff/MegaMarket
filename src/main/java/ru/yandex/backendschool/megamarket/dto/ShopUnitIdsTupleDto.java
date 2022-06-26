package ru.yandex.backendschool.megamarket.dto;

public record ShopUnitIdsTupleDto(
        String id,
        String parentId,
        String rootId
) {
}
