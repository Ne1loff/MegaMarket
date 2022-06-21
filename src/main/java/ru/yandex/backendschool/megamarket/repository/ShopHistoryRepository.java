package ru.yandex.backendschool.megamarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.entity.ShopHistory;

import java.time.ZonedDateTime;
import java.util.List;


@Repository
public interface ShopHistoryRepository extends JpaRepository<ShopHistory, Long> {

    List<ShopHistory> getShopHistoriesByTypeAndDateBetween(ShopUnitType type, ZonedDateTime after, ZonedDateTime before);

    @Query(value = "SELECT sh FROM ShopHistory sh WHERE sh.shopUnitId = :id AND sh.isDeleted = false AND sh.date BETWEEN :dateStart AND :dateEnd")
    List<ShopHistory> getShopHistoriesByShopUnitIdAndIsNotDeletedAndDateBetween(
            @Param("id") String id, @Param("dateStart") ZonedDateTime dateStart, @Param("dateEnd") ZonedDateTime dateEnd);
}
