package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.payload.CartDto;

import java.util.List;

public interface CartService {
    CartDto addProduct(Long productId, Integer quantity);

    List<CartDto> getAllCarts();
}
