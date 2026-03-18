package com.ticketinglab.show.domain;

import com.ticketinglab.event.domain.Show;
import com.ticketinglab.venue.domain.Section;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(ShowSectionInventoryId.class)
@Table(name = "show_section_inventories")
public class ShowSectionInventory {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "sold_qty", nullable = false)
    private int soldQty;

    @Column(name = "hold_qty", nullable = false)
    private int holdQty;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    public static ShowSectionInventory create(
            Show show,
            Section section,
            int price,
            int capacity,
            int soldQty,
            int holdQty
    ) {
        return ShowSectionInventory.builder()
                .show(show)
                .section(section)
                .price(price)
                .capacity(capacity)
                .soldQty(soldQty)
                .holdQty(holdQty)
                .build();
    }

    public int remainingQuantity() {
        return Math.max(capacity - soldQty - holdQty, 0);
    }

    @Builder
    public ShowSectionInventory(
            Show show,
            Section section,
            int price,
            int capacity,
            int soldQty,
            int holdQty,
            int version
    ) {
        this.show = show;
        this.section = section;
        this.price = price;
        this.capacity = capacity;
        this.soldQty = soldQty;
        this.holdQty = holdQty;
        this.version = version;
    }
}
