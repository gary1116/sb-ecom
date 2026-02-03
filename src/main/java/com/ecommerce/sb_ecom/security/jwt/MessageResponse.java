package com.ecommerce.sb_ecom.security.jwt;

import lombok.Data;

@Data
public class MessageResponse {

    private String message;

    public MessageResponse(String message) {
        this.message=message;
    }
}
