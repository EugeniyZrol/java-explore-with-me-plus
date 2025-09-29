package ewm.compilation.controller;

import ewm.compilation.dto.CompilationResponse;
import ewm.compilation.service.CompilationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationResponse> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {

        int validFrom = from < 0 ? 0 : from;
        int validSize = size <= 0 ? 10 : size;

        Pageable pageable = PageRequest.of(validFrom / validSize, validSize);

        return compilationService.getCompilations(pinned, pageable);
    }

    @GetMapping("/{compId}")
    public CompilationResponse getCompilation(@PathVariable Long compId) {
        return compilationService.getCompilationById(compId);
    }
}