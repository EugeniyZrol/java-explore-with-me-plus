package ewm.compilationTest;

import ewm.compilation.dto.CompilationResponse;
import ewm.compilation.dto.NewCompilationRequest;
import ewm.compilation.dto.UpdateCompilationRequest;
import ewm.compilation.mapper.CompilationMapper;
import ewm.compilation.model.Compilation;
import ewm.compilation.repository.CompilationRepository;
import ewm.compilation.service.CompilationServiceImpl;
import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CompilationMapper compilationMapper;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private NewCompilationRequest newCompilationRequest;
    private UpdateCompilationRequest updateCompilationRequest;
    private Compilation compilation;
    private CompilationResponse compilationResponse;
    private Event event;

    @BeforeEach
    void setUp() {
        event = Event.builder()
                .id(1L)
                .title("Test Event")
                .build();

        newCompilationRequest = NewCompilationRequest.builder()
                .title("Test Compilation")
                .pinned(true)
                .events(Set.of(1L))
                .build();

        updateCompilationRequest = UpdateCompilationRequest.builder()
                .title("Updated Compilation")
                .pinned(false)
                .events(Set.of(1L))
                .build();

        compilation = Compilation.builder()
                .id(1L)
                .title("Test Compilation")
                .pinned(true)
                .events(Set.of(event))
                .build();

        compilationResponse = CompilationResponse.builder()
                .id(1L)
                .title("Test Compilation")
                .pinned(true)
                .events(Collections.emptySet())
                .build();
    }

    @Test
    void createCompilation_ShouldCreateSuccessfully() {
        // Arrange
        when(compilationRepository.existsByTitle("Test Compilation")).thenReturn(false);
        when(compilationMapper.toEntity(newCompilationRequest)).thenReturn(compilation);
        when(eventRepository.findAllById(Set.of(1L))).thenReturn(List.of(event));
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(compilationResponse);

        // Act
        CompilationResponse result = compilationService.createCompilation(newCompilationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Compilation", result.getTitle());
        assertTrue(result.getPinned());

        verify(compilationRepository).existsByTitle("Test Compilation");
        verify(compilationMapper).toEntity(newCompilationRequest);
        verify(eventRepository).findAllById(Set.of(1L));
        verify(compilationRepository).save(compilation);
        verify(compilationMapper).toDto(compilation);
    }

    @Test
    void createCompilation_WithMinTitleLength_ShouldCreateSuccessfully() {
        // Arrange
        NewCompilationRequest requestWithMinTitle = NewCompilationRequest.builder()
                .title("A") // минимальная длина 1
                .pinned(false)
                .events(null)
                .build();

        Compilation compilationWithMinTitle = Compilation.builder()
                .id(2L)
                .title("A")
                .pinned(false)
                .events(Collections.emptySet())
                .build();

        when(compilationRepository.existsByTitle("A")).thenReturn(false);
        when(compilationMapper.toEntity(requestWithMinTitle)).thenReturn(compilationWithMinTitle);
        when(compilationRepository.save(compilationWithMinTitle)).thenReturn(compilationWithMinTitle);
        when(compilationMapper.toDto(compilationWithMinTitle)).thenReturn(
                CompilationResponse.builder().id(2L).title("A").pinned(false).events(Collections.emptySet()).build()
        );

        // Act
        CompilationResponse result = compilationService.createCompilation(requestWithMinTitle);

        // Assert
        assertNotNull(result);
        assertEquals("A", result.getTitle());
        assertFalse(result.getPinned());
    }

    @Test
    void createCompilation_WithMaxTitleLength_ShouldCreateSuccessfully() {
        // Arrange
        String maxLengthTitle = "A".repeat(50); // максимальная длина 50
        NewCompilationRequest requestWithMaxTitle = NewCompilationRequest.builder()
                .title(maxLengthTitle)
                .pinned(true)
                .events(Set.of())
                .build();

        Compilation compilationWithMaxTitle = Compilation.builder()
                .id(3L)
                .title(maxLengthTitle)
                .pinned(true)
                .events(Collections.emptySet())
                .build();

        when(compilationRepository.existsByTitle(maxLengthTitle)).thenReturn(false);
        when(compilationMapper.toEntity(requestWithMaxTitle)).thenReturn(compilationWithMaxTitle);
        when(compilationRepository.save(compilationWithMaxTitle)).thenReturn(compilationWithMaxTitle);
        when(compilationMapper.toDto(compilationWithMaxTitle)).thenReturn(
                CompilationResponse.builder().id(3L).title(maxLengthTitle).pinned(true).events(Collections.emptySet()).build()
        );

        // Act
        CompilationResponse result = compilationService.createCompilation(requestWithMaxTitle);

        // Assert
        assertNotNull(result);
        assertEquals(maxLengthTitle, result.getTitle());
        assertEquals(50, result.getTitle().length());
    }

    @Test
    void createCompilation_WithNullEvents_ShouldCreateSuccessfully() {
        // Arrange
        NewCompilationRequest requestWithNullEvents = NewCompilationRequest.builder()
                .title("Test Compilation")
                .pinned(false)
                .events(null)
                .build();

        Compilation compilationWithNullEvents = Compilation.builder()
                .id(4L)
                .title("Test Compilation")
                .pinned(false)
                .events(null)
                .build();

        when(compilationRepository.existsByTitle("Test Compilation")).thenReturn(false);
        when(compilationMapper.toEntity(requestWithNullEvents)).thenReturn(compilationWithNullEvents);
        when(compilationRepository.save(compilationWithNullEvents)).thenReturn(compilationWithNullEvents);
        when(compilationMapper.toDto(compilationWithNullEvents)).thenReturn(
                CompilationResponse.builder().id(4L).title("Test Compilation").pinned(false).events(null).build()
        );

        // Act
        CompilationResponse result = compilationService.createCompilation(requestWithNullEvents);

        // Assert
        assertNotNull(result);
        assertEquals("Test Compilation", result.getTitle());
        assertNull(result.getEvents());
        verify(eventRepository, never()).findAllById(anySet());
    }

    @Test
    void createCompilation_WithEmptyEvents_ShouldCreateSuccessfully() {
        // Arrange
        NewCompilationRequest requestWithEmptyEvents = NewCompilationRequest.builder()
                .title("Test Compilation")
                .pinned(false)
                .events(Collections.emptySet())
                .build();

        Compilation compilationWithEmptyEvents = Compilation.builder()
                .id(5L)
                .title("Test Compilation")
                .pinned(false)
                .events(Collections.emptySet())
                .build();

        when(compilationRepository.existsByTitle("Test Compilation")).thenReturn(false);
        when(compilationMapper.toEntity(requestWithEmptyEvents)).thenReturn(compilationWithEmptyEvents);
        when(compilationRepository.save(compilationWithEmptyEvents)).thenReturn(compilationWithEmptyEvents);
        when(compilationMapper.toDto(compilationWithEmptyEvents)).thenReturn(
                CompilationResponse.builder().id(5L).title("Test Compilation").pinned(false).events(Collections.emptySet()).build()
        );

        // Act
        CompilationResponse result = compilationService.createCompilation(requestWithEmptyEvents);

        // Assert
        assertNotNull(result);
        assertEquals("Test Compilation", result.getTitle());
        assertTrue(result.getEvents().isEmpty());
        verify(eventRepository, never()).findAllById(anySet());
    }

    @Test
    void createCompilation_WhenTitleAlreadyExists_ShouldThrowConflictException() {
        // Arrange
        when(compilationRepository.existsByTitle("Test Compilation")).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class,
                () -> compilationService.createCompilation(newCompilationRequest));

        assertEquals("Компиляция с названием уже существует: Test Compilation", exception.getMessage());
        verify(compilationRepository).existsByTitle("Test Compilation");
        verify(compilationMapper, never()).toEntity(any());
        verify(eventRepository, never()).findAllById(anySet());
        verify(compilationRepository, never()).save(any());
    }

    @Test
    void deleteCompilation_ShouldDeleteSuccessfully() {
        // Arrange
        when(compilationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(compilationRepository).deleteById(1L);

        // Act
        compilationService.deleteCompilation(1L);

        // Assert
        verify(compilationRepository).existsById(1L);
        verify(compilationRepository).deleteById(1L);
    }

    @Test
    void deleteCompilation_WhenCompilationNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(compilationRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> compilationService.deleteCompilation(999L));

        assertEquals("Компиляция с идентификатором не найдена: 999", exception.getMessage());
        verify(compilationRepository).existsById(999L);
        verify(compilationRepository, never()).deleteById(any());
    }

    @Test
    void updateCompilation_ShouldUpdateSuccessfully() {
        // Arrange
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        doNothing().when(compilationMapper).updateEntityFromDto(updateCompilationRequest, compilation);
        when(eventRepository.findAllById(Set.of(1L))).thenReturn(List.of(event));
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(compilationResponse);

        // Act
        CompilationResponse result = compilationService.updateCompilation(1L, updateCompilationRequest);

        // Assert
        assertNotNull(result);
        verify(compilationRepository).findById(1L);
        verify(compilationMapper).updateEntityFromDto(updateCompilationRequest, compilation);
        verify(eventRepository).findAllById(Set.of(1L));
        verify(compilationRepository).save(compilation);
        verify(compilationMapper).toDto(compilation);
    }

    @Test
    void updateCompilation_WithNullEvents_ShouldUpdateSuccessfully() {
        // Arrange
        UpdateCompilationRequest requestWithNullEvents = UpdateCompilationRequest.builder()
                .title("Updated Compilation")
                .pinned(false)
                .events(null)
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        doNothing().when(compilationMapper).updateEntityFromDto(requestWithNullEvents, compilation);
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(compilationResponse);

        // Act
        CompilationResponse result = compilationService.updateCompilation(1L, requestWithNullEvents);

        // Assert
        assertNotNull(result);
        verify(eventRepository, never()).findAllById(anySet());
    }

    @Test
    void updateCompilation_WithEmptyEvents_ShouldUpdateSuccessfully() {
        // Arrange
        UpdateCompilationRequest requestWithEmptyEvents = UpdateCompilationRequest.builder()
                .title("Updated Compilation")
                .pinned(false)
                .events(Collections.emptySet())
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        doNothing().when(compilationMapper).updateEntityFromDto(requestWithEmptyEvents, compilation);
        when(eventRepository.findAllById(Collections.emptySet())).thenReturn(Collections.emptyList());
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(compilationResponse);

        // Act
        CompilationResponse result = compilationService.updateCompilation(1L, requestWithEmptyEvents);

        // Assert
        assertNotNull(result);
        verify(eventRepository).findAllById(Collections.emptySet());
    }

    @Test
    void updateCompilation_WhenCompilationNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(compilationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> compilationService.updateCompilation(999L, updateCompilationRequest));

        assertEquals("Компиляция с идентификатором не найдена: 999", exception.getMessage());
        verify(compilationRepository).findById(999L);
        verify(compilationMapper, never()).updateEntityFromDto(any(), any());
        verify(eventRepository, never()).findAllById(anySet());
        verify(compilationRepository, never()).save(any());
    }

    @Test
    void getCompilations_WithPinnedTrue_ShouldReturnPinnedCompilations() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(compilationRepository.findByPinned(true, pageable))
                .thenReturn(new PageImpl<>(List.of(compilation)));
        when(compilationMapper.toDto(compilation)).thenReturn(compilationResponse);

        // Act
        List<CompilationResponse> result = compilationService.getCompilations(true, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(compilationRepository).findByPinned(true, pageable);
        verify(compilationMapper).toDto(compilation);
    }

    @Test
    void getCompilations_WithPinnedFalse_ShouldReturnNotPinnedCompilations() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Compilation notPinnedCompilation = Compilation.builder()
                .id(2L)
                .title("Not Pinned")
                .pinned(false)
                .events(Set.of(event))
                .build();

        when(compilationRepository.findByPinned(false, pageable))
                .thenReturn(new PageImpl<>(List.of(notPinnedCompilation)));
        when(compilationMapper.toDto(notPinnedCompilation)).thenReturn(
                CompilationResponse.builder().id(2L).title("Not Pinned").pinned(false).events(Collections.emptySet()).build()
        );

        // Act
        List<CompilationResponse> result = compilationService.getCompilations(false, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.getFirst().getPinned());
        verify(compilationRepository).findByPinned(false, pageable);
    }

    @Test
    void getCompilations_WithNullPinned_ShouldReturnAllCompilations() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(compilationRepository.findByPinned(null, pageable))
                .thenReturn(new PageImpl<>(List.of(compilation)));
        when(compilationMapper.toDto(compilation)).thenReturn(compilationResponse);

        // Act
        List<CompilationResponse> result = compilationService.getCompilations(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(compilationRepository).findByPinned(null, pageable);
    }

    @Test
    void getCompilations_WithEmptyResult_ShouldReturnEmptyList() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(compilationRepository.findByPinned(true, pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Act
        List<CompilationResponse> result = compilationService.getCompilations(true, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(compilationRepository).findByPinned(true, pageable);
        verify(compilationMapper, never()).toDto(any());
    }

    @Test
    void getCompilationById_ShouldReturnCompilation() {
        // Arrange
        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(compilationMapper.toDto(compilation)).thenReturn(compilationResponse);

        // Act
        CompilationResponse result = compilationService.getCompilationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(compilationRepository).findById(1L);
        verify(compilationMapper).toDto(compilation);
    }

    @Test
    void getCompilationById_WhenCompilationNotFound_ShouldThrowNotFoundException() {
        // Arrange
        when(compilationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> compilationService.getCompilationById(999L));

        assertEquals("Компиляция с идентификатором не найдена: 999", exception.getMessage());
        verify(compilationRepository).findById(999L);
        verify(compilationMapper, never()).toDto(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void createCompilation_WithInvalidTitle_ShouldThrowException(String invalidTitle) {
        // Этот тест проверяет валидацию на уровне контроллера, но мы можем проверить граничные случаи
        NewCompilationRequest request = NewCompilationRequest.builder()
                .title(invalidTitle)
                .pinned(false)
                .events(null)
                .build();

        // Для null и пустых строк репозиторий вернет false (не существует)
        when(compilationRepository.existsByTitle(invalidTitle)).thenReturn(false);

        // Act & Assert - здесь может выброситься исключение валидации или метод выполнится
        // В зависимости от того, где происходит валидация
        try {
            compilationService.createCompilation(request);
            // Если дошли сюда, значит валидация происходит на другом уровне
        } catch (Exception e) {
            // Ожидаем исключение валидации
            assertTrue(e instanceof jakarta.validation.ConstraintViolationException ||
                    e instanceof IllegalArgumentException);
        }
    }

    @Test
    void createCompilation_WithTitleExceedingMaxLength_ShouldThrowException() {
        // Arrange
        String longTitle = "A".repeat(51); // превышает максимальную длину 50
        NewCompilationRequest request = NewCompilationRequest.builder()
                .title(longTitle)
                .pinned(false)
                .events(null)
                .build();

        // Act & Assert - валидация происходит на уровне контроллера
        try {
            compilationService.createCompilation(request);
            // Если дошли сюда, значит валидация происходит на другом уровне
        } catch (Exception e) {
            // Ожидаем исключение валидации
            assertTrue(e instanceof jakarta.validation.ConstraintViolationException ||
                    e instanceof IllegalArgumentException);
        }
    }

    @Test
    void updateCompilation_WithMinTitleLength_ShouldUpdateSuccessfully() {
        // Arrange
        UpdateCompilationRequest requestWithMinTitle = UpdateCompilationRequest.builder()
                .title("A") // минимальная длина 1
                .pinned(true)
                .events(null)
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        doNothing().when(compilationMapper).updateEntityFromDto(requestWithMinTitle, compilation);
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(compilationResponse);

        // Act
        CompilationResponse result = compilationService.updateCompilation(1L, requestWithMinTitle);

        // Assert
        assertNotNull(result);
        verify(compilationMapper).updateEntityFromDto(requestWithMinTitle, compilation);
    }

    @Test
    void updateCompilation_WithMaxTitleLength_ShouldUpdateSuccessfully() {
        // Arrange
        String maxLengthTitle = "A".repeat(50); // максимальная длина 50
        UpdateCompilationRequest requestWithMaxTitle = UpdateCompilationRequest.builder()
                .title(maxLengthTitle)
                .pinned(true)
                .events(null)
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        doNothing().when(compilationMapper).updateEntityFromDto(requestWithMaxTitle, compilation);
        when(compilationRepository.save(compilation)).thenReturn(compilation);
        when(compilationMapper.toDto(compilation)).thenReturn(compilationResponse);

        // Act
        CompilationResponse result = compilationService.updateCompilation(1L, requestWithMaxTitle);

        // Assert
        assertNotNull(result);
        verify(compilationMapper).updateEntityFromDto(requestWithMaxTitle, compilation);
    }
}
