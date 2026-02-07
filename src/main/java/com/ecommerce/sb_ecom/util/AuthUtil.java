package com.ecommerce.sb_ecom.util;

import com.ecommerce.sb_ecom.model.User;
import com.ecommerce.sb_ecom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    @Autowired
    UserRepository userRepository;

    public String loggedInEmail(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();

        User user=userRepository.findByUsername(authentication.getName())
                .orElseThrow(()-> new UsernameNotFoundException("User not Found!!"));

        return user.getEmail();
    }

    public User loggedInUser(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();

        User user= userRepository.findByUsername(authentication.getName())
                .orElseThrow(()->new UsernameNotFoundException("User Not Found!!"));

        return user;
    }


}
