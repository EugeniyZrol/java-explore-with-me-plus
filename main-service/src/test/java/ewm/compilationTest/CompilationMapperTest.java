package ewm.compilationTest;

import ewm.compilation.dto.CompilationResponse;
import ewm.compilation.dto.NewCompilationRequest;
import ewm.compilation.dto.UpdateCompilationRequest;
import ewm.compilation.mapper.CompilationMapper;
import ewm.compilation.model.Compilation;
import ewm.event.dto.EventShortDto;
import ewm.event.model.Event;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompilationMapperTest {

    private final CompilationMapper compilationMapper = Mappers.getMapper(CompilationMapper.class);

    @Test
    void toEntity_ShouldMapCorrectly() {
        // Arrange
        NewCompilationRequest request = NewCompilationRequest.builder()
                .title("Test Compilation")
                .pinned(true)
                .events(Set.of(1L, 2L))
                .build();

        // Act
        Compilation result = compilationMapper.toEntity(request);

        // Assert
        assertNotNull(result);
        assertEquals("Test Compilation", result.getTitle());
        assertTrue(result.isPinned());
        assertNull(result.getEvents()); // events игнорируются при маппинге
    }

    @Test
    void toDto_ShouldMapCorrectly() {
        // Arrange
        Event event = Event.builder()
                .id(1L)
                .title("Test Event")
                .build();

        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Test Compilation")
                .pinned(true)
                .events(Set.of(event))
                .build();

        // Act
        CompilationResponse result = compilationMapper.toDto(compilation);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Compilation", result.getTitle());
        assertTrue(result.getPinned());
        assertNotNull(result.getEvents());
    }

    @Test
    void toDto_WithNullEvents_ShouldReturnNullEvents() {
        // Arrange
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Test Compilation")
                .pinned(true)
                .events(null)
                .build();

        // Act
        CompilationResponse result = compilationMapper.toDto(compilation);

        // Assert
        assertNotNull(result);
        assertNull(result.getEvents());
    }

    @Test
    void toDto_WithEmptyEvents_ShouldReturnEmptyEvents() {
        // Arrange
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Test Compilation")
                .pinned(true)
                .events(Collections.emptySet())
                .build();

        // Act
        CompilationResponse result = compilationMapper.toDto(compilation);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getEvents());
        assertTrue(result.getEvents().isEmpty());
    }

    @Test
    void updateEntityFromDto_ShouldUpdateFields() {
        // Arrange
        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Old Title")
                .pinned(false)
                .events(Collections.emptySet())
                .build();

        UpdateCompilationRequest updateRequest = UpdateCompilationRequest.builder()
                .title("New Title")
                .pinned(true)
                .events(Set.of(1L, 2L))
                .build();

        // Act
        compilationMapper.updateEntityFromDto(updateRequest, compilation);

        // Assert
        assertEquals("New Title", compilation.getTitle());
        assertTrue(compilation.isPinned());
        // events не должны обновляться через маппер
        assertNotNull(compilation.getEvents());
        assertTrue(compilation.getEvents().isEmpty());
    }

    @Test
    void eventsToShortDtos_ShouldConvertEventsToShortDtos() {
        // Arrange
        Event event1 = Event.builder().id(1L).title("Event 1").build();
        Event event2 = Event.builder().id(2L).title("Event 2").build();
        Set<Event> events = Set.of(event1, event2);

        // Act
        Set<EventShortDto> result = compilationMapper.eventsToShortDtos(events);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void eventsToShortDtos_WithNullInput_ShouldReturnNull() {
        // Act
        Set<EventShortDto> result = compilationMapper.eventsToShortDtos(null);

        // Assert
        assertNull(result);
    }

    @Test
    void eventsToShortDtos_WithEmptyInput_ShouldReturnEmptySet() {
        // Act
        Set<EventShortDto> result = compilationMapper.eventsToShortDtos(Collections.emptySet());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
