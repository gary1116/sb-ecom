package com.ecommerce.sb_ecom.security.jwt;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    private String username;
    @NotBlank
    @Email
    private String email;
    private Set<String> role;
    @NotBlank
    @Size(min = 5,max = 20)
    private String password;


}
