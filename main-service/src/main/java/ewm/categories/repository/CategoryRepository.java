package ewm.categories.repository;

import ewm.categories.model.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    @Query("SELECT c FROM Category c ORDER BY c.id DESC")
    List<Category> findCategoriesWithPagination(Pageable pageable);
}
