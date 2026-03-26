package com.ticketinglab.show.infrastructure.jpa;

import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ShowSectionInventoryJpaRepository extends JpaRepository<ShowSectionInventory, ShowSectionInventoryId> {

    @EntityGraph(attributePaths = "section")
    List<ShowSectionInventory> findAllByShow_Id(Long showId);

    List<ShowSectionInventory> findAllByShow_IdAndSection_IdIn(Long showId, Collection<Long> sectionIds);
}