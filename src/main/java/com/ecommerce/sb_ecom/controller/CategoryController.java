package com.ecommerce.sb_ecom.controller;


import com.ecommerce.sb_ecom.config.AppConstants;
import com.ecommerce.sb_ecom.payload.CategoryDto;
import com.ecommerce.sb_ecom.payload.CategoryResponse;
import com.ecommerce.sb_ecom.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class CategoryController {
    CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    //example for requestParam
    @GetMapping("/echo")
    public ResponseEntity<String> echoMessage(@RequestParam(name="message") String message){
        return new ResponseEntity<>("Echoed message:- "+message,HttpStatus.OK);
    }

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name="pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false)Integer pageNumber,
            @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name="sortBy",defaultValue = AppConstants.SORT_CATEGORIES_BY,required = false) String sortBy,
            @RequestParam(name="sortOrder",defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder
    ){
        CategoryResponse categoryResponse= categoryService.getAllCategories(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(categoryResponse,HttpStatus.OK);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto categoryDto){
        CategoryDto savedCategoryDto=categoryService.createCategory(categoryDto);
        return new ResponseEntity<>(savedCategoryDto,HttpStatus.CREATED);
    }

    @DeleteMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDto> deleteCategory(@PathVariable Long categoryId){
            CategoryDto categoryDto=categoryService.deleteCategory(categoryId);
            return new ResponseEntity<>(categoryDto, HttpStatus.OK);
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(@Valid @RequestBody CategoryDto categoryDto,@PathVariable long categoryId){
            CategoryDto updatedCategoryDto=categoryService.updateCategory(categoryDto, categoryId);
            return new ResponseEntity<>(updatedCategoryDto, HttpStatus.OK);
    }

}
