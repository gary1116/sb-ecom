package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.payload.CartDto;

public interface CartService {
    CartDto addProduct(Long productId, Integer quantity);
}
