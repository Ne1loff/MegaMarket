package ru.yandex.backendschool.megamarket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.dto.ShopUnitDto;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImport;
import ru.yandex.backendschool.megamarket.dto.ShopUnitImportRequest;
import ru.yandex.backendschool.megamarket.dto.ShopUnitStatisticResponse;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;
import ru.yandex.backendschool.megamarket.exception.badRequest.ValidationError;
import ru.yandex.backendschool.megamarket.exception.notFound.ItemNotFoundError;
import ru.yandex.backendschool.megamarket.mapper.ShopUnitMapper;
import ru.yandex.backendschool.megamarket.repository.ShopHistoryRepository;
import ru.yandex.backendschool.megamarket.repository.ShopUnitsRepository;
import ru.yandex.backendschool.megamarket.validator.ShopUnitValidator;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ShopService {

    private final ShopUnitsRepository shopUnitsRepository;
    private final ShopHistoryRepository shopHistoryRepository;
    private final ShopUnitMapper mapper;
    private final ShopUnitValidator validator;

    public ShopService(ShopUnitsRepository shopUnitsRepository, ShopHistoryRepository shopHistoryRepository,
                       ShopUnitMapper mapper, ShopUnitValidator validator
    ) {
        this.shopUnitsRepository = shopUnitsRepository;
        this.shopHistoryRepository = shopHistoryRepository;
        this.mapper = mapper;
        this.validator = validator;
    }

    private void setUnitPrice(ShopUnit unit) {
        var count = unit.getChildren().stream().mapToLong(ShopUnit::getTotalChildrenOfferCount).sum();
        var sum = unit.getChildren().stream().mapToLong(ShopUnit::getSumOfChildrenPrice).sum();

        unit.setTotalChildrenOfferCount(count);
        unit.setSumOfChildrenPrice(sum);
        unit.setPrice(count == 0 ? null : sum / count);
    }

    private void updateRootPrice(ShopUnit unit) {
        Stack<ShopUnit> units = new Stack<>();
        Set<String> secondPush = new HashSet<>();

        var children = unit.getChildren().stream()
                .filter(it -> it.getType() == ShopUnitType.CATEGORY)
                .toList();

        units.addAll(children);

        while (!units.isEmpty()) {
            var child = units.pop();

            children = child.getChildren().stream()
                    .filter(it -> it.getType() == ShopUnitType.CATEGORY)
                    .toList();

            if (children.isEmpty() || secondPush.contains(child.getId())) {
                setUnitPrice(child);
            } else {
                secondPush.add(units.push(child).getId());
                units.addAll(children);
            }
        }

        setUnitPrice(unit);
    }

    private void updateNewUnits(Map<String, ShopUnit> unitsMap, ShopUnit parent, List<ShopUnit> values) {

        if (parent.getType() == ShopUnitType.OFFER) throw new ValidationError();

        values.forEach(it -> {
            parent.getChildren().add(it);
            it.setRootId(parent.getRootId());
            it.setParentId(parent.getId());
            unitsMap.put(it.getId(), it);
        });
    }

    private List<ShopUnit> getAllUpdatedUnitsAndUpdateDate(Map<String, ShopUnit> unitsMap, List<ShopUnit> updatedUnits) {
        var updatedDate = updatedUnits.get(0).getDate();
        var allUpdatedUnits = new ArrayList<ShopUnit>();
        var parentIds = updatedUnits.stream()
                .filter(ShopUnit::haveParentId)
                .map(ShopUnit::getParentId)
                .distinct()
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
        while (!(parentIds.isEmpty())) {
            var parent = unitsMap.get(parentIds.poll());
            parent.setDate(updatedDate);

            var parentId = parent.getParentId();
            if (parentId != null && !parentIds.contains(parentId)) {
                parentIds.add(parentId);
            }
            allUpdatedUnits.add(parent);
        }
        allUpdatedUnits.addAll(updatedUnits);
        return allUpdatedUnits.stream().distinct().toList();
    }

    @Transactional
    void addOrUpdateShopUnits(
            List<ShopUnit> newUnits, List<ShopUnitImport> importUnitsForUpdateUnits, ZonedDateTime updateTime
    ) {
        var idsForUpdate = importUnitsForUpdateUnits.parallelStream()
                .map(ShopUnitImport::id)
                .toList();

        Map<String, List<ShopUnit>> unitsRootIdsMap = new HashMap<>();
        if (!(idsForUpdate.isEmpty()))
            unitsRootIdsMap = shopUnitsRepository.getShopUnitsByIds(idsForUpdate).stream()
                    .collect(Collectors.groupingBy(ShopUnit::getRootId));

        Set<ShopUnit> allUnitsForUpdate = new HashSet<>();
        if (!(unitsRootIdsMap.keySet().isEmpty()))
            allUnitsForUpdate = new HashSet<>(shopUnitsRepository.getShopUnitsByRootIds(unitsRootIdsMap.keySet()));

        var groupedNewUnits = newUnits.stream()
                .collect(Collectors.groupingBy(ShopUnit::haveParentId));

        boolean haveParent = true;

        allUnitsForUpdate.addAll(unitsRootIdsMap.values()
                .stream()
                .flatMap(List::stream)
                .toList()
        );

        if (groupedNewUnits.containsKey(!(haveParent)))
            allUnitsForUpdate.addAll(groupedNewUnits.get(!(haveParent)));

        var unitsMap = allUnitsForUpdate.stream()
                .collect(Collectors.toMap(ShopUnit::getId, it -> it));

        if (groupedNewUnits.containsKey(haveParent)) {
            var groupedUnitsByType = groupedNewUnits.get(haveParent).stream()
                    .collect(Collectors.groupingBy(ShopUnit::getType, Collectors.groupingBy(ShopUnit::getParentId)));

            var parentIds = groupedUnitsByType.values().stream()
                    .map(Map::keySet)
                    .flatMap(Collection::stream)
                    .toList();

            var parentsIdsForAdd = parentIds.stream()
                    .filter(it -> !unitsMap.containsKey(it))
                    .toList();

            var rootIds = shopUnitsRepository.getRootIdsByIds(parentsIdsForAdd);
            shopUnitsRepository.getShopUnitsByRootIds(rootIds)
                    .forEach(it -> unitsMap.put(it.getId(), it));


            groupedUnitsByType.getOrDefault(ShopUnitType.CATEGORY, Collections.emptyMap())
                    .forEach((parentId, values) -> {
                        var parent = Optional.ofNullable(unitsMap.get(parentId))
                                .orElseThrow(ValidationError::new);
                        updateNewUnits(unitsMap, parent, values);
                    });

            groupedUnitsByType.getOrDefault(ShopUnitType.OFFER, Collections.emptyMap())
                    .forEach((parentId, values) -> {
                        var parent = Optional.ofNullable(unitsMap.get(parentId))
                                .orElseThrow(ValidationError::new);
                        updateNewUnits(unitsMap, parent, values);

                        var sumOfPrice = values.stream()
                                .mapToLong(ShopUnit::getSumOfChildrenPrice)
                                .sum();
                        var totalCountOfUnits = values.size();

                        parent.setSumOfChildrenPrice(parent.getSumOfChildrenPrice() + sumOfPrice);
                        parent.setTotalChildrenOfferCount(parent.getTotalChildrenOfferCount() + totalCountOfUnits);

                        shopUnitsRepository.saveAll(values);
                    });

        }

        var importUnitsIsHaveParent = importUnitsForUpdateUnits.stream()
                .collect(Collectors.groupingBy(ShopUnitImport::haveParentId));

        importUnitsIsHaveParent.getOrDefault(haveParent, Collections.emptyList()).stream()
                .collect(Collectors.groupingBy(ShopUnitImport::parentId))
                .forEach((parentId, imports) -> {
                    var parent = unitsMap.get(parentId);

                    if (parent.getType() == ShopUnitType.OFFER) throw new ValidationError();

                    imports.forEach(importUnit -> {
                        var unit = unitsMap.get(importUnit.id());

                        unit.setName(importUnit.name());
                        unit.setDate(updateTime);

                        boolean unitIsOffer = unit.getType() == ShopUnitType.OFFER;

                        if (unitIsOffer) {
                            parent.setSumOfChildrenPrice(parent.getSumOfChildrenPrice() - unit.getSumOfChildrenPrice());
                            unit.setPrice(importUnit.price());
                            unit.setSumOfChildrenPrice(unit.getPrice());
                            parent.setSumOfChildrenPrice(parent.getSumOfChildrenPrice() + unit.getSumOfChildrenPrice());
                        }

                        unit.setRootId(parent.getRootId());

                        if (!(Objects.equals(unit.getParentId(), parentId))) {
                            var oldParent = unitsMap.get(unit.getParentId()); // 100% have parent

                            oldParent.setTotalChildrenOfferCount(
                                    oldParent.getTotalChildrenOfferCount() - unit.getTotalChildrenOfferCount());
                            oldParent.setSumOfChildrenPrice(
                                    oldParent.getSumOfChildrenPrice() - unit.getSumOfChildrenPrice());

                            oldParent.getChildren().remove(unit);
                            parent.getChildren().add(unit);
                            unit.setParentId(parentId);
                        }
                    });
                });

        importUnitsIsHaveParent.getOrDefault(!haveParent, Collections.emptyList())
                .forEach(it -> {
                    var unit = unitsMap.get(it.id());

                    unit.setName(it.name());

                    if (!(Objects.equals(it.parentId(), unit.getParentId()))) {
                        var oldParent = unitsMap.get(unit.getParentId());
                        oldParent.getChildren().remove(unit);

                        var sum = oldParent.getSumOfChildrenPrice();
                        var count = oldParent.getTotalChildrenOfferCount();
                        oldParent.setSumOfChildrenPrice(sum - unit.getSumOfChildrenPrice());
                        oldParent.setTotalChildrenOfferCount(count - unit.getTotalChildrenOfferCount());
                    }
                });

        unitsMap.values().stream()
                .filter(it -> it.getParentId() == null)
                .forEach(this::updateRootPrice);

        var updatedUnitsForStatistic = Stream.concat(newUnits.stream(),
                importUnitsForUpdateUnits.stream().map(it -> unitsMap.get(it.id()))
        ).toList();

        var allUpdatedUnits = getAllUpdatedUnitsAndUpdateDate(unitsMap, updatedUnitsForStatistic).stream()
                .map(mapper::mapToShopHistory).toList();

        shopHistoryRepository.saveAll(allUpdatedUnits);
        shopUnitsRepository.saveAll(unitsMap.values());
    }

    public void importProducts(ShopUnitImportRequest importRequest) {
        var ids = shopUnitsRepository.getAllIds();
        var items = importRequest.items();

        var updateTime = validator
                .validateDateAndGet(importRequest.updateDate());


        items.forEach(validator::validateShopUnitImport);

        List<ShopUnit> newUnits = new ArrayList<>();
        List<ShopUnitImport> importUnitsForUpdate = new ArrayList<>();

        items.forEach(it -> {
            if (ids.contains(it.id())) {
                importUnitsForUpdate.add(it);
            } else {
                newUnits.add(mapper.mapToShopUnit(it, updateTime));
            }
        });

        addOrUpdateShopUnits(newUnits, importUnitsForUpdate, updateTime);
    }

    public ShopUnitDto getShopUnitById(String id) {
        if (validator.isInvalidUuid(id)) throw new ValidationError();
        return shopUnitsRepository.getShopUnitById(id).map(mapper::mapToShopUnitDto).orElseThrow(ItemNotFoundError::new);
    }

    @Transactional
    public void deleteShopUnit(String id) {
        if (validator.isInvalidUuid(id)) throw new ValidationError();
        var shopUnit = shopUnitsRepository.getShopUnitById(id).orElseThrow(ItemNotFoundError::new);

        if (shopUnit.haveParentId()) {
            var parents = shopUnitsRepository.getShopUnitsByIds(
                            List.of(shopUnit.getParentId(), shopUnit.getRootId())
                    ).stream()
                    .collect(Collectors.toMap(ShopUnit::getId, it -> it));

            ShopUnit parent = Optional.ofNullable(parents.get(shopUnit.getParentId()))
                    .orElseThrow(ValidationError::new);
            ShopUnit rootUnit = Optional.ofNullable(parents.get(shopUnit.getRootId()))
                    .orElseThrow(ValidationError::new);

            parent.getChildren().remove(shopUnit);
            parent.setSumOfChildrenPrice(parent.getSumOfChildrenPrice() - shopUnit.getSumOfChildrenPrice());
            parent.setTotalChildrenOfferCount(parent.getTotalChildrenOfferCount() - shopUnit.getTotalChildrenOfferCount());

            updateRootPrice(rootUnit);
        }

        Stack<ShopUnit> children = new Stack<>();
        List<String> idsForDelete = new ArrayList<>();

        idsForDelete.add(shopUnit.getId());
        children.addAll(shopUnit.getChildren());

        while (!children.isEmpty()) {
            var child = children.pop();
            children.addAll(child.getChildren());
            idsForDelete.add(child.getId());
        }

        shopUnitsRepository.deleteAllByIds(idsForDelete);
    }

    public ShopUnitStatisticResponse getLastOffersStatistic(String date) {
        var dateEnd = validator.validateDateAndGet(date);
        var dateStart = dateEnd.minusDays(1);

        var offers = shopHistoryRepository
                .getShopHistoriesByTypeAndDateBetween(ShopUnitType.OFFER, dateStart, dateEnd)
                .stream().map(mapper::mapToShopUnitStatisticUnit)
                .toList();

        ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return new ShopUnitStatisticResponse().setItems(offers);
    }

    public ShopUnitStatisticResponse getShopUnitStatistic(String id, String dateStartStr, String dateEndStr) {
        if (validator.isInvalidUuid(id)) throw new ValidationError();

        var dateStart = validator.validateDateAndGet(dateStartStr);
        var dateEnd = dateEndStr.isEmpty() ? ZonedDateTime.now() : validator.validateDateAndGet(dateEndStr);

        var units = shopHistoryRepository
                .getShopHistoriesByShopUnitIdAndIsNotDeletedAndDateBetween(id, dateStart, dateEnd)
                .stream().map(mapper::mapToShopUnitStatisticUnit)
                .toList();

        return new ShopUnitStatisticResponse().setItems(units);
    }

}
