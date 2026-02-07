package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.ApiException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Cart;
import com.ecommerce.sb_ecom.model.CartItem;
import com.ecommerce.sb_ecom.model.Product;
import com.ecommerce.sb_ecom.payload.CartDto;
import com.ecommerce.sb_ecom.payload.ProductDTO;
import com.ecommerce.sb_ecom.repository.CartItemRepository;
import com.ecommerce.sb_ecom.repository.CartRepository;
import com.ecommerce.sb_ecom.repository.ProductRepository;
import com.ecommerce.sb_ecom.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class CartServiceImpl implements CartService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    AuthUtil authUtil;


    @Override
    public CartDto addProduct(Long productId, Integer quantity) {
        Cart cart =createCart();
        Product product= productRepository.findById(productId)
        .orElseThrow(()-> new ResourceNotFoundException("Product","productId", productId));

        CartItem cartItem= cartRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );

        if(cartItem!=null){
            throw new ApiException("Product "+product.getProductName()+"already exists in the cart");
        }

        if(product.getQuantity()==0){
            throw new ApiException(product.getProductName()+"is not available");
        }

        if(product.getQuantity()<quantity){
            throw new ApiException("Please, make an order of the "+product.getProductName() +" less than or equal to the quantity "+product.getQuantity());
        }

        CartItem newCartItem= new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());


        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());


        cart.setTotalPrice(cart.getTotalPrice()+(product.getSpecialPrice()*quantity));


        cartRepository.save(cart);

        CartDto cartDto=modelMapper.map(cart,CartDto.class);

        List<CartItem> cartItems=cart.getCartItems();

        Stream<ProductDTO> productDTOStrem=cartItems.stream().map(
                item->{
                    ProductDTO map= modelMapper.map(item.getProduct(),ProductDTO.class);
                    map.setQuantity(item.getQuantity());

                    return map;
                }
        );

        cartDto.setProducts(productDTOStrem.toList());

        return cartDto;
    }

    @Override
    public List<CartDto> getAllCarts() {

        List<Cart> carts=cartRepository.findAll();

        if(carts.size()==0){
            throw new ApiException("No cart exist");
        }

        List<CartDto> cartDtos= carts.stream().map(
                item->{
                    CartDto cartDto=modelMapper.map(item,CartDto.class);
                    List<ProductDTO> products=item.getCartItems().stream()
                            .map(p->modelMapper.map(p.getProduct(),ProductDTO.class))
                            .collect(Collectors.toList());

                    cartDto.setProducts(products);
                    return cartDto;
                }
        ).collect(Collectors.toList());

        return cartDtos;
    }


    private Cart createCart(){
        Cart userCart=cartRepository.findCartByEmail(authUtil.loggedInEmail());

        if(userCart !=null){
            return userCart;
        }

        Cart cart= new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart= cartRepository.save(cart);
        return newCart;
    }
}
