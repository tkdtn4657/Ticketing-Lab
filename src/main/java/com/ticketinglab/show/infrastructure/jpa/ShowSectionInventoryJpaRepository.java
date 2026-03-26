package com.ticketinglab.show.infrastructure.jpa;

import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ShowSectionInventoryJpaRepository extends JpaRepository<ShowSectionInventory, ShowSectionInventoryId> {

    @EntityGraph(attributePaths = "section")
    List<ShowSectionInventory> findAllByShow_Id(Long showId);

    List<ShowSectionInventory> findAllByShow_IdAndSection_IdIn(Long showId, Collection<Long> sectionIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select inventory
            from ShowSectionInventory inventory
            where inventory.show.id = :showId
              and inventory.section.id in :sectionIds
            """)
    List<ShowSectionInventory> findAllByShowIdAndSectionIdInForUpdate(
            @Param("showId") Long showId,
            @Param("sectionIds") Collection<Long> sectionIds
    );
}
