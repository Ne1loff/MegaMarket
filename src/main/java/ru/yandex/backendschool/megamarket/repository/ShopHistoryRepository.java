package ru.yandex.backendschool.megamarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.entity.ShopHistory;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;


@Repository
public interface ShopHistoryRepository extends JpaRepository<ShopHistory, Long> {

    List<ShopHistory> getShopHistoriesByTypeAndDateBetween(ShopUnitType type, ZonedDateTime after, ZonedDateTime before);

    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE ShopHistory SET isDeleted = true WHERE shopUnitId IN :ids")
    void markAsDeleted(@Param("ids") Collection<String> ids);

    @Query(value = "SELECT sh FROM ShopHistory sh WHERE sh.shopUnitId = :id AND sh.isDeleted = false AND sh.date >= :dateStart AND sh.date < :dateEnd")
    List<ShopHistory> getShopHistoriesByShopUnitIdAndIsNotDeletedAndDateBetween(
            @Param("id") String id, @Param("dateStart") ZonedDateTime dateStart, @Param("dateEnd") ZonedDateTime dateEnd);
}
