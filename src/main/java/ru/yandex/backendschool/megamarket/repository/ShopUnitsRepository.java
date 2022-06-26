package ru.yandex.backendschool.megamarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.backendschool.megamarket.dto.ShopUnitIdsTupleDto;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ShopUnitsRepository extends JpaRepository<ShopUnit, String> {

    Optional<ShopUnit> getShopUnitById(String id);

    List<ShopUnit> getShopUnitsByRootId(String rootId);

    @Modifying(flushAutomatically = true)
    @Query(value = "DELETE FROM ShopUnit WHERE id IN :ids")
    void deleteAllByIds(@Param("ids") Collection<String> ids);

    @Query(value = "SELECT DISTINCT su.parentId FROM ShopUnit su WHERE su.parentId is NOT NULL AND su.id IN :ids")
    Set<String> getParentIdsByIds(@Param("ids") Collection<String> ids);

    @Query(value = "SELECT DISTINCT su.rootId FROM ShopUnit su WHERE su.id IN :ids ")
    Set<String> getRootIdsByIds(@Param("ids") Collection<String> ids);

    @Query(value = "SELECT new ru.yandex.backendschool.megamarket.dto.ShopUnitIdsTupleDto(id, parentId, rootId) FROM ShopUnit WHERE rootId IN :rootIds")
    List<ShopUnitIdsTupleDto> getShopUnitsIdsTuples(@Param("rootIds") Collection<String> rootIds);

    @Query(value = "SELECT su FROM ShopUnit su WHERE su.rootId IN :rootIds")
    List<ShopUnit> getShopUnitsByRootIds(@Param("rootIds") Collection<String> rootIds);

    @Query(value = "SELECT id FROM ShopUnit")
    List<String> getAllIds();

    @Query(value = "SELECT su FROM ShopUnit su WHERE su.id IN :ids")
    List<ShopUnit> getShopUnitsByIds(@Param("ids") Collection<String> ids);
}
