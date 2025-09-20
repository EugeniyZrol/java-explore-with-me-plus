package ewm.event.mapper;

import ewm.event.dto.EventShortDto;
import ewm.event.model.Event;
import ewm.categories.mapper.CategoryMapper;
import ewm.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {
    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "confirmedRequests", expression = "java(0)")
    @Mapping(target = "views", expression = "java(0L)")
    EventShortDto toShortDto(Event event);
}