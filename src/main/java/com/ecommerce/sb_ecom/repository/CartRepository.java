package com.ecommerce.sb_ecom.repository;

import com.ecommerce.sb_ecom.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart,Long> {
}
