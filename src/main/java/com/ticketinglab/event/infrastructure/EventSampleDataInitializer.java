package com.ticketinglab.event.infrastructure;

import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.event.domain.ShowStatus;
import com.ticketinglab.show.domain.ShowSectionInventory;
import com.ticketinglab.show.domain.ShowSectionInventoryRepository;
import com.ticketinglab.show.domain.ShowSeat;
import com.ticketinglab.show.domain.ShowSeatRepository;
import com.ticketinglab.show.domain.ShowSeatStatus;
import com.ticketinglab.venue.domain.Seat;
import com.ticketinglab.venue.domain.SeatRepository;
import com.ticketinglab.venue.domain.Section;
import com.ticketinglab.venue.domain.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Profile("local")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sample-data.events.enabled", havingValue = "true")
public class EventSampleDataInitializer implements ApplicationRunner {

    private final EventRepository eventRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final SectionRepository sectionRepository;
    private final ShowSeatRepository showSeatRepository;
    private final ShowSectionInventoryRepository showSectionInventoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!eventRepository.existsAny()) {
            seedSampleEvents();
        }

        if (!showSeatRepository.existsAny() && !showSectionInventoryRepository.existsAny()) {
            seedShowAvailabilitySamples();
        }
    }

    private void seedSampleEvents() {
        seedJazzNight();
        seedIndieWeek();
        seedWinterGala();
    }

    private void seedJazzNight() {
        Event event = eventRepository.save(
                Event.create(
                        "서울 재즈 페스티벌 2026",
                        "브라스와 피아노 중심의 재즈 공연 예시 데이터입니다.",
                        EventStatus.PUBLISHED
                )
        );

        saveShow(event, LocalDateTime.of(2026, 4, 10, 19, 30), ShowStatus.SCHEDULED, 101L);
        saveShow(event, LocalDateTime.of(2026, 4, 11, 19, 30), ShowStatus.SOLD_OUT, 101L);
        saveShow(event, LocalDateTime.of(2026, 4, 12, 17, 0), ShowStatus.SCHEDULED, 101L);
    }

    private void seedIndieWeek() {
        Event event = eventRepository.save(
                Event.create(
                        "한강 인디 위크 2026",
                        "인디 밴드 라인업을 확인하기 위한 샘플 이벤트 데이터입니다.",
                        EventStatus.DRAFT
                )
        );

        saveShow(event, LocalDateTime.of(2026, 5, 2, 18, 0), ShowStatus.SCHEDULED, 102L);
        saveShow(event, LocalDateTime.of(2026, 5, 3, 18, 0), ShowStatus.SCHEDULED, 102L);
        saveShow(event, LocalDateTime.of(2026, 5, 4, 18, 0), ShowStatus.CANCELLED, 102L);
    }

    private void seedWinterGala() {
        Event event = eventRepository.save(
                Event.create(
                        "겨울 갈라 콘서트 2026",
                        "취소 상태 회차를 확인하기 위한 샘플 이벤트 데이터입니다.",
                        EventStatus.CANCELLED
                )
        );

        saveShow(event, LocalDateTime.of(2026, 12, 20, 19, 0), ShowStatus.CANCELLED, 103L);
        saveShow(event, LocalDateTime.of(2026, 12, 21, 19, 0), ShowStatus.CANCELLED, 103L);
        saveShow(event, LocalDateTime.of(2026, 12, 22, 19, 0), ShowStatus.CANCELLED, 103L);
    }

    private void seedShowAvailabilitySamples() {
        Show firstShow = showRepository.findById(1L)
                .orElseGet(this::seedStandaloneAvailabilityShow);
        Show secondShow = showRepository.findById(2L).orElse(null);
        Show thirdShow = showRepository.findById(3L).orElse(null);

        VenueInventoryFixture fixture = seedVenueInventoryFixture(firstShow.getVenueId());

        seedScheduledShowAvailability(firstShow, fixture);
        seedSoldOutShowAvailability(secondShow, fixture, firstShow.getVenueId());
        seedMatineeShowAvailability(thirdShow, fixture, firstShow.getVenueId());
    }

    private Show seedStandaloneAvailabilityShow() {
        Event event = eventRepository.save(
                Event.create(
                        "회차 가용성 샘플 공연",
                        "SHW-001 테스트 페이지에서 바로 확인할 수 있는 샘플 데이터입니다.",
                        EventStatus.PUBLISHED
                )
        );

        return saveShow(event, LocalDateTime.of(2026, 6, 1, 19, 30), ShowStatus.SCHEDULED, 101L);
    }

    private VenueInventoryFixture seedVenueInventoryFixture(Long venueId) {
        Seat a1 = seatRepository.save(Seat.create("A1", 1, 1, venueId));
        Seat a2 = seatRepository.save(Seat.create("A2", 1, 2, venueId));
        Seat a3 = seatRepository.save(Seat.create("A3", 1, 3, venueId));
        Seat b1 = seatRepository.save(Seat.create("B1", 2, 1, venueId));
        Seat b2 = seatRepository.save(Seat.create("B2", 2, 2, venueId));

        Section floor = sectionRepository.save(Section.create("FLOOR", venueId));
        Section balcony = sectionRepository.save(Section.create("BALCONY", venueId));

        return new VenueInventoryFixture(a1, a2, a3, b1, b2, floor, balcony);
    }

    private void seedScheduledShowAvailability(Show show, VenueInventoryFixture fixture) {
        showSeatRepository.save(ShowSeat.create(show, fixture.a1(), 130000, ShowSeatStatus.AVAILABLE));
        showSeatRepository.save(ShowSeat.create(show, fixture.a2(), 130000, ShowSeatStatus.HELD));
        showSeatRepository.save(ShowSeat.create(show, fixture.a3(), 130000, ShowSeatStatus.SOLD));
        showSeatRepository.save(ShowSeat.create(show, fixture.b1(), 110000, ShowSeatStatus.AVAILABLE));
        showSeatRepository.save(ShowSeat.create(show, fixture.b2(), 110000, ShowSeatStatus.AVAILABLE));

        showSectionInventoryRepository.save(
                ShowSectionInventory.create(show, fixture.floor(), 99000, 180, 96, 12)
        );
        showSectionInventoryRepository.save(
                ShowSectionInventory.create(show, fixture.balcony(), 77000, 90, 34, 6)
        );
    }

    private void seedSoldOutShowAvailability(Show show, VenueInventoryFixture fixture, Long venueId) {
        if (show == null || !show.getVenueId().equals(venueId)) {
            return;
        }

        showSeatRepository.save(ShowSeat.create(show, fixture.a1(), 130000, ShowSeatStatus.SOLD));
        showSeatRepository.save(ShowSeat.create(show, fixture.a2(), 130000, ShowSeatStatus.SOLD));
        showSeatRepository.save(ShowSeat.create(show, fixture.a3(), 130000, ShowSeatStatus.SOLD));
        showSeatRepository.save(ShowSeat.create(show, fixture.b1(), 110000, ShowSeatStatus.RESERVED));
        showSeatRepository.save(ShowSeat.create(show, fixture.b2(), 110000, ShowSeatStatus.SOLD));

        showSectionInventoryRepository.save(
                ShowSectionInventory.create(show, fixture.floor(), 99000, 180, 180, 0)
        );
        showSectionInventoryRepository.save(
                ShowSectionInventory.create(show, fixture.balcony(), 77000, 90, 88, 2)
        );
    }

    private void seedMatineeShowAvailability(Show show, VenueInventoryFixture fixture, Long venueId) {
        if (show == null || !show.getVenueId().equals(venueId)) {
            return;
        }

        showSeatRepository.save(ShowSeat.create(show, fixture.a1(), 120000, ShowSeatStatus.AVAILABLE));
        showSeatRepository.save(ShowSeat.create(show, fixture.a2(), 120000, ShowSeatStatus.AVAILABLE));
        showSeatRepository.save(ShowSeat.create(show, fixture.a3(), 120000, ShowSeatStatus.AVAILABLE));
        showSeatRepository.save(ShowSeat.create(show, fixture.b1(), 98000, ShowSeatStatus.HELD));
        showSeatRepository.save(ShowSeat.create(show, fixture.b2(), 98000, ShowSeatStatus.AVAILABLE));

        showSectionInventoryRepository.save(
                ShowSectionInventory.create(show, fixture.floor(), 92000, 180, 48, 8)
        );
        showSectionInventoryRepository.save(
                ShowSectionInventory.create(show, fixture.balcony(), 69000, 90, 18, 4)
        );
    }

    private Show saveShow(Event event, LocalDateTime startAt, ShowStatus status, Long venueId) {
        return showRepository.save(Show.create(event, startAt, status, venueId));
    }

    private record VenueInventoryFixture(
            Seat a1,
            Seat a2,
            Seat a3,
            Seat b1,
            Seat b2,
            Section floor,
            Section balcony
    ) {
    }
}