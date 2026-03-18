package com.ticketinglab.event.infrastructure;

import com.ticketinglab.event.domain.Event;
import com.ticketinglab.event.domain.EventRepository;
import com.ticketinglab.event.domain.EventStatus;
import com.ticketinglab.event.domain.Show;
import com.ticketinglab.event.domain.ShowRepository;
import com.ticketinglab.event.domain.ShowStatus;
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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (eventRepository.existsAny()) {
            return;
        }

        seedJazzNight();
        seedIndieWeek();
        seedWinterGala();
    }

    private void seedJazzNight() {
        Event event = eventRepository.save(
                Event.create(
                        "서울 재즈 나이트 2026",
                        "브라스와 피아노 중심의 심야 재즈 공연 예시 데이터입니다.",
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
                        "인디 밴드 쇼케이스 확인을 위한 드래프트 이벤트 예시 데이터입니다.",
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
                        "취소 상태 필터 확인을 위한 갈라 콘서트 예시 데이터입니다.",
                        EventStatus.CANCELLED
                )
        );

        saveShow(event, LocalDateTime.of(2026, 12, 20, 19, 0), ShowStatus.CANCELLED, 103L);
        saveShow(event, LocalDateTime.of(2026, 12, 21, 19, 0), ShowStatus.CANCELLED, 103L);
        saveShow(event, LocalDateTime.of(2026, 12, 22, 19, 0), ShowStatus.CANCELLED, 103L);
    }

    private void saveShow(Event event, LocalDateTime startAt, ShowStatus status, Long venueId) {
        showRepository.save(Show.create(event, startAt, status, venueId));
    }
}