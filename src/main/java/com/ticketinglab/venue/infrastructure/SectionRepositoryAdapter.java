package com.ticketinglab.venue.infrastructure;

import com.ticketinglab.venue.domain.Section;
import com.ticketinglab.venue.domain.SectionRepository;
import com.ticketinglab.venue.infrastructure.jpa.SectionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SectionRepositoryAdapter implements SectionRepository {

    private final SectionJpaRepository jpaRepository;

    @Override
    public Section save(Section section) {
        return jpaRepository.save(section);
    }

    @Override
    public List<Section> saveAll(List<Section> sections) {
        return jpaRepository.saveAll(sections);
    }

    @Override
    public List<Section> findAllByVenueId(Long venueId) {
        return jpaRepository.findAllByVenueIdOrderByNameAscIdAsc(venueId);
    }

    @Override
    public List<Section> findAllByVenueIdAndIdIn(Long venueId, Collection<Long> sectionIds) {
        return jpaRepository.findAllByVenueIdAndIdIn(venueId, sectionIds);
    }

    @Override
    public List<Section> findAllByVenueIdAndNameIn(Long venueId, Collection<String> names) {
        return jpaRepository.findAllByVenueIdAndNameIn(venueId, names);
    }
}