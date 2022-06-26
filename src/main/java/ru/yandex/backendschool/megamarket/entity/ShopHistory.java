package ru.yandex.backendschool.megamarket.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import ru.yandex.backendschool.megamarket.dataEnum.ShopUnitType;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ShopHistory {

    public ShopHistory(String shopUnitId, String name, ZonedDateTime date,
                       String parentId, ShopUnitType type, Long price
    ) {
        this.id = -1L;
        this.shopUnitId = shopUnitId;
        this.name = name;
        this.date = date;
        this.parentId = parentId;
        this.type = type;
        this.price = price;
        this.isDeleted = false;
    }

    @Id
    @SequenceGenerator(name = "history_seq",
            sequenceName = "history_sequence",
            allocationSize = 20)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_seq")
    private Long id;

    @Column(nullable = false)
    private String shopUnitId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private ZonedDateTime date;

    private String parentId;

    @Column(nullable = false)
    private ShopUnitType type;

    private Long price;

    @Column(nullable = false)
    private Boolean isDeleted;

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
