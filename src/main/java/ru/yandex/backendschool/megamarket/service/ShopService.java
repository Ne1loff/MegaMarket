package ru.yandex.backendschool.megamarket.service;

import io.github.bucket4j.Bucket;
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
import ru.yandex.backendschool.megamarket.exception.tooManyRequest.TooManyRequestError;
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

    private void addNewUnitsAndUpdateParents(Collection<ShopUnit> newUnits, Map<String, ShopUnit> unitsMap) {
        var newUnitsGroupedByParentId = newUnits.stream()
                .filter(ShopUnit::haveParentId)
                .collect(Collectors.groupingBy(ShopUnit::getParentId));

        newUnitsGroupedByParentId.forEach((parentId, values) -> {
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
        });
    }

    private void updateUnits(Collection<ShopUnitImport> updatedImports, Map<String, ShopUnit> unitsMap, ZonedDateTime updateTime) {
        updatedImports.forEach(it -> {
            var parent = Optional.ofNullable(unitsMap.get(it.parentId()))
                    .orElseThrow(ValidationError::new);
            if (parent.isOffer()) throw new ValidationError();
            var unit = Optional.ofNullable(unitsMap.get(it.id()))
                    .orElseThrow(ValidationError::new);

            unit.setName(it.name());

            if (Objects.equals(it.parentId(), unit.getParentId())) {
                var oldParent = Optional.ofNullable(unitsMap.get(unit.getParentId()))
                        .orElseThrow(ValidationError::new);
                oldParent.removeFromChildren(unit);
                parent.addToChild(unit);
                unit.setRootId(parent.getRootId());
            }

            if (unit.isOffer()) {
                unit.updatePrices(it.price());
            }
        });
    }

    @Transactional
    void addOrUpdateShopUnits(
            Collection<ShopUnit> newUnits, Collection<ShopUnit> unitsForUpdate, Collection<ShopUnitImport> updatedImports, ZonedDateTime updateTime
    ) {
        var parentIds = Stream.concat(newUnits.stream(), unitsForUpdate.stream())
                .map(ShopUnit::getParentId).collect(Collectors.toSet());

        var rootIds = !parentIds.isEmpty() ?
                shopUnitsRepository.getRootIdsByIds(parentIds) : new ArrayList<String>();
        var allUnitsByRootIds = !rootIds.isEmpty() ?
                shopUnitsRepository.getShopUnitsByRootIds(rootIds) : new ArrayList<ShopUnit>();

        allUnitsByRootIds.addAll(newUnits);

        var unitsMap = allUnitsByRootIds.stream()
                .collect(Collectors.toMap(ShopUnit::getId, it -> it));

        addNewUnitsAndUpdateParents(newUnits, unitsMap);
        updateUnits(updatedImports, unitsMap, updateTime);

        var units = unitsMap.values();

        units.stream()
                .filter(it -> !it.haveParentId())
                .forEach(this::updateRootPrice);

        var updatedUnitsForStatistic = Stream.concat(
                newUnits.stream(), unitsForUpdate.stream()
        ).toList();
        var unitsForHistory = getAllUpdatedUnitsAndUpdateDate(
                unitsMap, updatedUnitsForStatistic, updateTime
        ).stream()
                .map(mapper::mapToShopHistory).toList();

        shopHistoryRepository.saveAll(unitsForHistory);
        shopUnitsRepository.saveAll(units);
    }

    public void importProducts(ShopUnitImportRequest importRequest) {
        var items = importRequest.items();
        items.forEach(validator::validateShopUnitImport);
        var updateTime = validator.validateDateAndGet(importRequest.updateDate());

        var ids = items.stream()
                .map(ShopUnitImport::id).toList();
        var unitsForUpdate = shopUnitsRepository.getShopUnitsByIds(ids).stream()
                .collect(Collectors.toMap(ShopUnit::getId, it -> it));
        var unitsIdForUpdate = unitsForUpdate.keySet();

        var unitImportsUpdated = new ConcurrentLinkedQueue<ShopUnitImport>();
        var newUnits = new ConcurrentLinkedQueue<ShopUnit>();

        items.parallelStream().forEach(it -> {
            if (unitsIdForUpdate.contains(it.id())) {
                unitImportsUpdated.add(it);
            } else {
                newUnits.add(mapper.mapToShopUnit(it, updateTime));
            }
        });

        addOrUpdateShopUnits(newUnits, unitsForUpdate.values(), unitImportsUpdated, updateTime);
    }

    public ShopUnitDto getShopUnitById(String id) {
        if (validator.isInvalidUuid(id)) throw new ValidationError();
        return shopUnitsRepository.getShopUnitById(id).map(mapper::mapToShopUnitDto).orElseThrow(ItemNotFoundError::new);
    }

    @Transactional
    public void deleteShopUnit(String id, Bucket limiter) {
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
        if (limiter.tryConsume(idsForDelete.size())) {
            shopUnitsRepository.deleteAllByIds(idsForDelete);
        }
        throw new TooManyRequestError();
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
