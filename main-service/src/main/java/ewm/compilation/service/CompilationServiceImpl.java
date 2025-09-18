package ewm.compilation.service;

import ewm.compilation.dto.CompilationResponse;
import ewm.compilation.dto.NewCompilationRequest;
import ewm.compilation.dto.UpdateCompilationRequest;
import ewm.compilation.mapper.CompilationMapper;
import ewm.compilation.model.Compilation;
import ewm.compilation.repository.CompilationRepository;
import ewm.event.model.Event;
import ewm.event.repository.EventRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationResponse createCompilation(NewCompilationRequest request) {
        if (compilationRepository.existsByTitle(request.getTitle())) {
            throw new ConflictException("Компиляция с названием уже существует: " + request.getTitle());
        }

        Compilation compilation = compilationMapper.toEntity(request);

        if (request.getEvents() != null && !request.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(request.getEvents()));
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Компиляция с идентификатором не найдена: " + compId);
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationResponse updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Компиляция с идентификатором не найдена: " + compId));

        compilationMapper.updateEntityFromDto(request, compilation);

        if (request.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(request.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updateCompilation = compilationRepository.save(compilation);
        return compilationMapper.toDto(updateCompilation);
    }

    @Override
    public List<CompilationResponse> getCompilations(Boolean pinned, Pageable pageable) {
        return compilationRepository.findByPinned(pinned, pageable)
                .stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationResponse getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Компиляция с идентификатором не найдена: " + compId));
        return compilationMapper.toDto(compilation);
    }
}