package com.ecommerce.microcommerce.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ProduitGratuitException extends RuntimeException {
	
	public ProduitGratuitException(String excDesc) {
		super(excDesc);
	}

}
