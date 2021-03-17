package com.brchain.account.service;

import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.account.entity.RefreshTokenEntity;
import com.brchain.account.repository.RefreshTokenRepository;
import com.brchain.common.dto.ResultDto;
import com.brchain.core.util.Util;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final Util                   util;

	/**
	 * 리프레시 토큰 생성 서비스
	 * 
	 * @return 생성된 토큰 정보 Entity
	 * 
	 */

	public RefreshTokenEntity generateRefreshToken() {
		RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();

		// 무작위 값으로 토큰 생성
		refreshTokenEntity.setToken(UUID.randomUUID()
			.toString());

		return refreshTokenRepository.save(refreshTokenEntity);
	}

	/**
	 * 리프레시 토큰 유효성 체크 서비스
	 * 
	 * @param refreshToken 리프레시 토큰 값
	 * 
	 */

	public void validateRefreshToken(String refreshToken) {
		refreshTokenRepository.findByToken(refreshToken)
			.orElseThrow(() -> new EntityNotFoundException("Invalid refresh Token"));
	}

	/**
	 * 리프레시 토큰 정보 삭제 서비스
	 * 
	 * @param refreshToken 삭제할 리프레시 토큰 값
	 * 
	 * @return 토큰 삭제 결과
	 */

	public ResultDto deleteRefreshToken(String refreshToken) {
		refreshTokenRepository.deleteByToken(refreshToken);
		return util.setResult("0000", true, "Success logout user", null);
	}
}