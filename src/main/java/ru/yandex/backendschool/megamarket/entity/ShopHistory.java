/* TODO: Thing about it.
package ru.yandex.backendschool.megamarket.entity;

import lombok.*;
import org.hibernate.Hibernate;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
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

    @NotNull
    String shopUnitId;

    @NotNull
    Date date;

    String parentId;

    @NotNull
    ShopUnitType type;

    Long price;

    @NotNull
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

*/
