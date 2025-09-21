package ewm.event.model;

import ewm.categories.model.Category;
import ewm.user.model.User;
import ewm.util.PgPointType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.postgresql.geometric.PGpoint;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    private User initiator;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "published_on")
    private LocalDateTime publishedAt;

    @Column(columnDefinition = "point")
    @Type(PgPointType.class)
    private PGpoint location;

    @Column(name = "paid")
    private Boolean isPaid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "request_moderation")
    private Boolean isRequestModeration;

    @Column(nullable = false, length = 20)
    private String state;
}
