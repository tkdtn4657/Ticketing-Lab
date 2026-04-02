package com.ticketinglab.ticket.infrastructure.jpa;

import com.ticketinglab.ticket.domain.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}