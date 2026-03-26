package com.ticketinglab.show.domain;

import java.util.Collection;
import java.util.List;

public interface ShowSectionInventoryRepository {
    ShowSectionInventory save(ShowSectionInventory inventory);
    List<ShowSectionInventory> saveAll(List<ShowSectionInventory> inventories);
    boolean existsAny();
    List<ShowSectionInventory> findAllByShowId(Long showId);
    List<ShowSectionInventory> findAllByShowIdAndSectionIdIn(Long showId, Collection<Long> sectionIds);
    List<ShowSectionInventory> findAllByShowIdAndSectionIdInForUpdate(Long showId, Collection<Long> sectionIds);
}
