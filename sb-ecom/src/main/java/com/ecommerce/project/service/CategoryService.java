package com.ecommerce.project.service;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDto;
import com.ecommerce.project.payload.CategoryResponse;

public interface CategoryService {

    CategoryResponse getAllCategories();
    CategoryDto createCategory(CategoryDto categoryDto);
    CategoryDto deleteCategory(Long categoryId);

    CategoryDto updateCategory(CategoryDto categoryDto, Long categoryId);
}
