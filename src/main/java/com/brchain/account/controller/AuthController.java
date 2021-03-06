package com.brchain.account.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.account.dto.LoginDto;
import com.brchain.account.dto.RefreshTokenDto;
import com.brchain.account.dto.RegisterDto;
import com.brchain.account.dto.TokenDto;
import com.brchain.account.service.AuthService;
import com.brchain.account.service.RefreshTokenService;
import com.brchain.common.dto.ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error401ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error403ResultDto;
import com.brchain.common.exception.ControllerExceptionHandler.Error500ResultDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

//TODO 스프링시큐리티로 권한 관리 추가
@SuppressWarnings("rawtypes")
public class AuthController {

	private final AuthService         authService;
	private final RefreshTokenService refreshTokenService;
//	private final ChaincodeService cc;
//	private final ConInfoRepository conInfoRepository;
	
	@Operation(summary = "회원가입", description = "회원가입 API",responses={
			 @ApiResponse(responseCode="200", content = @Content(schema = @Schema(implementation =ResultDto.class))),
			 @ApiResponse(responseCode="401", content = @Content(schema = @Schema(implementation =Error401ResultDto.class))),
			 @ApiResponse(responseCode="403", content = @Content(schema = @Schema(implementation =Error403ResultDto.class))),
			 @ApiResponse(responseCode="500", content = @Content(schema = @Schema(implementation =Error500ResultDto.class)))
	})
	@PostMapping("/register")
	public ResponseEntity<ResultDto> register(@Parameter(description = "회원가입 정보 DTO", required = true) @RequestBody RegisterDto registerDto) {

		return ResponseEntity.status(HttpStatus.OK).body(authService.register(registerDto));
	}

	@Operation(summary = "로그인", description = "로그인 API",responses={
			 @ApiResponse(responseCode="200", content = @Content(schema = @Schema(implementation =TokenResultDto.class))),
			 @ApiResponse(responseCode="401", content = @Content(schema = @Schema(implementation =Error401ResultDto.class))),
			 @ApiResponse(responseCode="403", content = @Content(schema = @Schema(implementation =Error403ResultDto.class))),
			 @ApiResponse(responseCode="500", content = @Content(schema = @Schema(implementation =Error500ResultDto.class)))
	})
	@PostMapping("/login")
	public ResponseEntity<ResultDto> login(@Parameter(required = true) @RequestBody LoginDto loginDto) {

		return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginDto));
	}

	@Operation(summary = "JWT 토큰 재발급", description = "JWT 토큰 재발급 API",responses={
			 @ApiResponse(responseCode="200", content = @Content(schema = @Schema(implementation =TokenResultDto.class))),
			 @ApiResponse(responseCode="401", content = @Content(schema = @Schema(implementation =Error401ResultDto.class))),
			 @ApiResponse(responseCode="403", content = @Content(schema = @Schema(implementation =Error403ResultDto.class))),
			 @ApiResponse(responseCode="500", content = @Content(schema = @Schema(implementation =Error500ResultDto.class)))
	})
	@PostMapping("/refresh")
	public ResponseEntity<ResultDto> refreshToken(@Parameter(description = "리프레시 토큰 정보 DTO", required = true) @RequestBody RefreshTokenDto refreshTokenDto) {

		return ResponseEntity.status(HttpStatus.OK).body(authService.refreshToken(refreshTokenDto));

	}

	@Operation(summary = "로그아웃", description = "로그아웃 API",responses={
			 @ApiResponse(responseCode="200", content = @Content(schema = @Schema(implementation =ResultDto.class))),
			 @ApiResponse(responseCode="401", content = @Content(schema = @Schema(implementation =Error401ResultDto.class))),
			 @ApiResponse(responseCode="403", content = @Content(schema = @Schema(implementation =Error403ResultDto.class))),
			 @ApiResponse(responseCode="500", content = @Content(schema = @Schema(implementation =Error500ResultDto.class)))
	})
	@PostMapping("/logout")
	public ResponseEntity<ResultDto> logout(@Parameter(description = "리프레시 토큰 정보 DTO", required = true) @RequestBody RefreshTokenDto refreshTokenDto) {

		return ResponseEntity.status(HttpStatus.OK).body(refreshTokenService.deleteRefreshToken(refreshTokenDto.getRefreshToken()));
	}

	private class TokenResultDto extends ResultDto<TokenDto> {
	}
	



//	@GetMapping("/test")
//	public ResponseEntity<List<CcSummaryDto>> getChannelSummaryList() {
//
//		conInfoRepository.testQuery();
//		return ResponseEntity.status(HttpStatus.OK).body(conInfoRepository.testQuery());
//
//	}
	
}


