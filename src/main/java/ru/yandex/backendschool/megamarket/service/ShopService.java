package ru.yandex.backendschool.megamarket.service;

import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;
import ru.yandex.backendschool.megamarket.dto.*;
import ru.yandex.backendschool.megamarket.entity.ShopUnit;
import ru.yandex.backendschool.megamarket.exception.badRequest.ValidationError;
import ru.yandex.backendschool.megamarket.exception.notFound.ItemNotFoundError;
import ru.yandex.backendschool.megamarket.exception.tooManyRequest.TooManyRequestError;
import ru.yandex.backendschool.megamarket.mapper.ShopUnitMapper;
import ru.yandex.backendschool.megamarket.repository.ShopHistoryRepository;
import ru.yandex.backendschool.megamarket.repository.ShopUnitsRepository;
import ru.yandex.backendschool.megamarket.util.Tuple;
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

    private void updateRootPrice(ShopUnit unit, Map<String, ShopUnit> unitsMap, Map<String, Tuple<Long, Long>> pricesMap) {
        var unitsMappedByParentId = unitsMap.values().stream()
                .filter(ShopUnit::haveParentId)
                .collect(Collectors.groupingBy(ShopUnit::getParentId));

        Stack<ShopUnit> units = new Stack<>();
        Set<String> secondAdd = new HashSet<>();
        units.add(unit);
        while (!units.isEmpty()) {
            var shopUnit = units.pop();
            if (unitsMappedByParentId.containsKey(shopUnit.getId()) && !secondAdd.contains(shopUnit.getId())) {
                secondAdd.add(shopUnit.getId());
                units.add(shopUnit);
                units.addAll(unitsMappedByParentId.get(shopUnit.getId()));
            } else if (shopUnit.haveParentId()) {
                var parent = unitsMap.get(shopUnit.getParentId());
                var tuple = pricesMap.get(shopUnit.getId());
                var oldSumOfPrices = tuple != null ? tuple.first() : 0;
                var oldTotalCount = tuple != null ? tuple.second() : 0;

                var sum = shopUnit.getSumOfChildrenPrices();
                var count = shopUnit.getTotalChildrenOfferCount();

                parent.minusSumOfChildrenPrice(oldSumOfPrices)
                        .plusSumOfChildrenPrice(sum);
                parent.minusTotalChildrenOfferCount(oldTotalCount)
                        .plusTotalChildrenOfferCount(count);

                var currentSum = parent.getSumOfChildrenPrices();
                var currentCount = parent.getTotalChildrenOfferCount();
                parent.setPrice(currentCount == 0 ? null : currentSum / currentCount);
            }
        }
    }

    private List<ShopUnit> getAllUpdatedUnitsAndUpdateDate(Map<String, ShopUnit> unitsMap, Collection<ShopUnit> updatedUnits, ZonedDateTime updatedDate) {
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

    private void addShopUnits(Collection<ShopUnit> newUnits, Map<String, ShopUnit> unitsMap) {
        var newUnitsGroupedByParentId = newUnits.stream()
                .filter(ShopUnit::haveParentId)
                .collect(Collectors.groupingBy(ShopUnit::getParentId));

        for (var entry : newUnitsGroupedByParentId.entrySet()) {
            String parentId = entry.getKey();
            List<ShopUnit> values = entry.getValue();
            var parent = Optional.ofNullable(unitsMap.get(parentId))
                    .orElseThrow(ValidationError::new);
            if (parent.isOffer()) throw new ValidationError();

            parent.addAllToChild(values);
            String rootId = parent.getRootId();

            if (validator.isInvalidUuid(rootId)) {
                var nextParent = parent;
                while (nextParent.haveParentId()) {
                    nextParent = Optional.ofNullable(unitsMap.get(nextParent.getParentId()))
                            .orElseThrow(ValidationError::new);
                    rootId = nextParent.getRootId();
                }
            }

            for (var unit : values) {
                unit.setRootId(rootId);
            }
        }
    }

    private void updateShopUnits(Collection<ShopUnitImport> updatedImports, Map<String, ShopUnit> unitsMap, ZonedDateTime updateTime) {
        for (ShopUnitImport unitUpdateReq : updatedImports) {
            var unitEntity = Optional.ofNullable(unitsMap.get(unitUpdateReq.id()))
                    .orElseThrow(ValidationError::new);
            unitEntity.setDate(updateTime);
            unitEntity.setName(unitUpdateReq.name());
            if (unitEntity.isOffer()) {
                unitEntity.updatePrices(unitUpdateReq.price());
            }
            if (unitEntity.haveParentId()) {
                var parent = Optional.ofNullable(unitsMap.get(unitUpdateReq.parentId()))
                        .orElseThrow(ValidationError::new);
                if (parent.isOffer()) throw new ValidationError();


                if (!Objects.equals(unitUpdateReq.parentId(), unitEntity.getParentId())) {
                    var oldParent = Optional.ofNullable(unitsMap.get(unitEntity.getParentId()))
                            .orElseThrow(ValidationError::new);
                    oldParent.removeFromChildren(unitEntity);
                    parent.addToChild(unitEntity);
                    unitEntity.setRootId(parent.getRootId());
                }
            }
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void importProducts(ShopUnitImportRequest importRequest) {
        var updateTime = validator.validateDateAndGet(importRequest.updateDate());
        var unitsToImport = importRequest.items();
        unitsToImport.forEach(validator::validateShopUnitImport);

        var unitsToImportMap = unitsToImport.stream()
                .collect(Collectors.toMap(ShopUnitImport::id, it -> it));

        var ids = Stream.concat(
                unitsToImport.stream().map(ShopUnitImport::id),
                unitsToImport.stream().filter(ShopUnitImport::haveParentId)
                        .map(ShopUnitImport::parentId)
        ).collect(Collectors.toSet());
        var rootIds = shopUnitsRepository.getRootIdsByIds(ids);

        var idsTuplesMap = shopUnitsRepository.getShopUnitsIdsTuples(rootIds).stream()
                .collect(Collectors.toMap(ShopUnitIdsTupleDto::id, it -> it));
        List<ShopUnit> newUnits = new ArrayList<>();
        List<ShopUnitImport> updatedImports = new ArrayList<>();
        Set<String> toBeUpdatedShopUnitsIds = new HashSet<>();

        for (var id : ids) {
            var tupleDto = idsTuplesMap.get(id);
            if (tupleDto == null) {
                newUnits.add(mapper.mapToShopUnit(unitsToImportMap.get(id), updateTime));
            } else {
                var importUnit = unitsToImportMap.get(id);
                if (importUnit != null) {
                    updatedImports.add(importUnit);
                }
                toBeUpdatedShopUnitsIds.add(id);
                if (tupleDto.parentId() != null) {
                    var parent = Optional.ofNullable(idsTuplesMap.get(tupleDto.parentId()))
                            .orElseThrow(ValidationError::new);
                    toBeUpdatedShopUnitsIds.add(parent.id());
                    while (parent.parentId() != null) {
                        if (!toBeUpdatedShopUnitsIds.contains(parent.id())) {
                            toBeUpdatedShopUnitsIds.add(parent.id());
                            parent = Optional.ofNullable(idsTuplesMap.get(parent.parentId()))
                                    .orElseThrow(ValidationError::new);
                        } else {
                            break;
                        }

                    }

                }
            }
        }

        var unitsForUpdate = shopUnitsRepository.getShopUnitsByIds(toBeUpdatedShopUnitsIds);
        var pricesMap = unitsForUpdate.stream()
                .collect(Collectors.toMap(ShopUnit::getId,
                        it -> new Tuple<>(it.getSumOfChildrenPrices(), it.getTotalChildrenOfferCount())));
        var unitsMap = Stream.concat(unitsForUpdate.stream(), newUnits.stream())
                .collect(Collectors.toMap(ShopUnit::getId, it -> it));

        addShopUnits(newUnits, unitsMap);
        updateShopUnits(updatedImports, unitsMap, updateTime);

        var units = unitsMap.values();
        units.stream()
                .filter(it -> !it.haveParentId())
                .forEach(it -> updateRootPrice(it, unitsMap, pricesMap));

        var updatedUnitsForStatistic = Stream.concat(newUnits.stream(), unitsForUpdate.stream())
                .map(it -> unitsMap.get(it.getId()))
                .toList();
        var shopHistories = getAllUpdatedUnitsAndUpdateDate(
                unitsMap, updatedUnitsForStatistic, updateTime
        ).stream().map(mapper::mapToShopHistory)
                .toList();

        shopHistoryRepository.saveAll(shopHistories);
        shopUnitsRepository.saveAll(units);
    }

    public ShopUnitDto getShopUnitById(String id) {
        if (validator.isInvalidUuid(id)) {
            throw new ValidationError();
        }
        return shopUnitsRepository.getShopUnitById(id)
                .map(mapper::mapToShopUnitDto).orElseThrow(ItemNotFoundError::new);
    }

    @Transactional
    public void deleteShopUnit(String id, Bucket limiter) {
        if (validator.isInvalidUuid(id)) {
            throw new ValidationError();
        }

        var shopUnit = shopUnitsRepository.getShopUnitById(id).orElseThrow(ItemNotFoundError::new);

        if (shopUnit.haveParentId()) {
            var shopUnitsIdsTupleMap = shopUnitsRepository.getShopUnitsIdsTuples(
                            Collections.singleton(shopUnit.getRootId())).stream()
                    .collect(Collectors.toMap(ShopUnitIdsTupleDto::id, it -> it));
            var parentsIds = new ArrayList<String>();
            var tuple = shopUnitsIdsTupleMap.get(shopUnit.getParentId());
            parentsIds.add(tuple.id());
            while (tuple.parentId() != null) {
                tuple = shopUnitsIdsTupleMap.get(tuple.parentId());
                parentsIds.add(tuple.id());
            }
            var parents = shopUnitsRepository.getShopUnitsByIds(parentsIds).stream()
                    .collect(Collectors.toMap(ShopUnit::getId, it -> it));
            var parent = Optional.ofNullable(parents.get(shopUnit.getParentId()))
                    .orElseThrow(ValidationError::new);
            parent.getChildren().remove(shopUnit);
            var child = shopUnit;
            while (true) {
                var sum = child.getSumOfChildrenPrices();
                var count = child.getTotalChildrenOfferCount();
                parent.minusSumOfChildrenPrice(sum)
                        .minusTotalChildrenOfferCount(count);

                var currentSum = parent.getSumOfChildrenPrices();
                var currentCount = parent.getTotalChildrenOfferCount();

                parent.setPrice(currentCount == 0 ? null : currentSum / currentCount);
                if (parent.haveParentId()) {
                    child = parent;
                    parent = Optional.ofNullable(parents.get(shopUnit.getParentId()))
                            .orElseThrow(ValidationError::new);
                } else {
                    break;
                }
            }
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

        if (limiter.tryConsume(idsForDelete.size())) {
            shopHistoryRepository.markAsDeleted(idsForDelete);
            shopUnitsRepository.deleteAllByIds(idsForDelete);
        } else {
            throw new TooManyRequestError();
        }
    }

    public ShopUnitStatisticResponse getLastOffersStatistic(String date) {
        var dateEnd = validator.validateDateAndGet(date);
        var dateStart = dateEnd.minusDays(1);

        var offers = shopHistoryRepository
                .getShopHistoriesByTypeAndDateBetween(ShopUnitType.OFFER, dateStart, dateEnd).stream()
                .map(mapper::mapToShopUnitStatisticUnit)
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

        if (units.isEmpty()) {
            throw new ItemNotFoundError();
        }

        return new ShopUnitStatisticResponse().setItems(units);
    }

}
