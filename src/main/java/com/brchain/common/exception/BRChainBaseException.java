package com.brchain.common.exception;

public class BRChainBaseException extends RuntimeException {
    public BRChainBaseException(String exMessage, Exception exception) {
        super(exMessage, exception);
    }

    public BRChainBaseException(String exMessage) {
        super(exMessage);
    }
}