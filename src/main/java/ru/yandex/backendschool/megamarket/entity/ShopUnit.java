package ru.yandex.backendschool.megamarket.entity;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ShopUnit {

    public ShopUnit(String id,
                    String name,
                    Date date,
                    String parentId,
                    ShopUnitType type,
                    Long price,
                    ShopUnit parent,
                    List<ShopUnit> children
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.parentId = parentId;
        this.type = type;
        this.price = price;
        this.parent = parent;
        this.children = children;
    }

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    String id;

    @NotNull
    String name;

    @NotNull
    Date date;

    String parentId;

    @NotNull
    ShopUnitType type;

    Long price;

    @ManyToOne()
    ShopUnit parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @ToString.Exclude
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
