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

import com.brchain.account.dto.LoginDto;
import com.brchain.account.dto.RefreshTokenDto;
import com.brchain.account.dto.RegisterDto;
import com.brchain.account.dto.TokenDto;
import com.brchain.account.entity.UserEntity;
import com.brchain.account.repository.UserRepository;
import com.brchain.common.dto.ResultDto;
import com.brchain.common.exception.BrchainException;
import com.brchain.common.security.JwtProvider;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

	private final PasswordEncoder       passwordEncoder;
	private final UserRepository        userRepository;
	private final AuthenticationManager authenticationManager;
	private final JwtProvider           jwtProvider;
	private final RefreshTokenService   refreshTokenService;
	private final Util                  util;

	private Logger                      logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 회원가입 서비스
	 * 
	 * @param registerDto 회원가입 관련 정보 DTO
	 * 
	 * @return 회원가입 결과 DTO
	 */

	public ResultDto<String> register(RegisterDto registerDto) {

		logger.info("this is registerDto : " + registerDto);

		// 유저 정보 엔티티 생성
		UserEntity userEntity = new UserEntity();

		userEntity.setUserName(registerDto.getUserName());
		userEntity.setUserId(registerDto.getUserId());
		userEntity.setUserEmail(registerDto.getUserEmail());
		userEntity.setUserPassword(passwordEncoder.encode(registerDto.getUserPassword()));
		userEntity.setUserAuthority(registerDto.isAdminYn()?"ADMIN":"USER");
		userEntity.setActive(true);

		logger.info("this is userEntity : " + userEntity);

		// 유저 정보 엔티티 저장
		userRepository.save(userEntity);

		return util.setResult("0000", true, "Success register", null);
	}

	/**
	 * 로그인 서비스
	 * 
	 * @param loginDto 로그인 정보 DTO
	 * 
	 * @return 로그인 결과 DTO
	 */

	public ResultDto<TokenDto> login(LoginDto loginDto) {

		// 로그인 시작 (로그인 실패시 401리턴)
		Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUserId(), loginDto.getUserPassword()));

		SecurityContextHolder.getContext()
			.setAuthentication(authenticate);

		// 정상 로그인시 JWT 토큰 발급
		String   token    = jwtProvider.generateToken(authenticate);

		// 리턴값 생성
		TokenDto tokenDto = TokenDto.builder()
			.accessToken(token)
			.refreshToken(refreshTokenService.generateRefreshToken()
				.getToken())
			.expiresAt(Instant.now()
				.plusMillis(jwtProvider.getJwtExpirationInMillis()))
			.userId(loginDto.getUserId())
			.build();

		return util.setResult("0000", true, "Success login", tokenDto);
	}

	/**
	 * JWT 토큰 재발급 서비스
	 * 
	 * @param refreshTokenDto 토큰 재발급 관련 DTO
	 * 
	 * @return 토근 재발급 결과 DTO
	 */

	@Transactional(readOnly = true)
	public ResultDto<TokenDto> refreshToken(RefreshTokenDto refreshTokenDto) {

		// 리프레쉬 토큰 유효성 검사
		refreshTokenService.validateRefreshToken(refreshTokenDto.getRefreshToken());

		// JWT 토큰 재발급
		String   token    = jwtProvider.generateTokenWithUserName(refreshTokenDto.getUserId());

		// 리턴값 생성
		TokenDto tokenDto = TokenDto.builder()
			.accessToken(token)
			.refreshToken(refreshTokenDto.getRefreshToken())
			.expiresAt(Instant.now()
				.plusMillis(jwtProvider.getJwtExpirationInMillis()))
			.userId(refreshTokenDto.getUserId())
			.build();

		return util.setResult("0000", true, "Success refresh token", tokenDto);
	}

	@Transactional(readOnly = true)
	public UserEntity getCurrentUser() {
		org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.getContext()
			.getAuthentication()
			.getPrincipal();
		return userRepository.findByUserId(principal.getUsername())
			.orElseThrow(() -> new EntityNotFoundException("User name not found - " + principal.getUsername()));
	}

	public boolean isLoggedIn() {
		Authentication authentication = SecurityContextHolder.getContext()
			.getAuthentication();
		return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
	}
}