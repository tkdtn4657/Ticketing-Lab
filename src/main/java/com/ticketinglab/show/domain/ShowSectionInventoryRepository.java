package com.ticketinglab.show.domain;

import java.util.List;

public interface ShowSectionInventoryRepository {
    ShowSectionInventory save(ShowSectionInventory inventory);
    boolean existsAny();
    List<ShowSectionInventory> findAllByShowId(Long showId);
}