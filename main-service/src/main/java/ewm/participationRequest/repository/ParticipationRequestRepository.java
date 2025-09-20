package ewm.participationRequest.repository;

import ewm.participationRequest.model.ParticipationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long userId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr WHERE pr.event.id = :eventId AND pr.status.name = 'CONFIRMED'")
    Long countConfirmedRequests(@Param("eventId") Long eventId);

    @Query("SELECT pr FROM ParticipationRequest pr WHERE pr.event.id = :eventId AND pr.status.name = :status")
    List<ParticipationRequest> findAllByEventIdAndStatusName(@Param("eventId") Long eventId, @Param("status") String status);
}
