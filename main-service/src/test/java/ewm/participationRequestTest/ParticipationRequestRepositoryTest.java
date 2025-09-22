package ewm.participationRequestTest;

import ewm.participationRequest.model.ParticipationRequest;
import ewm.participationRequest.model.RequestStatus;
import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import ewm.participationRequest.repository.ParticipationRequestRepository;
import ewm.participationRequest.repository.RequestStatusRepository;
import ewm.user.model.User;
import ewm.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ParticipationRequestRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ParticipationRequestRepository requestRepository;

    @Autowired
    private RequestStatusRepository statusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User user;
    private Event event;
    private RequestStatus pendingStatus;
    private RequestStatus confirmedStatus;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user = userRepository.save(user);

        event = new Event();
        event.setTitle("Test Event");
        event.setInitiator(user);
        event.setState("PUBLISHED");
        event = eventRepository.save(event);

        pendingStatus = new RequestStatus();
        pendingStatus.setId((byte) 1);
        pendingStatus.setName("PENDING");
        pendingStatus = statusRepository.save(pendingStatus);

        confirmedStatus = new RequestStatus();
        confirmedStatus.setId((byte) 2);
        confirmedStatus.setName("CONFIRMED");
        confirmedStatus = statusRepository.save(confirmedStatus);
    }

    @Test
    void testFindAllByRequesterId() {
        // Given
        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(pendingStatus)
                .build();
        entityManager.persist(request);

        // When
        List<ParticipationRequest> requests = requestRepository.findAllByRequesterId(user.getId());

        // Then
        assertEquals(1, requests.size());
        assertEquals(user.getId(), requests.getFirst().getRequester().getId());
    }

    @Test
    void testFindAllByEventId() {
        // Given
        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(pendingStatus)
                .build();
        entityManager.persist(request);

        // When
        List<ParticipationRequest> requests = requestRepository.findAllByEventId(event.getId());

        // Then
        assertEquals(1, requests.size());
        assertEquals(event.getId(), requests.getFirst().getEvent().getId());
    }

    @Test
    void testFindByIdAndRequesterId() {
        // Given
        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(pendingStatus)
                .build();
        request = entityManager.persist(request);

        // When
        Optional<ParticipationRequest> foundRequest = requestRepository.findByIdAndRequesterId(
                request.getId(), user.getId());

        // Then
        assertTrue(foundRequest.isPresent());
        assertEquals(request.getId(), foundRequest.get().getId());
        assertEquals(user.getId(), foundRequest.get().getRequester().getId());
    }

    @Test
    void testExistsByEventIdAndRequesterId() {
        // Given
        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(pendingStatus)
                .build();
        entityManager.persist(request);

        // When
        boolean exists = requestRepository.existsByEventIdAndRequesterId(event.getId(), user.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void testCountConfirmedRequests() {
        // Given
        ParticipationRequest request1 = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(confirmedStatus)
                .build();
        entityManager.persist(request1);

        User user2 = new User();
        user2.setName("Another User");
        user2.setEmail("another@example.com");
        user2 = userRepository.save(user2);

        ParticipationRequest request2 = ParticipationRequest.builder()
                .requester(user2)
                .event(event)
                .created(LocalDateTime.now())
                .status(confirmedStatus)
                .build();
        entityManager.persist(request2);

        // When
        Long count = requestRepository.countConfirmedRequests(event.getId());

        // Then
        assertEquals(2L, count);
    }

    @Test
    void testFindAllByEventIdAndStatusName() {
        // Given
        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(pendingStatus)
                .build();
        entityManager.persist(request);

        // When
        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndStatusName(
                event.getId(), "PENDING");

        // Then
        assertEquals(1, requests.size());
        assertEquals("PENDING", requests.getFirst().getStatus().getName());
    }
}