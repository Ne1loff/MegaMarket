package ru.yandex.backendschool.megamarket.controller;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.yandex.backendschool.megamarket.dto.ShopUnitDto;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImportRequest;
import ru.yandex.backendschool.megamarket.dto.ShopUnitStatisticResponse;
import ru.yandex.backendschool.megamarket.exception.tooManyRequest.TooManyRequestError;
import ru.yandex.backendschool.megamarket.service.ShopService;

import java.time.Duration;

@Controller
public class ShopController {

    private final ShopService shopService;
    private final Bucket statisticLimiter;
    private final Bucket importDeleteLimiter;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;

        Bandwidth limit = Bandwidth.simple(100, Duration.ofSeconds(1));
        this.statisticLimiter = Bucket.builder()
                .addLimit(limit)
                .build();

        limit = Bandwidth.simple(1000, Duration.ofMinutes(1));
        this.importDeleteLimiter = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @PostMapping("/imports")
    ResponseEntity<HttpStatus> importProducts(@RequestBody ShopUnitImportRequest importRequest) {
        if (importDeleteLimiter.tryConsume(importRequest.items().size())) {
            shopService.importProducts(importRequest);
            return ResponseEntity.ok().build();
        }
        throw new TooManyRequestError();
    }

    @DeleteMapping("/delete/{id}")
    ResponseEntity<HttpStatus> deleteShopUnit(@PathVariable String id) {
        shopService.deleteShopUnit(id, importDeleteLimiter);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nodes/{id}")
    ResponseEntity<ShopUnitDto> getShopUnitInfo(@PathVariable String id) {
        if (statisticLimiter.tryConsume(1)) {
            return ResponseEntity.ok(shopService.getShopUnitById(id));
        }
        throw new TooManyRequestError();
    }

    @GetMapping("/sales")
    ResponseEntity<ShopUnitStatisticResponse> getSales(@RequestParam String date) {
        if (statisticLimiter.tryConsume(1)) {
            return ResponseEntity.ok(shopService.getLastOffersStatistic(date));
        }
        throw new TooManyRequestError();
    }

    @GetMapping("/node/{id}/statistic")
    ResponseEntity<ShopUnitStatisticResponse> getShopUnitStatistic(@PathVariable String id,
                                                                   @RequestParam String dateStart,
                                                                   @RequestParam(defaultValue = "") String dateEnd
    ) {
        if (statisticLimiter.tryConsume(1)) {
            return ResponseEntity.ok(shopService.getShopUnitStatistic(id, dateStart, dateEnd));
        }
        throw new TooManyRequestError();
    }

}
