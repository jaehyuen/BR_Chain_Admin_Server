package com.brchain.account.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.account.dto.LoginDto;
import com.brchain.account.dto.RefreshTokenDto;
import com.brchain.account.dto.RegisterDto;
import com.brchain.account.service.AuthService;
import com.brchain.account.service.RefreshTokenService;
import com.brchain.common.dto.ResultDto;
import com.brchain.core.chaincode.service.ChaincodeService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService         authService;
	private final RefreshTokenService refreshTokenService;
	private final ChaincodeService cc;
	
	
	@ApiOperation(value = "회원가입", notes = "회원가입 API")
	@PostMapping("/register")
	public ResponseEntity<ResultDto> register(@ApiParam(value = "회원가입 정보 DTO", required = true) @RequestBody RegisterDto registerDto) {

		return ResponseEntity.status(HttpStatus.OK).body(authService.register(registerDto));
	}

	@ApiOperation(value = "로그인", notes = "로그인 API")
	@PostMapping("/login")
	public ResponseEntity<ResultDto> login(@ApiParam(value = "로그인 정보 DTO", required = true) @RequestBody LoginDto loginDto) {

		return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginDto));
	}

	@ApiOperation(value = "JWT 토큰 재발급", notes = "JWT 토큰 재발급 API")
	@PostMapping("/refresh")
	public ResponseEntity<ResultDto> refreshToken(@ApiParam(value = "리프레시 토큰 정보 DTO", required = true) @RequestBody RefreshTokenDto refreshTokenDto) {

		return ResponseEntity.status(HttpStatus.OK).body(authService.refreshToken(refreshTokenDto));

	}

	@ApiOperation(value = "로그아웃", notes = "로그아웃 API")
	@PostMapping("/logout")
	public ResponseEntity<ResultDto> logout(@ApiParam(value = "리프레시 토큰 정보 DTO", required = true) @RequestBody RefreshTokenDto refreshTokenDto) {

		return ResponseEntity.status(HttpStatus.OK).body(refreshTokenService.deleteRefreshToken(refreshTokenDto.getRefreshToken()));
	}
	

	@GetMapping("/test")
	public ResponseEntity<String> getChannelSummaryList() {

		cc.getCcList();
		return ResponseEntity.status(HttpStatus.OK).body("");

	}
	
}


