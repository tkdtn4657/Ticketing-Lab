package com.ticketinglab.venue.infrastructure;

import com.ticketinglab.venue.domain.Section;
import com.ticketinglab.venue.domain.SectionRepository;
import com.ticketinglab.venue.infrastructure.jpa.SectionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SectionRepositoryAdapter implements SectionRepository {

    private final SectionJpaRepository jpaRepository;

    @Override
    public Section save(Section section) {
        return jpaRepository.save(section);
    }
}
