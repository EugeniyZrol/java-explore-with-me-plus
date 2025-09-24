package ewm.event.repository;

import ewm.event.model.Event;
import ewm.event.model.EventState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.lang.ScopedValue;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    @NonNull
    List<Event> findAllById(@NonNull Iterable<Long> ids);

    boolean existsById(@NonNull Long id);

    @Query("SELECT e FROM Event e WHERE e.initiator.id = :initiatorId ORDER BY e.createdAt DESC")
    Page<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    Optional<Event> findByIdAndState(Long id, EventState state);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.category.id = :categoryId")
    boolean existsByCategoryId(Long categoryId);

}