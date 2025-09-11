package ru.practicum;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "app", target = "app")
    @Mapping(source = "uri", target = "uri")
    @Mapping(source = "ip", target = "ip")
    @Mapping(source = "timestamp", target = "timestamp")
    EndpointHitEntity toEntity(EndpointHitDto endpointHitDto);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "app", target = "app")
    @Mapping(source = "uri", target = "uri")
    @Mapping(source = "ip", target = "ip")
    @Mapping(source = "timestamp", target = "timestamp")
    EndpointHitDto toDto(EndpointHitEntity entity);
}