package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.ApiException;
import com.ecommerce.sb_ecom.exceptions.NoDataException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.payload.CategoryDto;
import com.ecommerce.sb_ecom.payload.CategoryResponse;
import com.ecommerce.sb_ecom.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;



import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService{
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize,String sortBy, String sortOrder) {

        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Category> categoryPage= categoryRepository.findAll(pageDetails);
        List<Category> list = categoryPage.getContent();
        if(list.size()==0){
            throw new NoDataException("the list is empty please add a new Category!");
        }
        List<CategoryDto> categoryDTOS=list.stream().
                                            map(category->modelMapper.map(category,CategoryDto.class))
                                            .toList();
        CategoryResponse categoryResponse= new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);

        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());

        return categoryResponse;
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category=modelMapper.map(categoryDto,Category.class);
        Category categoryFromDb=categoryRepository.findByCategoryName(category.getCategoryName());
        if(categoryFromDb !=null)
            throw new ApiException("Category with the name "+ category.getCategoryName()+" already exists!!");
        Category savedCategory=categoryRepository.save(category);
        CategoryDto savedCategoryDto =modelMapper.map(savedCategory,CategoryDto.class);
        return savedCategoryDto;
    }

    @Override
    public CategoryDto deleteCategory(Long categoryId) {
        Category category=categoryRepository.findById(categoryId).
                          orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));
        categoryRepository.delete(category);
        return modelMapper.map(category,CategoryDto.class);
    }
    
    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));

// copy only updatable fields from dto → entity
        category.setCategoryName(categoryDto.getCategoryName());

        Category saved = categoryRepository.save(category);
        return modelMapper.map(saved, CategoryDto.class);
    }
    

}
