package ewm.event.service;

import ewm.event.model.Event;
import ewm.event.model.EventState;
import ewm.participationRequest.model.RequestStatus;

import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;

public final class PublicEventSpecs {

    private PublicEventSpecs() {}

    public static Specification<Event> published() {
        return (root, query, cb) -> cb.equal(root.get("state"), EventState.PUBLISHED);
    }

    public static Specification<Event> text(String text) {
        if (text == null || text.isBlank()) return null;
        String like = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("annotation")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<Event> categories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return null;
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    public static Specification<Event> paid(Boolean paid) {
        if (paid == null) return null;
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> dateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("eventDate"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("eventDate"), start);
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("eventDate"), end);
            } else {
                return cb.conjunction();
            }
        };
    }

    public static Specification<Event> hasSlots() {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<?> pr = sub.from(ewm.participationRequest.model.ParticipationRequest.class);
            sub.select(cb.count(pr));
            sub.where(
                    cb.equal(pr.get("event").get("id"), root.get("id")),
                    cb.equal(pr.get("status"), RequestStatus.CONFIRMED)
            );
            return cb.or(
                    cb.equal(root.get("participantLimit"), 0),
                    cb.greaterThan(root.get("participantLimit"), sub)
            );
        };
    }
}
