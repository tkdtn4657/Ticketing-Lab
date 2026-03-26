package com.ticketinglab.show.infrastructure;

import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.show.infrastructure.jpa.ShowSectionInventoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ShowSectionInventoryRepositoryAdapter implements ShowSectionInventoryRepository {

    private final ShowSectionInventoryJpaRepository jpaRepository;

    @Override
    public ShowSectionInventory save(ShowSectionInventory inventory) {
        return jpaRepository.save(inventory);
    }

    @Override
    public List<ShowSectionInventory> saveAll(List<ShowSectionInventory> inventories) {
        return jpaRepository.saveAll(inventories);
    }

    @Override
    public boolean existsAny() {
        return jpaRepository.count() > 0;
    }

    @Override
    public List<ShowSectionInventory> findAllByShowId(Long showId) {
        return jpaRepository.findAllByShow_Id(showId);
    }

    @Override
    public List<ShowSectionInventory> findAllByShowIdAndSectionIdIn(Long showId, Collection<Long> sectionIds) {
        return jpaRepository.findAllByShow_IdAndSection_IdIn(showId, sectionIds);
    }

    @Override
    public List<ShowSectionInventory> findAllByShowIdAndSectionIdInForUpdate(
            Long showId,
            Collection<Long> sectionIds
    ) {
        return jpaRepository.findAllByShowIdAndSectionIdInForUpdate(showId, sectionIds);
    }
}
