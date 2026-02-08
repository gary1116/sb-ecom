package com.ecommerce.sb_ecom.repository;

import com.ecommerce.sb_ecom.model.Cart;
import com.ecommerce.sb_ecom.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {

    @Query("SELECT c from Cart c WHERE c.user.email=?1")
    Cart findCartByEmail(String email);



    @Query("SELECT c from Cart c WHERE c.user.email=?1 AND c.id=?2")
    Cart findCartByEmailAndCartId(String emailId, Long cartId);


    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p WHERE p.id=?1")
    List<Cart> findCartsByProductId(Long productId);
}
