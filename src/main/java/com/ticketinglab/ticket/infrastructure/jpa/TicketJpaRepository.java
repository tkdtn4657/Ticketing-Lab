package com.ticketinglab.ticket.infrastructure.jpa;

import com.ticketinglab.ticket.domain.Ticket;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketJpaRepository extends JpaRepository<Ticket, String> {

    @Query(
            value = """
                    select ticket
                    from Ticket ticket
                    join fetch ticket.reservationItem reservationItem
                    join fetch reservationItem.reservation reservation
                    where reservation.userId = :userId
                    """,
            countQuery = """
                    select count(ticket)
                    from Ticket ticket
                    join ticket.reservationItem reservationItem
                    join reservationItem.reservation reservation
                    where reservation.userId = :userId
                    """
    )
    Page<Ticket> findPageByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            select ticket
            from Ticket ticket
            join fetch ticket.reservationItem reservationItem
            join fetch reservationItem.reservation reservation
            where reservation.id = :reservationId
            order by ticket.createdAt asc, ticket.id asc
            """)
    List<Ticket> findAllByReservationId(@Param("reservationId") String reservationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ticket
            from Ticket ticket
            join fetch ticket.reservationItem reservationItem
            join fetch reservationItem.reservation reservation
            where ticket.qrToken = :qrToken
            """)
    Optional<Ticket> findByQrTokenForUpdate(@Param("qrToken") String qrToken);
}
