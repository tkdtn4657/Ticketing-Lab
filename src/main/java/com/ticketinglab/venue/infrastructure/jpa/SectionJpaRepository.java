package com.ticketinglab.venue.infrastructure.jpa;

import com.ticketinglab.venue.domain.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionJpaRepository extends JpaRepository<Section, Long> {
}
