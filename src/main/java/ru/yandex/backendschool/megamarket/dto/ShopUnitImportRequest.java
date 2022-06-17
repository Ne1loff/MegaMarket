package ru.yandex.backendschool.megamarket.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class ShopUnitImportRequest {

    @NotNull
    List<ShopUnitImport> items;

    @NotNull
    String updateDate;
}
