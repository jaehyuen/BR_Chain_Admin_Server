package com.brchain.account.service;

import java.time.Instant;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.account.dto.AuthDto;
import com.brchain.account.dto.LoginDto;
import com.brchain.account.dto.RefreshTokenRequest;
import com.brchain.account.dto.UserDto;
import com.brchain.account.entity.UserEntity;
import com.brchain.account.repository.UserRepository;
import com.brchain.common.dto.ResultDto;
import com.brchain.common.security.JwtProvider;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;
	private final Util util;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ResultDto register(UserDto userDto) {

		try {

			logger.info("this is userDto : " + userDto);

			UserEntity userEntity = new UserEntity();

			userEntity.setUserName(userDto.getUserName());
			userEntity.setUserId(userDto.getUserId());
			userEntity.setEmail(userDto.getEmail());
			userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
			userEntity.setActive(false);

			logger.info("this is userEntity : " + userEntity);

			userRepository.save(userEntity);
		} catch (Exception e) {

			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);

		}
		return util.setResult("0000", true, "Success Register", "");
	}

	public ResultDto login(LoginDto loginDto) {

		AuthDto authDto = null;
		try {

			Authentication authenticate = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginDto.getUserId(), loginDto.getPassword()));

			SecurityContextHolder.getContext().setAuthentication(authenticate);

			String token = jwtProvider.generateToken(authenticate);
			authDto = AuthDto.builder().token(token).refreshToken(refreshTokenService.generateRefreshToken().getToken())
					.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
					.userId(loginDto.getUserId()).build();

		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return util.setResult("9999", false, e.getMessage(), null);
		}

		return util.setResult("0000", true, "Success Login", authDto);
	}

	public AuthDto refreshToken(RefreshTokenRequest refreshTokenRequest) {
		refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
		String token = jwtProvider.generateTokenWithUserName(refreshTokenRequest.getUsername());
		return AuthDto.builder().token(token).refreshToken(refreshTokenRequest.getRefreshToken())
				.expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
				.userId(refreshTokenRequest.getUsername()).build();
	}

	@Transactional(readOnly = true)
	public UserEntity getCurrentUser() {
		org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		return userRepository.findByUserId(principal.getUsername())
				.orElseThrow(() -> new EntityNotFoundException("User name not found - " + principal.getUsername()));
	}

	public boolean isLoggedIn() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
	}
}