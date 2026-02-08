package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.payload.OrderDto;
import jakarta.transaction.Transactional;

public interface OrderService {
    @Transactional
    OrderDto placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);
}
