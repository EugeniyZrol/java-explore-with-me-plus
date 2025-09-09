import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EndpointHitMapperTest {

    private final EndpointHitMapper mapper = Mappers.getMapper(EndpointHitMapper.class);

    @Test
    void toEntity_shouldMapDtoToEntityCorrectly() {
        EndpointHitDto dto = new EndpointHitDto();
        dto.setApp("test-app");
        dto.setUri("/test/endpoint");
        dto.setIp("192.168.1.1");
        dto.setTimestamp(LocalDateTime.now());

        EndpointHitEntity entity = mapper.toEntity(dto);

        assertNull(entity.getId());
        assertEquals(dto.getApp(), entity.getApp());
        assertEquals(dto.getUri(), entity.getUri());
        assertEquals(dto.getIp(), entity.getIp());
        assertEquals(dto.getTimestamp(), entity.getTimestamp());
    }

    @Test
    void toDto_shouldMapEntityToDtoCorrectly() {
        EndpointHitEntity entity = new EndpointHitEntity();
        entity.setId(1L);
        entity.setApp("test-app");
        entity.setUri("/test/endpoint");
        entity.setIp("192.168.1.1");
        entity.setTimestamp(LocalDateTime.now());

        EndpointHitDto dto = mapper.toDto(entity);

        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getApp(), dto.getApp());
        assertEquals(entity.getUri(), dto.getUri());
        assertEquals(entity.getIp(), dto.getIp());
        assertEquals(entity.getTimestamp(), dto.getTimestamp());
    }
}