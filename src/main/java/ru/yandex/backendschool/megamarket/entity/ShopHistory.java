package ru.yandex.backendschool.megamarket.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ShopHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(nullable = false)
    String shopUnitId;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    Date date;

    String parentId;

    @Column(nullable = false)
    ShopUnitType type;

    Long price;

    @Column(nullable = false)
    Boolean isDeleted;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ShopHistory that = (ShopHistory) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
