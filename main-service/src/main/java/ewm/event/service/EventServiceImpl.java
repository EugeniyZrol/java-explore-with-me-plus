package ewm.event.service;

import ewm.event.repository.specification.EventSpecifications;
import ewm.event.dto.*;
import ewm.event.mapper.EventMapper;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.model.StateAction;
import ewm.event.repository.EventRepository;
import ewm.util.validation.EventValidationUtils;
import ewm.exception.NotFoundException;
import ewm.user.model.User;
import ewm.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private static final String ENDPOINT = "/events";

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserService userService;
    private final EventStatsService eventStatsService;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(Long userId, Pageable pageable) {
        userService.getUserById(userId);
        Page<Event> eventsPage = eventRepository.findByInitiatorIdOrderByCreatedAtDesc(userId, pageable);

        return eventsPage.getContent().stream()
                .map(event -> eventStatsService.enrichEventShortDto(event, eventMapper))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEvent(Long userId, Long eventId, String ip) {
        userService.getUserById(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        eventStatsService.recordHit(ENDPOINT + "/" + eventId, ip);
        return eventStatsService.enrichEventFullDto(event, eventMapper);
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        EventValidationUtils.validateEventDate(newEventDto.getEventDate(), 2);

        User user = userService.getUserEntityById(userId);
        Event event = eventMapper.toEvent(newEventDto);

        event.setInitiator(user);
        event.setCreatedAt(LocalDateTime.now());
        event.setState(EventState.PENDING.toString());

        Event savedEvent = eventRepository.save(event);

        EventFullDto eventDto = eventMapper.toFullDto(savedEvent);
        eventDto.setConfirmedRequests(0L);
        eventDto.setViews(0L);
        return eventDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        userService.getUserById(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        EventValidationUtils.validateEventStateForUpdate(event);
        EventValidationUtils.validateEventDate(request.getEventDate(), 2);
        EventValidationUtils.validateParticipantLimit(request.getParticipantLimit());

        eventMapper.updateEventFromUserRequest(request, event);
        updateEventState(event, request);

        Event updatedEvent = eventRepository.save(event);
        return eventStatsService.enrichEventFullDto(updatedEvent, eventMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, Integer from, Integer size, String ip) {

        EventValidationUtils.validateDateRange(rangeStart, rangeEnd);
        eventStatsService.recordHit(ENDPOINT, ip);

        Specification<Event> spec = buildPublicEventsSpecification(text, categories, paid, rangeStart, rangeEnd, onlyAvailable);
        Pageable pageable = createPageable(from, size, sort);

        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        List<EventShortDto> result = events.stream()
                .map(event -> eventStatsService.enrichEventShortDto(event, eventMapper))
                .collect(Collectors.toList());

        return sortEvents(result, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEventById(Long eventId, String ip) {
        Event event = eventRepository.findById(eventId)
                .filter(e -> EventState.PUBLISHED.toString().equals(e.getState()))
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        eventStatsService.recordHit(ENDPOINT + "/" + eventId, ip);
        return eventStatsService.enrichEventFullDto(event, eventMapper);
    }

    private Specification<Event> buildPublicEventsSpecification(String text, List<Long> categories,
                                                                Boolean paid, LocalDateTime rangeStart,
                                                                LocalDateTime rangeEnd, Boolean onlyAvailable) {
        Specification<Event> spec = Specification.where(EventSpecifications.isPublished());

        if (text != null && !text.trim().isEmpty()) {
            spec = spec.and(EventSpecifications.containsText(text));
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and(EventSpecifications.hasCategories(categories));
        }

        if (paid != null) {
            spec = spec.and(EventSpecifications.isPaid(paid));
        }

        LocalDateTime actualRangeStart = (rangeStart == null && rangeEnd == null) ?
                LocalDateTime.now() : rangeStart;

        if (actualRangeStart != null) {
            spec = spec.and(EventSpecifications.startsAfter(actualRangeStart));
        }

        if (rangeEnd != null) {
            spec = spec.and(EventSpecifications.endsBefore(rangeEnd));
        }

        if (Boolean.TRUE.equals(onlyAvailable)) {
            spec = spec.and(EventSpecifications.hasAvailableSlots());
        }

        return spec;
    }

    private Pageable createPageable(Integer from, Integer size, String sort) {
        int pageSize = size != null ? Math.max(1, size) : 10;
        int pageFrom = from != null ? Math.max(0, from) : 0;
        int page = pageFrom / pageSize;

        Sort sorting = "VIEWS".equals(sort) ?
                Sort.by(Sort.Direction.DESC, "id") :
                Sort.by(Sort.Direction.DESC, "eventDate");

        return PageRequest.of(page, pageSize, sorting);
    }

    private List<EventShortDto> sortEvents(List<EventShortDto> events, String sort) {
        if ("VIEWS".equals(sort)) {
            return events.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .collect(Collectors.toList());
        }
        return events;
    }

    private void updateEventState(Event event, UpdateEventUserRequest request) {
        if (request.getStateAction() != null) {
            StateAction stateAction = StateAction.valueOf(request.getStateAction());
            if (stateAction == StateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING.toString());
            } else if (stateAction == StateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED.toString());
            }
        }
    }
}