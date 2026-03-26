package com.ticketinglab.venue.domain;

import java.util.Collection;
import java.util.List;

public interface SectionRepository {
    Section save(Section section);
    List<Section> saveAll(List<Section> sections);
    List<Section> findAllByVenueId(Long venueId);
    List<Section> findAllByVenueIdAndIdIn(Long venueId, Collection<Long> sectionIds);
    List<Section> findAllByVenueIdAndNameIn(Long venueId, Collection<String> names);
}