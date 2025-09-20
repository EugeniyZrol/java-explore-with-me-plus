package ewm.category.service;

import ewm.category.dto.CategoryDto;
import ewm.category.mapper.CategoryMapper;
import ewm.category.model.Category;
import ewm.category.repository.CategoryRepository;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).stream()
                .map(categoryMapper::toCategoryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long categoryId) {
        checkCategoryExists(categoryId);
        return categoryMapper.toCategoryDto(categoryRepository.findById(categoryId).get());
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        checkUniqueCategoryName(categoryDto.getName());
        Category category = categoryMapper.toCategory(categoryDto);
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        checkCategoryExists(categoryId);
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        checkCategoryExists(categoryId);
        checkUniqueCategoryName(categoryDto.getName());
        Category category = categoryMapper.toCategory(categoryDto);
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public void checkCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException(String.format("Category with id %d does not exist", categoryId));
        }
    }

    private void checkUniqueCategoryName(String categoryName) {
        if (categoryRepository.existsByName(categoryName)) {
            throw new ConflictException(String.format("Category with name %s already exists", categoryName));
        }
    }
}
