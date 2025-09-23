package ewm.event.admin.service;

import client.StatsClient;
import ewm.event.dto.EventFullDto;
import ewm.event.dto.UpdateEventAdminRequest;
import ewm.participationRequest.service.ConfirmedRequestsService;
import model.ViewStatsDto;
import ewm.event.mapper.EventMapper;
import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.event.model.StateAction;
import ewm.event.repository.EventRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEventServiceImpl implements AdminEventService {
    private static final String ENDPOINT = "/events";

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final StatsClient statsClient;
    private final ConfirmedRequestsService confirmedRequestsService;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(List<Long> users,
                                        List<String> states,
                                        List<Long> categories,
                                        LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd,
                                        Pageable pageable) {

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

        List<Event> events = eventRepository.findAll(specification, pageable).getContent();

        List<String> uris = events.stream()
                .map(event -> ENDPOINT + "/" + event.getId())
                .collect(Collectors.toList());

        Map<String, Long> viewsMap = getViewsMap(uris);

        return events.stream()
                .map(event -> {
                    EventFullDto eventDto = eventMapper.toFullDto(event);

                    Long confirmedRequests = confirmedRequestsService.getConfirmedRequestsCount(event.getId());
                    eventDto.setConfirmedRequests(confirmedRequests);

                    String uri = ENDPOINT + "/" + event.getId();
                    eventDto.setViews(viewsMap.getOrDefault(uri, 0L));

                    return eventDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (request.getStateAction() != null) {
            StateAction stateAction = StateAction.valueOf(request.getStateAction());
            EventState currentState = EventState.valueOf(event.getState());

            if (stateAction == StateAction.PUBLISH_EVENT) {
                if (currentState != EventState.PENDING) {
                    throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Дата начала события должна быть не ранее чем за час от даты публикации");
                }
                event.setState(EventState.PUBLISHED.toString());
                event.setPublishedAt(LocalDateTime.now());
            } else if (stateAction == StateAction.REJECT_EVENT) {
                if (currentState == EventState.PUBLISHED) {
                    throw new ConflictException("Событие можно отклонить, только если оно еще не опубликовано");
                }
                event.setState(EventState.CANCELED.toString());
            }
        }

        eventMapper.updateEventFromAdminRequest(request, event);

        Event updatedEvent = eventRepository.save(event);
        EventFullDto eventDto = eventMapper.toFullDto(updatedEvent);

        Long confirmedRequests = confirmedRequestsService.getConfirmedRequestsCount(eventId);
        eventDto.setConfirmedRequests(confirmedRequests);

        String uri = ENDPOINT + "/" + eventId;
        Map<String, Long> viewsMap = getViewsMap(List.of(uri));
        eventDto.setViews(viewsMap.getOrDefault(uri, 0L));

        return eventDto;
    }

    private Map<String, Long> getViewsMap(List<String> uris) {
        List<ViewStatsDto> viewStatsDtos = statsClient.getStats(
                LocalDateTime.now().minusYears(10),
                LocalDateTime.now().plusHours(1),
                uris.isEmpty() ? Optional.empty() : Optional.of(uris),
                false
        );

        return viewStatsDtos.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits
                ));
    }
}