package ru.yandex.backendschool.megamarket.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ShopUnit {

    public ShopUnit(
            String id,
            String name,
            ZonedDateTime date,
            String parentId,
            ShopUnitType type,
            Long price,
            String rootId,
            Long sumOfChildrenPrice,
            Long totalChildrenOfferCount,
            List<ShopUnit> children
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.parentId = parentId;
        this.type = type;
        this.price = price;
        this.rootId = rootId;
        this.sumOfChildrenPrice = sumOfChildrenPrice;
        this.totalChildrenOfferCount = totalChildrenOfferCount;
        this.children = children;
    }

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime date;

    private String parentId;

    @Column(nullable = false)
    private ShopUnitType type;

    private Long price;

    @Column(nullable = false)
    private String rootId;

    @Column(nullable = false)
    private Long sumOfChildrenPrice;

    @Column(nullable = false)
    private Long totalChildrenOfferCount;

    @ToString.Exclude
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    @OneToMany(mappedBy = "parentId", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    private List<ShopUnit> children;

    public boolean haveParentId() {
        return parentId != null;
    }

    public boolean isOffer() {
        return type == ShopUnitType.OFFER;
    }

    public boolean isCategory() {
        return type == ShopUnitType.CATEGORY;
    }

    public void addToChild(ShopUnit unit) {
        children.add(unit);
    }

    public void addAllToChild(Collection<ShopUnit> units) {
        children.addAll(units);
    }

    public void removeFromChildren(ShopUnit unit) {
        children.remove(unit);
    }

    public void plusSumOfChildrenPrice(Long price) {
        sumOfChildrenPrice += price;
    }

    public void minusSumOfChildrenPrice(Long price) {
        sumOfChildrenPrice -= price;
    }

    public void updatePrices(Long price) {
        this.price = price;
        this.sumOfChildrenPrice = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ShopUnit shopUnit = (ShopUnit) o;
        return id != null && Objects.equals(id, shopUnit.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
