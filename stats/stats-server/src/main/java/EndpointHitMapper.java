import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {

    @Mapping(target = "id", ignore = true) // Игнорируем id при преобразовании DTO -> Entity
    EndpointHitEntity toEntity(EndpointHit endpointHit);

    EndpointHit toDto(EndpointHitEntity entity);
}