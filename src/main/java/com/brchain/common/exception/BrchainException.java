package com.brchain.common.exception;

public class BrchainException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8860013712221028748L;
	private String code;

	public BrchainException(String exMessage, Exception exception) {
		super(exMessage, exception);
	}

	public BrchainException(String exMessage) {
		super(exMessage);
	}
	public BrchainException(String exMessage, Exception exception,String code) {
		super(exMessage, exception);
		this.code =code;
	}
}