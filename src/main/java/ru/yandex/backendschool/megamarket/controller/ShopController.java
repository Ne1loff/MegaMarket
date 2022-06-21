package ru.yandex.backendschool.megamarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.yandex.backendschool.megamarket.dto.ShopUnitDto;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImportRequest;
import ru.yandex.backendschool.megamarket.dto.ShopUnitStatisticResponse;
import ru.yandex.backendschool.megamarket.service.ShopService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ShopController {

    ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/imports")
    ResponseEntity<HttpStatus> importProducts(@RequestBody ShopUnitImportRequest importRequest) {
        shopService.importProducts(importRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    ResponseEntity<HttpStatus> deleteShopUnit(@PathVariable String id) {
        shopService.deleteShopUnit(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nodes/{id}")
    ResponseEntity<ShopUnitDto> getShopUnitInfo(@PathVariable String id) {
        return ResponseEntity.ok(shopService.getShopUnitById(id));
    }

    @GetMapping("/sales")
    ResponseEntity<ShopUnitStatisticResponse> getSales(@RequestParam String date) {
        return ResponseEntity.ok(shopService.getLastOffersStatistic(date));
    }

    @GetMapping("/node/{id}/statistic")
    ResponseEntity<ShopUnitStatisticResponse> getShopUnitStatistic(@PathVariable String id,
                                                                   @RequestParam String dateStart,
                                                                   @RequestParam(defaultValue = "") String dateEnd
    ) {
        return ResponseEntity.ok(shopService.getShopUnitStatistic(id, dateStart, dateEnd));
    }

}
