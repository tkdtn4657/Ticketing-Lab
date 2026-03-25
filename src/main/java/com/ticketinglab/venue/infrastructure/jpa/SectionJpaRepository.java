package com.ticketinglab.venue.infrastructure.jpa;

import com.ticketinglab.venue.domain.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SectionJpaRepository extends JpaRepository<Section, Long> {
    List<Section> findAllByVenueIdOrderByNameAscIdAsc(Long venueId);
    List<Section> findAllByVenueIdAndIdIn(Long venueId, Collection<Long> sectionIds);
    List<Section> findAllByVenueIdAndNameIn(Long venueId, Collection<String> names);
}