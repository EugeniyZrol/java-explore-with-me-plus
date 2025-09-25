package ewm.event.service;

import ewm.event.dto.EventFullDto;
import ewm.event.dto.UpdateEventAdminRequest;
import ewm.event.mapper.EventMapper;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.model.StateAction;
import ewm.event.repository.EventRepository;
import ewm.util.validation.EventValidationUtils;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventStatsService eventStatsService;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(List<Long> users,
                                        List<String> states,
                                        List<Long> categories,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Pageable pageable) {

        EventValidationUtils.validateDateRange(rangeStart, rangeEnd);

        Specification<Event> specification = buildSpecification(users, states, categories, rangeStart, rangeEnd);
        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        return events.stream()
                .map(event -> eventStatsService.enrichEventFullDto(event, eventMapper))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        validateAndUpdateEventState(event, request);
        eventMapper.updateEventFromAdminRequest(request, event);

        Event updatedEvent = eventRepository.save(event);
        return eventStatsService.enrichEventFullDto(updatedEvent, eventMapper);
    }

    private Specification<Event> buildSpecification(List<Long> users, List<String> states,
                                                    List<Long> categories, LocalDateTime rangeStart,
                                                    LocalDateTime rangeEnd) {
        Specification<Event> specification = Specification.where(null);

        if (users != null && !users.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    root.get("initiator").get("id").in(users));
        }

        if (states != null && !states.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    root.get("state").in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            specification = specification.and((root, query, cb) ->
                    root.get("category").get("id").in(categories));
        }

        if (rangeStart != null) {
            specification = specification.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            specification = specification.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        if (rangeStart == null && rangeEnd == null) {
            specification = specification.and((root, query, cb) ->
                    cb.greaterThan(root.get("eventDate"), LocalDateTime.now()));
        }

        return specification;
    }

    private void validateAndUpdateEventState(Event event, UpdateEventAdminRequest request) {
        if (request.getStateAction() != null) {
            StateAction stateAction = StateAction.valueOf(request.getStateAction());
            EventState currentState = EventState.valueOf(event.getState());

            if (stateAction == StateAction.PUBLISH_EVENT) {
                validatePublishEvent(event, currentState);
                event.setState(EventState.PUBLISHED.toString());
                event.setPublishedAt(LocalDateTime.now());
            } else if (stateAction == StateAction.REJECT_EVENT) {
                validateRejectEvent(currentState);
                event.setState(EventState.CANCELED.toString());
            }
        }
    }

    private void validatePublishEvent(Event event, EventState currentState) {
        if (currentState != EventState.PENDING) {
            throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
        }
        EventValidationUtils.validateEventDate(event.getEventDate(), 1);
    }

    private void validateRejectEvent(EventState currentState) {
        if (currentState == EventState.PUBLISHED) {
            throw new ConflictException("Событие можно отклонить, только если оно еще не опубликовано");
        }
    }
}