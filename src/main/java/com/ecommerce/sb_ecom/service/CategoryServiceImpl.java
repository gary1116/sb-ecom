package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.ApiException;
import com.ecommerce.sb_ecom.exceptions.NoDataException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        List<Category> list = categoryRepository.findAll();
        if(list.size()==0){
            throw new NoDataException("the list is empty please add a new Category!");
        }
        return list;
    }

    @Override
    public void createCategory(Category category) {
        Category savedCategory=categoryRepository.findByCategoryName(category.getCategoryName());
        if(savedCategory !=null)
            throw new ApiException("Category with the name "+ category.getCategoryName()+" already exists!!");
        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category=categoryRepository.findById(categoryId).
                          orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));

        categoryRepository.delete(category);

        return "Category with categoryId:- "+ categoryId+" is deleted successfully";
    }

    @Override
    public Category updateCategory(Category category, long categoryId) {

        Category savedCategory= categoryRepository.findById(categoryId).
                                orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        category.setCategoryId(categoryId);
        savedCategory=categoryRepository.save(category);

        return savedCategory;
    }
}
