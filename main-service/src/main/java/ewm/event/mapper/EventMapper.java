package ewm.event.mapper;

import ewm.categories.model.Category;
import ewm.event.dto.*;
import ewm.event.model.Event;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.postgresql.geometric.PGpoint;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EventMapper {

    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryIdToCategory")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "publishedAt",  ignore = true)
    @Mapping(target = "location", source = "location", qualifiedByName = "locationDtoToPGpoint")
    @Mapping(target = "isPaid", source = "paid")
    @Mapping(target = "isRequestModeration", source = "requestModeration")
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryIdToCategory")
    @Mapping(target = "location", source = "location", qualifiedByName = "locationDtoToPGpoint")
    @Mapping(target = "isPaid", source = "paid")
    @Mapping(target = "isRequestModeration", source = "requestModeration")
    Event toEvent(UpdateEventUserRequest updateEventUserRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "category", qualifiedByName = "categoryIdToCategory")
    @Mapping(target = "location", source = "location", qualifiedByName = "locationDtoToPGpoint")
    @Mapping(target = "isPaid", source = "paid")
    @Mapping(target = "isRequestModeration", source = "requestModeration")
    Event toEvent(UpdateEventAdminRequest updateEventAdminRequest);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "initiator",  ignore = true)
    @Mapping(target = "paid", source = "isPaid")
    @Mapping(target = "views", ignore = true)
    EventShortDto toShortDto(Event event);

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "createdOn", source = "createdAt")
    @Mapping(target = "initiator",  ignore = true)
    @Mapping(target = "location", source = "location", qualifiedByName = "PGpointToLocationDto")
    @Mapping(target = "paid", source = "isPaid")
    @Mapping(target = "publishedOn", source = "publishedAt")
    @Mapping(target = "requestModeration", source = "isRequestModeration")
    @Mapping(target = "views", ignore = true)
    EventFullDto toFullDto(Event event);

    @Named("categoryIdToCategory")
    default Category toCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return new Category(categoryId, null);
    }

    @Named("locationDtoToPGpoint")
    default PGpoint toPGpoint(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }
        return new PGpoint(
                locationDto.getLon(),
                locationDto.getLat()
        );
    }

    @Named("PGpointToLocationDto")
    default LocationDto toLocationDto(PGpoint pgpoint) {
        if (pgpoint == null) {
            return null;
        }
        return new LocationDto(
                (float) pgpoint.y,
                (float) pgpoint.x
        );
    }
}