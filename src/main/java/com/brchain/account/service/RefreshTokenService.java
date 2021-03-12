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
	private final Util util;

	public RefreshTokenEntity generateRefreshToken() {
		RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
		refreshTokenEntity.setToken(UUID.randomUUID().toString());

		return refreshTokenRepository.save(refreshTokenEntity);
	}

	public void validateRefreshToken(String token) {
		refreshTokenRepository.findByToken(token)
				.orElseThrow(() -> new EntityNotFoundException("Invalid refresh Token"));
	}

	public ResultDto deleteRefreshToken(String token) {
		refreshTokenRepository.deleteByToken(token);
		return util.setResult("0000", true, "Success logout user", null);
	}
}