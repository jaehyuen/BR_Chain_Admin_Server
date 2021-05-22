package com.brchain.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.util.BrchainStatusCode;
import com.brchain.core.util.Util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ControllerAdvice
public class ControllerExceptionHandler {

	private final Util util;

	@ExceptionHandler(BrchainException.class)
	protected ResponseEntity<ResultDto<String>> handleBrchainExceptionException(BrchainException e) {
		e.printStackTrace();
		ResponseEntity<ResultDto<String>> response;

		if (e.getStatus()
			.getCode()
			.startsWith("803")) {
			response = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(util.setResult(e.getStatus(), e.getMessage()));
		} else if (e.getStatus()
			.getCode()
			.startsWith("8")) {
			response = ResponseEntity.status(HttpStatus.CONFLICT)
				.body(util.setResult(e.getStatus(), e.getMessage()));

		} else {
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(util.setResult(e.getStatus(), e.getMessage()));
		}

		return response;
	}

	// 로그인 에러
	@ExceptionHandler(BadCredentialsException.class)
	protected ResponseEntity<ResultDto<String>> handleBadCredentialsExceptionException(BadCredentialsException e) {
		e.printStackTrace();
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(util.setResult(BrchainStatusCode.LOGIN_ERROR, e.getMessage()));
	}
	
	public static abstract class ErrorResultDto extends ResultDto<String> {
		@Schema(example = "errer message")
		private String resultMessage;
		@Schema(example = "false")
		private boolean resultFlag;
	}

	public static class Error401ResultDto extends ErrorResultDto {
		@Schema(example = "803X")
		private String resultCode;

	}

	public static class Error403ResultDto extends ErrorResultDto {
		@Schema(example = "8XXX")
		private String resultCode;

	}

	public static class Error500ResultDto extends ErrorResultDto {
		@Schema(example = "9XXX")
		private String resultCode;

	}

}
