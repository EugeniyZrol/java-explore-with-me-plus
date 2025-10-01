package ewm.categories.mapper;

import ewm.categories.dto.CategoryDto;
import ewm.categories.dto.NewCategoryDto;
import ewm.categories.model.Category;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return new Category(newCategoryDto.getName());
    }

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}