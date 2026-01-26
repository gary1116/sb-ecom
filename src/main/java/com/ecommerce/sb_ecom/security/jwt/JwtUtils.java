package com.ecommerce.sb_ecom.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger= LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;
    @Value("${spring.app.jwtSecret}")
    private String JwtSecret;
    //getting JWT from Header
    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken= request.getHeader("Authorization");
        logger.debug("Authorization header: {}",bearerToken);
        if(bearerToken!=null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7); //Remove Bearer prefix
        }
        return null;
    }

    //Generating Token from Username
    public String generateTokenFromUsername(UserDetails userDetails){
        String username= userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime()+jwtExpirationMs)))
                .signWith(key())
                .compact();
    }

    //Getting Username from JWT Token
    public String getUsernameFromJWTToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }
    //Generate Signing Key
    public Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(JwtSecret)
        );
    }

    // Validate JWT Token
    public boolean validateJwtToken(String authToken){

        try{
            System.out.println("validate");
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;

        }catch(MalformedJwtException exception){
                logger.error("Invalid JWT token:{}", exception.getMessage());
        }catch(ExpiredJwtException exception){
            logger.error("JWT token is expired:{}", exception.getMessage());
        }catch(UnsupportedJwtException exception){
            logger.error("JWT token is not supported:{}", exception.getMessage());
        }catch(IllegalArgumentException exception){
            logger.error("JWT claims string is empty:{}", exception.getMessage());
        }
        return false;
    }

}
