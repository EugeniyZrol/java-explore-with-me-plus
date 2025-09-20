package ewm.event.dto;

import ewm.categories.dto.CategoryDto;
import ewm.user.dto.UserResponse;
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
    private CategoryDto category;
    private UserResponse initiator;
    private LocalDateTime eventDate;
    private Boolean paid;
    private Integer confirmedRequests;
    private Long views;
}
