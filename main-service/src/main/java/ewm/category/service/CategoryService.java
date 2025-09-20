package ewm.category.service;


import ewm.category.dto.CategoryDto;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CategoryService {
    @Transactional(readOnly = true)
    List<CategoryDto> getCategories(Pageable pageable);

    @Transactional(readOnly = true)
    CategoryDto getCategory(Long categoryId);

    CategoryDto createCategory(CategoryDto categoryDto);

    void deleteCategory(Long categoryId);

    CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto);

    void checkCategoryExists(Long categoryId);
}
