package com.brchain.common.exception;

import com.brchain.core.util.BrchainStatusCode;

import lombok.Getter;

public class BrchainException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8860013712221028748L;

	@Getter
	private BrchainStatusCode status;

	public BrchainException(Exception exception, BrchainStatusCode status) {
		super(exception);
		this.status = status;
	}

	public BrchainException(String exMessage, BrchainStatusCode status) {
		super(exMessage);
		this.status = status;
	}

}