package com.ecommerce.project.controller;


import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDto;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

//    creating object
    private CategoryService categoryService;

    //constructor
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

//    endpoints

//    @GetMapping("/public/categories")
    @RequestMapping(value="/public/categories",method=RequestMethod.GET)
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name="pageNumber",defaultValue = AppConstants.Page_Number,required = false) Integer pageNumber,
            @RequestParam(name="pageSize",defaultValue = AppConstants.Page_Size,required = false) Integer pageSize){

        CategoryResponse categoryResponse = categoryService.getAllCategories(pageNumber,pageSize);
        return new ResponseEntity<>(categoryResponse,HttpStatus.OK);
    }


//    @PostMapping("/public/categories")
@RequestMapping(value="/public/categories",method=RequestMethod.POST)
public ResponseEntity<CategoryDto> CreateCategory(@Valid @RequestBody CategoryDto categoryDto){
        CategoryDto savedCategoryDto = categoryService.createCategory(categoryDto);

    return new ResponseEntity<>(savedCategoryDto,HttpStatus.CREATED);
    }

//    @DeleteMapping("/admin/categories/{categoryId}")
@RequestMapping(value="/admin/categories/{categoryId}",method=RequestMethod.DELETE)
public ResponseEntity<CategoryDto> deleteCategory(@PathVariable Long categoryId){
            CategoryDto status = categoryService.deleteCategory(categoryId);
            return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(@RequestBody CategoryDto categoryDto,
                                                      @PathVariable Long categoryId){
            CategoryDto savedCategoryDto = categoryService.updateCategory(categoryDto,categoryId);
            return new ResponseEntity<>(savedCategoryDto, HttpStatus.OK);
    }

}
