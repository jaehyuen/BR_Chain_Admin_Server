package com.brchain.account.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.brchain.account.dto.AuthDto;
import com.brchain.account.dto.LoginDto;
import com.brchain.account.dto.RefreshTokenRequest;
import com.brchain.account.dto.UserDto;
import com.brchain.account.service.AuthService;
import com.brchain.account.service.RefreshTokenService;
import com.brchain.common.dto.ResultDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final RefreshTokenService refreshTokenService;

	@PostMapping("/register")
	public ResponseEntity<ResultDto> register(@RequestBody UserDto userDto) {

		return ResponseEntity.status(HttpStatus.OK).body(authService.register(userDto));
	}

	@PostMapping("/login")
	public ResponseEntity<ResultDto> login(@RequestBody LoginDto loginDto) {

		return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginDto));
	}

	@PostMapping("/refresh")
	public AuthDto refreshTokens(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
		return authService.refreshToken(refreshTokenRequest);
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
		refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());
		return ResponseEntity.status(HttpStatus.OK).body("Refresh Token Deleted Successfully!!");
	}
}