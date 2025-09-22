package ewm.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ewm.event.model.StateAction;
import ewm.util.validation.ValidEnum;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @Future //TODO дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    @ValidEnum(
            enumClass = StateAction.class,
            values = { "PUBLISH_EVENT", "REJECT_EVENT" },
            message = "Недопустимое значение. Допустимые: {accepted}"
    )
    private String stateAction;

    @Size(min = 3, max = 120)
    private String title;

    public boolean isAnnotationEmpty() {
        return annotation == null;
    }

    public boolean isCategoryEmpty() {
        return category == null || category == 0;
    }

    public boolean isDescriptionEmpty() {
        return description == null;
    }

    public boolean isEventDateEmpty() {
        return eventDate == null;
    }

    public boolean isLocationEmpty() {
        return location == null;
    }

    public boolean isPaidEmpty() {
        return paid == null;
    }

    public boolean isParticipantLimitEmpty() {
        return participantLimit == null || participantLimit == 0;
    }

    public boolean isRequestModerationEmpty() {
        return requestModeration == null;
    }

    public boolean isStateActionEmpty() {
        return stateAction == null;
    }

    public boolean isTitleEmpty() {
        return title == null;
    }
}
