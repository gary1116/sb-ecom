package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.ApiException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.*;
import com.ecommerce.sb_ecom.payload.OrderDto;
import com.ecommerce.sb_ecom.payload.OrderItemDTO;
import com.ecommerce.sb_ecom.payload.PaymentDTO;
import com.ecommerce.sb_ecom.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;


    @Override
    @Transactional
    public OrderDto placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {

        Cart cart=cartRepository.findCartByEmail(emailId);

        if(cart==null){
            throw new ResourceNotFoundException("Cart","email",emailId);
        }

        Address address= addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","address Id",addressId));

        Order order= new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment= new Payment(paymentMethod,pgPaymentId,pgStatus,pgResponseMessage,pgName);
        payment.setOrder(order);
        payment=paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder= orderRepository.save(order);
        List<CartItem> cartItems=cart.getCartItems();
        if(cartItems.isEmpty()){
            throw new ApiException("Cart is Empty");
        }
        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItem cartItem: cartItems){
                OrderItem orderItem=new OrderItem();
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setOrderedProductPrice(cartItem.getProductPrice());
                orderItem.setOrder(savedOrder);
                orderItems.add(orderItem);
        }

       orderItems= orderItemRepository.saveAll(orderItems);

        cart.getCartItems().forEach(item->{
            int quantity=item.getQuantity();
            Product product=item.getProduct();
            product.setQuantity(product.getQuantity()-quantity);
            productRepository.save(product);

            cartService.deleteProductFromCart(cart.getCartId(),item.getProduct().getProductId());
        });


        OrderDto orderDto=modelMapper.map(savedOrder,OrderDto.class);

        orderItems.
                forEach(
                        item-> orderDto.getOrderItems().
                                add(modelMapper.map(item, OrderItemDTO.class)));

        orderDto.setAddressId(addressId);

        return orderDto;
    }
}
