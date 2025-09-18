package ewm.compilation.repository;

import ewm.compilation.model.Compilation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query("Select c FROM Compilation c WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    Page<Compilation> findByPinned(@Param("pinned") Boolean pinned, Pageable pageable);

    boolean existsByTitle(String title);

    boolean existsById(@NonNull Long compId);
}
