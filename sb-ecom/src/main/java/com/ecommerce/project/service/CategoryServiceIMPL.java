package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ApiException;
import com.ecommerce.project.exceptions.EmptyListException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDto;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceIMPL implements CategoryService{


    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public CategoryResponse getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        if(categories.isEmpty()){
            throw new EmptyListException("There are no Categories present!!");
        }
        List<CategoryDto> categoryDTOS = categories.stream()
                .map(category->modelMapper.map(category,CategoryDto.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        return categoryResponse;
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {

        Category category = modelMapper.map(categoryDto,Category.class);

        Category categoryFromDB = categoryRepository.findByCategoryName(category.getCategoryName());
        if(categoryFromDB !=null){
            throw new ApiException("Category with name "+categoryDto.getCategoryName()+" already exists!!");
        }

        Category savedCategory = categoryRepository.save(category);

        CategoryDto savedCategoryDTO = modelMapper.map(savedCategory,CategoryDto.class);

        return savedCategoryDTO;
    }

    @Override
    public CategoryDto deleteCategory(Long categoryId) {
    Category deleteCategory = categoryRepository.findById(categoryId)
        .orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));

    categoryRepository.delete(deleteCategory);

        return modelMapper.map(categoryId, CategoryDto.class);
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, Long categoryId) {

        Category category = modelMapper.map(categoryDto,Category.class);

        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));

        category.setCategoryId(categoryId);
        savedCategory=categoryRepository.save(category);
        CategoryDto savedCategoryDTO = modelMapper.map(savedCategory,CategoryDto.class);
        return savedCategoryDTO;
    }


}
