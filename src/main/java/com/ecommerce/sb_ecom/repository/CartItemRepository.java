package com.ecommerce.sb_ecom.repository;

import com.ecommerce.sb_ecom.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
