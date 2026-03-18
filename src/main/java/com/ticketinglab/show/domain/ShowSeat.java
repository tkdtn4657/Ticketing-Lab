package com.ticketinglab.show.domain;

import com.ticketinglab.event.domain.Show;
import com.ticketinglab.venue.domain.Seat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@IdClass(ShowSeatId.class)
@Table(name = "show_seats")
public class ShowSeat {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "price", nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShowSeatStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    public static ShowSeat create(Show show, Seat seat, int price, ShowSeatStatus status) {
        return ShowSeat.builder()
                .show(show)
                .seat(seat)
                .price(price)
                .status(status)
                .build();
    }

    public boolean isAvailable() {
        return status == ShowSeatStatus.AVAILABLE;
    }

    @Builder
    public ShowSeat(
            Show show,
            Seat seat,
            int price,
            ShowSeatStatus status,
            int version
    ) {
        this.show = show;
        this.seat = seat;
        this.price = price;
        this.status = status;
        this.version = version;
    }
}
