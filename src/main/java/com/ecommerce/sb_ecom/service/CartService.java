package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.payload.CartDto;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
    CartDto addProduct(Long productId, Integer quantity);

    List<CartDto> getAllCarts();

    CartDto getCart(String emailId, Long cartId);

    @Transactional
    CartDto updateProductQuantityInCart(long productId, Integer quantity);
}
