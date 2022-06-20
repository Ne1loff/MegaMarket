package ru.yandex.backendschool.megamarket.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.*;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import java.util.Date;
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
            Date date,
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
    String id;

    String name;

    Date date;

    @Column(name = "pid")
    String parentId;

    ShopUnitType type;

    Long price;

    String rootId;

    Long sumOfChildrenPrice;

    Long totalChildrenOfferCount;

    @ToString.Exclude
    @BatchSize(size = 10)
    @NotFound(action = NotFoundAction.IGNORE)
    @OneToMany(mappedBy = "parentId", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    List<ShopUnit> children;

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
