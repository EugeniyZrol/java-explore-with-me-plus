package ewm.event.repository;

import ewm.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @NonNull
    List<Event> findAllById(@NonNull Iterable<Long> ids);

    boolean existsById(@NonNull Long id);
}