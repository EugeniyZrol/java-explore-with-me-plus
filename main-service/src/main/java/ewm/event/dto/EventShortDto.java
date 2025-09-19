package ewm.event.dto;

import ewm.categories.model.Category;
import ewm.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;
    private Category category;
    private User initiator;
    private LocalDateTime eventDate;
    private Boolean paid;
    private Integer confirmedRequests;
    private Long views;
}
