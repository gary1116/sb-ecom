package com.ecommerce.sb_ecom.exceptions;

public class NoDataException extends RuntimeException{

    String message;

    public NoDataException(){

    }

    public NoDataException(String message){
        super(message);
    }

}
